package com.flairlabs.workflow.services.metadata.metadata_service.exception;

import com.flairlabs.workflow.services.metadata.metadata_service.dto.BaseResponseDto;
import com.flairlabs.workflow.services.metadata.metadata_service.exception.dto.DuplicateEntityException;
import com.flairlabs.workflow.services.metadata.metadata_service.exception.dto.DuplicateFieldsException;
import com.flairlabs.workflow.services.metadata.metadata_service.exception.dto.GenericException;
import com.flairlabs.workflow.services.metadata.metadata_service.exception.dto.MissingTenantException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ObjectServiceExceptionController {

    @ExceptionHandler({GenericException.class})
    private ResponseEntity<BaseResponseDto> generic(RuntimeException e){
        return ResponseEntity.status(500).body(new BaseResponseDto(e.getMessage()));
    }

    @ExceptionHandler(MissingTenantException.class)
    public ResponseEntity<String> handleMissingTenant(MissingTenantException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body("Tenant error: " + ex.getMessage());
    }
}
