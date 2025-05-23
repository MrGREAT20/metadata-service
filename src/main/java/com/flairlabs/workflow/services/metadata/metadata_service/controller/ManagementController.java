package com.flairlabs.workflow.services.metadata.metadata_service.controller;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.flairlabs.workflow.services.metadata.metadata_service.multitenant.TenantContext;
import com.flairlabs.workflow.services.metadata.metadata_service.services.ObjectService;
import com.flairlabs.workflow.services.metadata.metadata_service.services.SeedObjectsService;
import liquibase.exception.LiquibaseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.flairlabs.workflow.services.metadata.metadata_service.dto.BaseResponseDto;
import com.flairlabs.workflow.services.metadata.metadata_service.dto.EntityRequestDto;
import com.flairlabs.workflow.services.metadata.metadata_service.dto.EntitySummaryDto;

@RestController
@RequestMapping("/api/management")
public class ManagementController {

    @Autowired
    ObjectService objectService;

    @GetMapping("/object")
    private ResponseEntity<List<EntitySummaryDto>> getAllTables(
            @RequestParam(required = false, name = "entity_name") Optional<String> entityName,
            @RequestParam(required = false, name = "entity_id") Optional<String> entityId) {
        List<EntitySummaryDto> result = objectService.getEntitySummary(entityId, entityName);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/object")
    private ResponseEntity<EntitySummaryDto> createOneTable(@RequestBody EntityRequestDto requestDto) throws Exception {
        // 1. Schema Validation Check
        // 2. Create Summary Obj and return
        EntitySummaryDto result = objectService.createEntityDefinition(requestDto, TenantContext.getTenantIdentifier());
        return ResponseEntity.ok(result);
    }

    @PutMapping("/object")
    private ResponseEntity<EntitySummaryDto> updateOneTable() {
        // 1. Schema Validation Check
        // 2. apply validation
        // 2. Create Summary and return
        EntitySummaryDto result = new EntitySummaryDto();
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/object/{entity_id}")
    private ResponseEntity<BaseResponseDto> deleteOneTable(@PathVariable(name = "entity_id") UUID entityId) {
        BaseResponseDto result = new BaseResponseDto("Deleted Successfully");
        return ResponseEntity.ok(result);
    }

    @PostMapping("/provision")
    private ResponseEntity<BaseResponseDto> provisionTenant() throws Exception {
        objectService.provisionTenant(TenantContext.getTenantIdentifier());
        BaseResponseDto result = new BaseResponseDto("Tenant Provision Successfully");
        return ResponseEntity.ok(result);
    }

}
