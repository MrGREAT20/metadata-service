package com.flairlabs.workflow.services.metadata.metadata_service.services;

import liquibase.Contexts;
import liquibase.LabelExpression;
import liquibase.Liquibase;
import liquibase.changelog.ChangeSet;
import liquibase.changelog.DatabaseChangeLog;
import liquibase.database.Database;
import liquibase.exception.LiquibaseException;
import liquibase.resource.ClassLoaderResourceAccessor;

import java.security.GeneralSecurityException;

public class LiquiBaseService {

//    private DatabaseChangeLog createChangeLog(String changeLogId){
//        DatabaseChangeLog databaseChangeLog = new DatabaseChangeLog("db.changelog-%s-%s.xml".formatted(changeLogId, "TIME"));
//        databaseChangeLog.setChangeLogParameters();
//    }
//
//    private ChangeSet createSchema(String tenantId){
//        //return new ChangeSet();
//    }
//
//    private ChangeSet create(String tenantId){
//        //return new ChangeSet();
//    }

    private void applyChangeLog(DatabaseChangeLog changeLog, Database database) throws LiquibaseException {
        Liquibase liquibase = new Liquibase(changeLog, new ClassLoaderResourceAccessor(), database);
        liquibase.update(new Contexts(), new LabelExpression());
    }

}
