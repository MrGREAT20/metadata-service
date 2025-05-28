package com.flairlabs.workflow.services.metadata.metadata_service.exception.dto;

public class MissingTenantException extends RuntimeException {
    public MissingTenantException(String tenantIdIsMissing) {
        super(tenantIdIsMissing);
    }
}
