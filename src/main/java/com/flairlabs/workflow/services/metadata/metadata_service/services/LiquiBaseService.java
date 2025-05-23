package com.flairlabs.workflow.services.metadata.metadata_service.services;

import com.flairlabs.workflow.services.metadata.metadata_service.models.EntityDefinition;
import com.flairlabs.workflow.services.metadata.metadata_service.models.FieldDefinition;
import com.flairlabs.workflow.services.metadata.metadata_service.models.RuntimeChangelog;
import com.flairlabs.workflow.services.metadata.metadata_service.repositories.IRuntimeChangelogRepository;
import com.flairlabs.workflow.services.metadata.metadata_service.utilities.LiquibaseUtility;
import com.flairlabs.workflow.services.metadata.metadata_service.utilities.enums.FieldDataType;
import com.flairlabs.workflow.services.metadata.metadata_service.utilities.enums.FieldType;
import liquibase.Contexts;
import liquibase.LabelExpression;
import liquibase.Liquibase;
import liquibase.change.ColumnConfig;
import liquibase.change.ConstraintsConfig;
import liquibase.change.core.CreateTableChange;
import liquibase.change.core.RawSQLChange;
import liquibase.changelog.ChangeSet;
import liquibase.changelog.DatabaseChangeLog;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.LiquibaseException;
import liquibase.resource.ClassLoaderResourceAccessor;
import liquibase.statement.DatabaseFunction;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LiquiBaseService {

    private final DataSource dataSource;
    private final String author = "system";

    @Autowired
    private LiquibaseUtility liquibaseUtility;

    @Autowired
    private IRuntimeChangelogRepository runtimeChangelogRepository;

    public void provisionTenant(String tenantId, List<EntityDefinition> coreObjects) throws Exception {

        String changeLogId = this.liquibaseUtility.generateChangeSetId( new ArrayList<>(List.of("initial", tenantId)));
        DatabaseChangeLog changelog = this.liquibaseUtility.createEmptyChangeLog(changeLogId);

        this.createSchema(tenantId);

        coreObjects.forEach(ed -> {
            ChangeSet tableChangeSet = this.createTableChangeSet(ed, tenantId, changelog);
            changelog.addChangeSet(tableChangeSet);
        });

        this.applyChangeLog(changelog, tenantId);

        RuntimeChangelog runtimeChangelog = RuntimeChangelog.builder().changelogXml(liquibaseUtility.convertChangeLogToXml(changelog)).build();
        runtimeChangelogRepository.save(runtimeChangelog);
    }

    public void createTable(EntityDefinition entityDefinition, String tenantId) throws Exception {
        String changeLogId = this.liquibaseUtility.generateChangeSetId(new ArrayList<>(List.of("create", "table", entityDefinition.getEntityId().toString(), entityDefinition.getName(), tenantId)));
        DatabaseChangeLog changelog = this.liquibaseUtility.createEmptyChangeLog(changeLogId);

        ChangeSet tableChangeSet = this.createTableChangeSet(entityDefinition, tenantId, changelog);
        changelog.addChangeSet(tableChangeSet);

        this.applyChangeLog(changelog, tenantId);

        RuntimeChangelog runtimeChangelog = RuntimeChangelog.builder().changelogXml(liquibaseUtility.convertChangeLogToXml(changelog)).build();
        runtimeChangelogRepository.save(runtimeChangelog);
    }


    private ChangeSet createTableChangeSet(EntityDefinition entityDefinition, String tenantId, DatabaseChangeLog changeLog) {
        String changeSetId = liquibaseUtility.generateChangeSetId(new ArrayList<>(List.of("create-table", tenantId, entityDefinition.getName())));

        ChangeSet result = this.liquibaseUtility.createNewChangeSet(changeSetId, this.author, changeLog);

        CreateTableChange createTableChange = this.liquibaseUtility.createTableChange(entityDefinition.getName().toLowerCase(), tenantId);

        for (FieldDefinition fd : entityDefinition.getFields()) {
            createTableChange.addColumn(this.createColumnConfig(fd));
        }

        result.addChange(createTableChange);

        return result;
    }

    private void applyChangeLog(DatabaseChangeLog changelog, String tenantId) throws LiquibaseException, SQLException {
        Database database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(dataSource.getConnection()));
        database.setDefaultSchemaName(tenantId);
        Liquibase liquibase = new Liquibase(changelog, new ClassLoaderResourceAccessor(), database);
        liquibase.update(new Contexts(), new LabelExpression());

    }

    private ColumnConfig createColumnConfig(FieldDefinition fieldDefinition) {
        ColumnConfig columnConfig = new ColumnConfig();

        columnConfig.setName(fieldDefinition.getName());
        columnConfig.setType(this.liquibaseUtility.mapFieldDataTypeToSqlType(fieldDefinition.getFieldDataType(), fieldDefinition.getMaxLength()));

        ConstraintsConfig constraintsConfig = new ConstraintsConfig();

        constraintsConfig.setNullable(fieldDefinition.getRequired());

        if (fieldDefinition.getFieldType().equals(FieldType.PRIMARY_KEY)) {
            constraintsConfig.setPrimaryKey(true);
            if (fieldDefinition.getFieldDataType().equals(FieldDataType.NUMBER)) {
                columnConfig.setAutoIncrement(fieldDefinition.getAutoGenerate());
            } else if (fieldDefinition.getFieldDataType().equals(FieldDataType.UUID) && fieldDefinition.getAutoGenerate()) {
                columnConfig.setDefaultValueComputed(new DatabaseFunction("gen_random_uuid()"));
            }
        } else if (fieldDefinition.getFieldType().equals(FieldType.COLUMN)) {
            if (fieldDefinition.getDefaultValue() != null && !fieldDefinition.getDefaultValue().isEmpty()) {
                columnConfig.setDefaultValue(fieldDefinition.getDefaultValue());
            }
        }

        columnConfig.setConstraints(constraintsConfig);
        return columnConfig;
    }

    private void createSchema(String tenantId) throws SQLException {
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("CREATE SCHEMA IF NOT EXISTS \"" + tenantId + "\"");
        }
    }

}
