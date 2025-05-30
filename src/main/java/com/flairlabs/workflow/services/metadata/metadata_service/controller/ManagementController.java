package com.flairlabs.workflow.services.metadata.metadata_service.controller;

import com.flairlabs.workflow.services.metadata.metadata_service.dto.BaseResponseDto;
import com.flairlabs.workflow.services.metadata.metadata_service.dto.EntityRequestDto;
import com.flairlabs.workflow.services.metadata.metadata_service.dto.EntitySummaryDto;
import com.flairlabs.workflow.services.metadata.metadata_service.dto.UpdateEntityDto;
import com.flairlabs.workflow.services.metadata.metadata_service.multitenant.TenantContext;
import com.flairlabs.workflow.services.metadata.metadata_service.services.ObjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

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
        EntitySummaryDto result = objectService.createEntityDefinition(requestDto, TenantContext.getTenantIdentifier());
        return ResponseEntity.ok(result);
    }

    @PutMapping("/object")
    private ResponseEntity<EntitySummaryDto> updateOneTable(@RequestBody UpdateEntityDto updateEntityDto) throws Exception {
        EntitySummaryDto result = this.objectService.updateEntityDefinition(updateEntityDto, TenantContext.getTenantIdentifier());
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/object/{entity_id}")
    private ResponseEntity<BaseResponseDto> deleteOneTable(@PathVariable(name = "entity_id") String entityId) throws Exception {
        this.objectService.deleteEntity(entityId, TenantContext.getTenantIdentifier());
        BaseResponseDto result = new BaseResponseDto("Deleted Successfully");
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/object/{entity_id}/fields")
    public ResponseEntity<BaseResponseDto> deleteFieldsFromTable(@PathVariable("entity_id") String entityId, @RequestBody List<String> fieldIds // or fieldNames
    ) throws Exception {
        objectService.deleteFieldsFromEntity(entityId, fieldIds, TenantContext.getTenantIdentifier());
        return ResponseEntity.ok(new BaseResponseDto("Fields deleted successfully"));
    }

    @PostMapping("/provision")
    private ResponseEntity<BaseResponseDto> provisionTenant() throws Exception {
        objectService.provisionTenant(TenantContext.getTenantIdentifier());
        BaseResponseDto result = new BaseResponseDto("Tenant Provision Successfully");
        return ResponseEntity.ok(result);
    }

}
