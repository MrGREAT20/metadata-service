package com.flairlabs.workflow.services.metadata.metadata_service.exception.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
public class DuplicateEntityException extends Exception {
    public DuplicateEntityException(String entityName){
        super("Entity Already Present %s".formatted(entityName));
    }
}
