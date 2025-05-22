package com.flairlabs.workflow.services.metadata.metadata_service.exception.dto;

public class InvalidFieldNameException extends RuntimeException {
    public InvalidFieldNameException(String message){
        super(message);
    }
}
