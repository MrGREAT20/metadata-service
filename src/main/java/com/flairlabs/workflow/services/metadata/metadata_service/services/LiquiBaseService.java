package com.flairlabs.workflow.services.metadata.metadata_service.services;

import com.flairlabs.workflow.services.metadata.metadata_service.models.EntityDefinition;
import com.flairlabs.workflow.services.metadata.metadata_service.models.FieldDefinition;
import liquibase.change.ColumnConfig;
import liquibase.change.ConstraintsConfig;
import liquibase.change.core.CreateTableChange;
import liquibase.change.core.RawSQLChange;
import liquibase.changelog.ChangeSet;
import liquibase.changelog.DatabaseChangeLog;
import liquibase.statement.DatabaseFunction;

import java.time.Instant;

public class LiquiBaseService {

    private final String author = "system";

    private String generateChangeSetId(String key) {
        return key + Instant.now();
    }

    public ChangeSet createTenantSchema(String tenantId, DatabaseChangeLog changeLog) {
        String changeSetId = generateChangeSetId("create-tenant-schema-" + tenantId);

        ChangeSet result = new ChangeSet(
                changeSetId,       // id
                this.author,       // author
                false,             // alwaysRun
                false,             // runOnChange
                null,              // filePath (optional or set logical path)
                null,              // context
                null,              // dbms
                changeLog          // required DatabaseChangeLog instance
        );

        // Add your actual change here — for example, create schema:
        RawSQLChange createSchemaSql = new RawSQLChange();
        createSchemaSql.setSql("CREATE SCHEMA IF NOT EXISTS \"" + tenantId + "\"");

        result.addChange(createSchemaSql);

        return result;
    }


    public DatabaseChangeLog createEmptyChangeLog(String inputKey) {
        String filePath = "changelogs/db.changelog-" + inputKey + ".xml";

        DatabaseChangeLog result = new DatabaseChangeLog(filePath);
        result.setPhysicalFilePath(filePath);
        result.setChangeLogParameters(new liquibase.changelog.ChangeLogParameters());

        return result;
    }

    public ChangeSet createTableChangeSet(EntityDefinition entityDefinition, String tenantId, DatabaseChangeLog changeLog) {
        String changeSetId = generateChangeSetId("create-table-" + tenantId + "-" + entityDefinition.getName());

        ChangeSet result = new ChangeSet(
                changeSetId,       // id
                this.author,       // author
                false,             // alwaysRun
                false,             // runOnChange
                null,              // filePath (optional or set logical path)
                null,              // context
                null,              // dbms
                changeLog          // required DatabaseChangeLog instance
        );

        CreateTableChange createTableChange = new CreateTableChange();

        //Added Table name
        createTableChange.setTableName(entityDefinition.getName().toLowerCase());
        createTableChange.setSchemaName(tenantId);

        for (FieldDefinition fd : entityDefinition.getFields()) {
            createTableChange.addColumn(createColumnConfig(fd));
        }

        // Add audit columns
        ColumnConfig createdAtColumn = new ColumnConfig();
        createdAtColumn.setName("created_at");
        createdAtColumn.setType("TIMESTAMP");
        createdAtColumn.setDefaultValueComputed(new DatabaseFunction("CURRENT_TIMESTAMP"));
        createTableChange.addColumn(createdAtColumn);

        ColumnConfig updatedAtColumn = new ColumnConfig();
        updatedAtColumn.setName("updated_at");
        updatedAtColumn.setType("TIMESTAMP");
        updatedAtColumn.setDefaultValueComputed(new DatabaseFunction("CURRENT_TIMESTAMP"));
        createTableChange.addColumn(updatedAtColumn);

        result.addChange(createTableChange);

        return result;
    }

    private String mapFieldDataTypeToSqlType(FieldDefinition.FieldDataType fieldDataType, Integer maxLength) {
        return switch (fieldDataType) {
            case TEXT -> {
                if (maxLength != null && maxLength > 0) {
                    yield "VARCHAR(" + Math.min(maxLength, 4000) + ")";
                }
                yield "VARCHAR(255)";
            }
            case NUMBER -> "INTEGER";
            case DECIMAL -> "DECIMAL(19, 2)";
            case BOOLEAN -> "BOOLEAN";
            case DATE -> "DATE";
            case UUID -> "UUID";
            case DATETIME -> "TIMESTAMP";
            case EMAIL, URL, PHONE -> {
                if (maxLength != null && maxLength > 0) {
                    yield "VARCHAR(" + Math.min(maxLength, 255) + ")";
                }
                yield "VARCHAR(100)";
            }
            default -> "VARCHAR(255)";
        };
    }

    private ColumnConfig createColumnConfig(FieldDefinition fieldDefinition){
        ColumnConfig columnConfig = new ColumnConfig();
        columnConfig.setName(fieldDefinition.getName());
        columnConfig.setType(this.mapFieldDataTypeToSqlType(fieldDefinition.getFieldDataType(), fieldDefinition.getMaxLength()));
        ConstraintsConfig constraintsConfig = new ConstraintsConfig();
        if (fieldDefinition.getFieldType().equals(FieldDefinition.FieldType.PRIMARY_KEY)) {
            constraintsConfig.setPrimaryKey(true).setNullable(false);
            if (fieldDefinition.getFieldDataType().equals(FieldDefinition.FieldDataType.NUMBER)) {
                columnConfig.setAutoIncrement(fieldDefinition.getAutoGenerate());
            } else if (fieldDefinition.getFieldDataType().equals(FieldDefinition.FieldDataType.UUID) && fieldDefinition.getAutoGenerate()) {
                columnConfig.setDefaultValueComputed(new DatabaseFunction("gen_random_uuid()"));
            }
        } else if (fieldDefinition.getFieldType().equals(FieldDefinition.FieldType.COLUMN)) {
            if (Boolean.TRUE.equals(fieldDefinition.getRequired())) {
                constraintsConfig.setNullable(false);
            }
            if (fieldDefinition.getDefaultValue() != null && !fieldDefinition.getDefaultValue().isEmpty()) {
                columnConfig.setDefaultValue(fieldDefinition.getDefaultValue());
            }
        }

        if (constraintsConfig.isNullable() != null || constraintsConfig.isPrimaryKey() != null) {
            columnConfig.setConstraints(constraintsConfig);
        }

        return columnConfig;
    }
}
