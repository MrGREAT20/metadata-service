package com.flairlabs.workflow.services.metadata.metadata_service.exception;

import com.flairlabs.workflow.services.metadata.metadata_service.dto.BaseResponseDto;
import com.flairlabs.workflow.services.metadata.metadata_service.exception.dto.DuplicateEntityException;
import com.flairlabs.workflow.services.metadata.metadata_service.exception.dto.DuplicateFieldsException;
import com.flairlabs.workflow.services.metadata.metadata_service.exception.dto.GenericException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ObjectServiceExceptionController {

    @ExceptionHandler({GenericException.class})
    private ResponseEntity<BaseResponseDto> generic(RuntimeException e){
        return ResponseEntity.status(500).body(new BaseResponseDto(e.getMessage()));
    }
}
