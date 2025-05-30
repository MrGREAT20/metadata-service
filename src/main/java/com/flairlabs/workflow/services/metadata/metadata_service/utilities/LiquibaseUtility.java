package com.flairlabs.workflow.services.metadata.metadata_service.utilities;

import com.flairlabs.workflow.services.metadata.metadata_service.utilities.enums.FieldDataType;
import liquibase.change.core.*;
import liquibase.changelog.ChangeSet;
import liquibase.changelog.DatabaseChangeLog;
import liquibase.exception.LiquibaseException;
import liquibase.serializer.ChangeLogSerializer;
import liquibase.serializer.core.xml.XMLChangeLogSerializer;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.time.Instant;
import java.util.List;

@Component
public class LiquibaseUtility {

    public String generateChangeSetId(List<String> keys) {
        keys.add(Instant.now().toString());
        return String.join("::", keys);
    }

    public String mapFieldDataTypeToSqlType(FieldDataType fieldDataType, Integer maxLength) {
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

    public CreateTableChange createTableChange(String tableName, String schemaName){
        CreateTableChange result = new CreateTableChange();

        result.setSchemaName(schemaName);
        result.setTableName(tableName);

        return result;
    }

    public RenameColumnChange renameColumnchange(String tableName, String oldColumnName, String newColumnName) {
        RenameColumnChange result = new RenameColumnChange();

        result.setTableName(tableName);
        result.setOldColumnName(oldColumnName);
        result.setNewColumnName(newColumnName);

        return result;
    }

    public RenameTableChange renameTableChange(String oldTableName, String newTableName) {
        RenameTableChange result = new RenameTableChange();

        result.setOldTableName(oldTableName);
        result.setNewTableName(newTableName);

        return result;

    }

    public ModifyDataTypeChange modifyMaxLength(String tableName, String columnName, Integer newMaxLength, FieldDataType dataType, String tenantId) {
        ModifyDataTypeChange result = new ModifyDataTypeChange();

        result.setSchemaName(tenantId);
        result.setTableName(tableName);
        result.setColumnName(columnName);
        result.setNewDataType(mapFieldDataTypeToSqlType(dataType, newMaxLength));

        return result;
    }

    public AddDefaultValueChange defaultValueChange(String tableName, String columnName, Object newDefaultValue) {
        AddDefaultValueChange result = new AddDefaultValueChange();

        result.setTableName(tableName);
        result.setColumnName(columnName);
        if (newDefaultValue instanceof Integer) {
            result.setDefaultValueNumeric(String.valueOf(newDefaultValue));
        } else if (newDefaultValue instanceof Boolean) {
            result.setDefaultValueBoolean(Boolean.valueOf(String.valueOf(newDefaultValue)));
        } else {
            result.setDefaultValue(newDefaultValue.toString());
        }

        return result;
    }

    public AddColumnChange addColumnToTable(String tableName) {
        AddColumnChange result = new AddColumnChange();

        result.setTableName(tableName);

        return result;
    }

    public ChangeSet createNewChangeSet(String changeSetId, String author, DatabaseChangeLog changeLog){

        return new ChangeSet(
                changeSetId,       // id
                author,       // author
                false,             // alwaysRun
                false,             // runOnChange
                changeLog.getFilePath(),              // filePath (optional or set logical path)
                null,              // context
                null,              // dbms
                changeLog          // required DatabaseChangeLog instance
        );
    }

    public DatabaseChangeLog createEmptyChangeLog(String key) throws SQLException, LiquibaseException {
        String filePath = "changelogs/db.changelog-" + key + "-" + Instant.now() + ".xml";

        DatabaseChangeLog result = new DatabaseChangeLog(filePath);
        result.setPhysicalFilePath(filePath);
        result.setChangeLogParameters(new liquibase.changelog.ChangeLogParameters());

        return result;
    }

    public String convertChangeLogToXml(DatabaseChangeLog changeLog) throws Exception {
        ChangeLogSerializer serializer = new XMLChangeLogSerializer();

        // Write the changelog to an output stream
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        serializer.write(changeLog.getChangeSets(), outputStream);

        // Convert to String
        return outputStream.toString(StandardCharsets.UTF_8);
    }

    public DropColumnChange dropColumnChange(String tableName, String columnName) {
        DropColumnChange result = new DropColumnChange();
        result.setTableName(tableName);
        result.setColumnName(columnName);
        return result;
    }

    public DropTableChange dropTableChange(String tableName) {
        DropTableChange result = new DropTableChange();
        result.setTableName(tableName);
        return result;
    }
}
