package com.flairlabs.workflow.services.metadata.metadata_service.services;

import liquibase.changelog.ChangeSet;
import liquibase.changelog.DatabaseChangeLog;

public class LiquiBaseService {

    private DatabaseChangeLog createChangeLog(String changeLogId){
        return new DatabaseChangeLog("db.changelog-%s-%s.xml".formatted(changeLogId, "TIME"));
    }

//    private ChangeSet createSchema(String tenantId){
//        return new ChangeSet();
//    }
//
//    private ChangeSet create(String tenantId){
//        return new ChangeSet();
//    }

    private void applyChangeLog(DatabaseChangeLog changeLog){}

}
