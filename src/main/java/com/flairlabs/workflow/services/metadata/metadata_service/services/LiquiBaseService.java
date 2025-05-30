package com.flairlabs.workflow.services.metadata.metadata_service.services;

import com.flairlabs.workflow.services.metadata.metadata_service.dto.FieldRequestDto;
import com.flairlabs.workflow.services.metadata.metadata_service.dto.UpdateEntityDto;
import com.flairlabs.workflow.services.metadata.metadata_service.dto.UpdateFieldDto;
import com.flairlabs.workflow.services.metadata.metadata_service.models.EntityDefinition;
import com.flairlabs.workflow.services.metadata.metadata_service.models.FieldDefinition;
import com.flairlabs.workflow.services.metadata.metadata_service.models.RuntimeChangelog;
import com.flairlabs.workflow.services.metadata.metadata_service.repositories.IEntityDefinitionRepository;
import com.flairlabs.workflow.services.metadata.metadata_service.repositories.IFieldDefinitionRepository;
import com.flairlabs.workflow.services.metadata.metadata_service.repositories.IRuntimeChangelogRepository;
import com.flairlabs.workflow.services.metadata.metadata_service.utilities.LiquibaseUtility;
import com.flairlabs.workflow.services.metadata.metadata_service.utilities.enums.FieldDataType;
import liquibase.Contexts;
import liquibase.LabelExpression;
import liquibase.Liquibase;
import liquibase.change.AddColumnConfig;
import liquibase.change.ConstraintsConfig;
import liquibase.change.core.*;
import liquibase.changelog.ChangeSet;
import liquibase.changelog.DatabaseChangeLog;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.resource.ClassLoaderResourceAccessor;
import liquibase.statement.DatabaseFunction;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.sql.DataSource;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LiquiBaseService {

    private final DataSource dataSource;
    private final String author = "system";

    @Autowired
    private LiquibaseUtility liquibaseUtility;

    @Autowired
    private IRuntimeChangelogRepository runtimeChangelogRepository;
    @Autowired
    private IEntityDefinitionRepository entityDefinitionRepository;
    @Autowired
    private IFieldDefinitionRepository fieldDefinitionRepository;
    @Autowired
    private MapperService mapperService;

    public void provisionTenant(String tenantId, List<EntityDefinition> coreObjects) throws Exception {
        String changeLogId = this.liquibaseUtility.generateChangeSetId(new ArrayList<>(List.of("initial", tenantId)));
        DatabaseChangeLog changelog = this.liquibaseUtility.createEmptyChangeLog(changeLogId);

        this.createSchema(tenantId);

        coreObjects.forEach(ed -> {
            ChangeSet tableChangeSet = this.createTableChangeSet(ed, tenantId, changelog);
            changelog.addChangeSet(tableChangeSet);
        });

        this.applyChangeLog(changelog, tenantId);

        RuntimeChangelog runtimeChangelog = RuntimeChangelog.builder().changelogXml(liquibaseUtility.convertChangeLogToXml(changelog)).description("create-custom-objects").build();
        runtimeChangelogRepository.save(runtimeChangelog);
    }

    public void createTable(EntityDefinition entityDefinition, String tenantId) throws Exception {
        String changeLogId = this.liquibaseUtility.generateChangeSetId(new ArrayList<>(List.of("create", "table", entityDefinition.getName(), tenantId)));
        DatabaseChangeLog changelog = this.liquibaseUtility.createEmptyChangeLog(changeLogId);

        ChangeSet tableChangeSet = this.createTableChangeSet(entityDefinition, tenantId, changelog);
        changelog.addChangeSet(tableChangeSet);

        this.applyChangeLog(changelog, tenantId);
        RuntimeChangelog runtimeChangelog = RuntimeChangelog.builder().changelogXml(liquibaseUtility.convertChangeLogToXml(changelog)).description("create core objects").build();
        runtimeChangelogRepository.save(runtimeChangelog);
    }

    public void updateTable(UpdateEntityDto updateEntityDto, String tenantId) throws Exception {
        String changeLogId = this.liquibaseUtility.generateChangeSetId(new ArrayList<>(List.of("update", "table", updateEntityDto.getEntityId(), tenantId)));
        DatabaseChangeLog changeLog = this.liquibaseUtility.createEmptyChangeLog(changeLogId);
        EntityDefinition entityDefinition = this.entityDefinitionRepository.findById(UUID.fromString(updateEntityDto.getEntityId())).get();
        String tableName = entityDefinition.getName().toLowerCase();
        if (StringUtils.hasText(updateEntityDto.getUpdatedEntityName())) {
            String changeSetId = this.liquibaseUtility.generateChangeSetId(new ArrayList<>(List.of("rename-table", tenantId, tableName)));
            ChangeSet renameTablechangeSet = this.liquibaseUtility.createNewChangeSet(changeSetId, this.author, changeLog);
            RenameTableChange renameTable = this.liquibaseUtility.renameTableChange(tableName, updateEntityDto.getUpdatedEntityName());
            renameTablechangeSet.addChange(renameTable);
            changeLog.addChangeSet(renameTablechangeSet);
            tableName = updateEntityDto.getUpdatedEntityName().toLowerCase();
        }
        if (!updateEntityDto.getFieldsToUpdate().isEmpty()) {
            for (UpdateFieldDto ufd : updateEntityDto.getFieldsToUpdate()) {
                FieldDefinition field = this.fieldDefinitionRepository.findById(UUID.fromString(ufd.getFieldId())).get();
                String fieldName = field.getName();
                String changeSetId = this.liquibaseUtility.generateChangeSetId(new ArrayList<>(List.of("modify-field", tenantId, fieldName, tableName)));
                ChangeSet modifyFieldchangeSet = this.liquibaseUtility.createNewChangeSet(changeSetId, this.author, changeLog);
                if (ufd.getChangeFieldName() != null && ufd.getChangeFieldName().isPresent() && StringUtils.hasText(ufd.getChangeFieldName().get())) {
//                    fieldName = field.getName();
                    RenameColumnChange renameColumnChange = this.liquibaseUtility.renameColumnchange(tableName, fieldName, ufd.getChangeFieldName().get());
                    modifyFieldchangeSet.addChange(renameColumnChange);
                    fieldName = ufd.getChangeFieldName().get();
                }
                if (ufd.getChangeMaxLength() != null && ufd.getChangeMaxLength().isPresent()) {
                    ModifyDataTypeChange maxLengthChange = this.liquibaseUtility.modifyMaxLength(tableName, fieldName, ufd.getChangeMaxLength().get(), field.getFieldDataType(), tenantId);
                    modifyFieldchangeSet.addChange(maxLengthChange);
                }
                if (ufd.getChangeDefaultValue() != null && ufd.getChangeDefaultValue().isPresent()) {
                    AddDefaultValueChange changeDefaultValue = this.liquibaseUtility.defaultValueChange(tableName, fieldName, ufd.getChangeDefaultValue().get());
                    modifyFieldchangeSet.addChange(changeDefaultValue);
                }
                changeLog.addChangeSet(modifyFieldchangeSet);
            }

        }
        if (!updateEntityDto.getFieldsToAdd().isEmpty()) {
            String changeSetId = this.liquibaseUtility.generateChangeSetId(new ArrayList<>(List.of("add-field", tenantId, tableName)));
            ChangeSet modifyFieldchangeSet = this.liquibaseUtility.createNewChangeSet(changeSetId, this.author, changeLog);
            for (FieldRequestDto fd : updateEntityDto.getFieldsToAdd()) {
                AddColumnChange addColumnChange = this.liquibaseUtility.addColumnToTable(tableName);
                AddColumnConfig columnConfig = this.createColumnConfig(mapperService.convertToFieldDefinition(fd, entityDefinition));
                addColumnChange.addColumn(columnConfig);
                modifyFieldchangeSet.addChange(addColumnChange);
            }
            changeLog.addChangeSet(modifyFieldchangeSet);
        }
        this.applyChangeLog(changeLog, tenantId);

        RuntimeChangelog runtimeChangelog = RuntimeChangelog.builder().changelogXml(liquibaseUtility.convertChangeLogToXml(changeLog)).description("create core objects").build();
        runtimeChangelogRepository.save(runtimeChangelog);
    }

    public void deleteTable(String tableName, String tenantId) throws Exception {
        String changeLogId = this.liquibaseUtility.generateChangeSetId(new ArrayList<>(List.of("drop", "table", tableName, tenantId)));
        DatabaseChangeLog changeLog = this.liquibaseUtility.createEmptyChangeLog(changeLogId);

        ChangeSet dropTableChangeSet = this.liquibaseUtility.createNewChangeSet(changeLogId, this.author, changeLog);
        dropTableChangeSet.addChange(this.liquibaseUtility.dropTableChange(tableName));

        changeLog.addChangeSet(dropTableChangeSet);

        this.applyChangeLog(changeLog, tenantId);

        RuntimeChangelog runtimeChangelog = RuntimeChangelog.builder().changelogXml(liquibaseUtility.convertChangeLogToXml(changeLog)).description("delete object").build();
        runtimeChangelogRepository.save(runtimeChangelog);
    }

    public void deleteFieldsFromTable(String tableName, List<String> fieldsToDelete, String tenantId) throws Exception {

        List<String> changeLogParts = new ArrayList<>();
        changeLogParts.add("drop");
        changeLogParts.add("fields");
        changeLogParts.add("from");
        changeLogParts.add(tableName);
        changeLogParts.addAll(fieldsToDelete);  // Add all field names
        changeLogParts.add(tenantId);

        String changeLogId = this.liquibaseUtility.generateChangeSetId(changeLogParts);
        DatabaseChangeLog changeLog = this.liquibaseUtility.createEmptyChangeLog(changeLogId);

        String changeSetId = this.liquibaseUtility.generateChangeSetId(new ArrayList<>(List.of("delete-field", tableName, tenantId)));
        ChangeSet dropFieldChangeSet = this.liquibaseUtility.createNewChangeSet(changeSetId, this.author, changeLog);

        for (String field : fieldsToDelete) {
            DropColumnChange dropColumnChange = this.liquibaseUtility.dropColumnChange(tableName, field);
            dropFieldChangeSet.addChange(dropColumnChange);
        }

        changeLog.addChangeSet(dropFieldChangeSet);

        this.applyChangeLog(changeLog, tenantId);

        RuntimeChangelog runtimeChangelog = RuntimeChangelog.builder().changelogXml(liquibaseUtility.convertChangeLogToXml(changeLog)).description("delete fields").build();
        runtimeChangelogRepository.save(runtimeChangelog);
    }

    private ChangeSet createTableChangeSet(EntityDefinition entityDefinition, String tenantId, DatabaseChangeLog changeLog) {
        String changeSetId = this.liquibaseUtility.generateChangeSetId(new ArrayList<>(List.of("create-table", tenantId, entityDefinition.getName())));

        ChangeSet result = this.liquibaseUtility.createNewChangeSet(changeSetId, this.author, changeLog);

        CreateTableChange createTableChange = this.liquibaseUtility.createTableChange(entityDefinition.getName().toLowerCase(), tenantId);
        // Add Primary Key Column
        createTableChange.addColumn(this.addPrimaryKey());

        for (FieldDefinition fd : entityDefinition.getFields()) {
            createTableChange.addColumn(this.createColumnConfig(fd));
        }

        result.addChange(createTableChange);

        return result;
    }

    public void applyChangeLog(DatabaseChangeLog changelog, String tenantId) throws Exception {

        Connection conn = dataSource.getConnection();
        Database database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(conn));

        database.setDefaultSchemaName(tenantId);

        Liquibase liquibase = new Liquibase(changelog, new ClassLoaderResourceAccessor(), database);

        StringWriter writer = new StringWriter();
        liquibase.update(new Contexts(), new LabelExpression(), writer);
        String sqlToExecute = writer.toString();

        conn.setAutoCommit(false);
//
        try (Statement stmt = conn.createStatement()) {
            stmt.execute(sqlToExecute);
            conn.commit();
        } catch (Exception e) {
            conn.rollback();
            throw e;
        }
    }

    private AddColumnConfig createColumnConfig(FieldDefinition fieldDefinition) {
        AddColumnConfig columnConfig = new AddColumnConfig();

        columnConfig.setName(fieldDefinition.getName());
        columnConfig.setType(this.liquibaseUtility.mapFieldDataTypeToSqlType(fieldDefinition.getFieldDataType(), fieldDefinition.getMaxLength()));

        ConstraintsConfig constraintsConfig = new ConstraintsConfig();

        constraintsConfig.setNullable(!fieldDefinition.getRequired());
        constraintsConfig.setUnique(fieldDefinition.getIsUnique());

        if (fieldDefinition.getDefaultValue() != null && !fieldDefinition.getDefaultValue().isEmpty()) {
            columnConfig.setDefaultValue(fieldDefinition.getDefaultValue());
        }

        columnConfig.setConstraints(constraintsConfig);
        return columnConfig;
    }

    private void createSchema(String tenantId) throws SQLException {
        try (Connection conn = dataSource.getConnection(); Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("CREATE SCHEMA IF NOT EXISTS \"" + tenantId + "\"");
        }
    }

    private AddColumnConfig addPrimaryKey() {
        AddColumnConfig columnConfig = new AddColumnConfig();
        columnConfig.setName("id");
        columnConfig.setType(this.liquibaseUtility.mapFieldDataTypeToSqlType(FieldDataType.UUID, null));
        columnConfig.setDefaultValueComputed(new DatabaseFunction("gen_random_uuid()"));
        ConstraintsConfig constraintsConfig = new ConstraintsConfig();
        constraintsConfig.setPrimaryKey(true);
        constraintsConfig.setNullable(false);
        columnConfig.setConstraints(constraintsConfig);
        return columnConfig;
    }
}
