package com.flairlabs.workflow.services.metadata.metadata_service.services;

import com.flairlabs.workflow.services.metadata.metadata_service.dto.*;
import com.flairlabs.workflow.services.metadata.metadata_service.models.EntityDefinition;
import com.flairlabs.workflow.services.metadata.metadata_service.models.FieldDefinition;
import com.flairlabs.workflow.services.metadata.metadata_service.repositories.IEntityDefinitionRepository;
import com.flairlabs.workflow.services.metadata.metadata_service.repositories.IFieldDefinitionRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ObjectService {

    @Autowired
    private IEntityDefinitionRepository entityDefinitionRepository;

    @Autowired
    private IFieldDefinitionRepository fieldDefinitionRepository;

    @Autowired
    private MapperService mapperService;

    @Autowired
    private LiquiBaseService liquiBaseService;

    @Autowired
    private SeedObjectsService seedObjectsService;


    private void validateEntityRequest(EntityRequestDto entityRequestDto) throws Exception {
        // Exists
        if(this.entityDefinitionRepository.existsByName(entityRequestDto.getEntityName())){
            throw new Exception("Entity Already Present " + entityRequestDto.getEntityName());
        }

        if(entityRequestDto.getFields().stream().map(FieldRequestDto::getFieldName).collect(Collectors.toSet()).size() < entityRequestDto.getFields().size()){
            throw new Exception("Duplicate Fields Present");
        }

//        if(entityRequestDto.getFields().stream().noneMatch(fd -> fd.getFieldType().equals(FieldType.PRIMARY_KEY))){
//            throw new Exception("Primary Key Field is Required");
//        };

//        List<FieldRequestDto> foreignKeys = entityRequestDto.getFields().stream().filter(fd -> fd.getFieldType().equals(FieldType.FOREIGN_KEY)).toList();

//        if(!foreignKeys.isEmpty() && foreignKeys.stream().filter(fd -> fd.getReferenceEntityId() != null).toList().size() < foreignKeys.size()){
//            throw new Exception("Foreign Keys Configuration is Invalid");
//        }

//        List<FieldDefinition> fieldDefinitions = this.fieldDefinitionRepository.findAllById(foreignKeys.stream().map(fd -> UUID.fromString(fd.getReferenceFieldId())).collect(Collectors.toSet()));
//
//        for (FieldRequestDto ft: foreignKeys){
//            Optional<FieldDefinition> fd = fieldDefinitions.stream().filter(e-> ft.getReferenceFieldId() != null && e.getId().equals(UUID.fromString(ft.getReferenceFieldId()))).findFirst();
//
//            if(fd.isEmpty() || !fd.get().getEntity().getEntityId().equals(UUID.fromString(ft.getReferenceEntityId()))){
//                throw new Exception("Foreign Keys Configuration is Invalid");
//            }
//        }
    }

    @Transactional
    private void validateEntityUpdate(UpdateEntityDto updateEntityDto) throws Exception {

        Optional<EntityDefinition> entityDefinition = entityDefinitionRepository.findById(UUID.fromString(updateEntityDto.getEntityId()));

        if(entityDefinition.isPresent()){

            List<FieldDefinition> existingFields = entityDefinition.get().getFields().stream().toList();
            List<UpdateFieldDto> fieldsToUpdate = updateEntityDto.getFieldsToUpdate();
            List<FieldRequestDto> fieldsToAdd = updateEntityDto.getFieldsToAdd();

            List<String> newLabels = new ArrayList<>();
                for (UpdateFieldDto uf: fieldsToUpdate){
                    Optional<FieldDefinition> temp = existingFields.stream().filter(fd -> fd.getId().equals(UUID.fromString(uf.getFieldId()))).findFirst();

                    if(temp.isPresent()){
                        if (uf.getChangeFieldName() != null && uf.getChangeFieldName().isPresent()) {
                                newLabels.add(uf.getChangeFieldName().get());
                            } else {
                                newLabels.add(temp.get().getName());
                            }
                        if (uf.getChangeMaxLength() != null && uf.getChangeMaxLength().isPresent() && uf.getChangeMaxLength().get() < temp.get().getMaxLength()) {
                                throw new Exception("Field Id to update Max length should be greater than existing");
                            }
                    } else {
                        throw new Exception("Field Id to update not present");
                    }
                }

                if(fieldsToAdd.stream().anyMatch(r -> newLabels.contains(r.getFieldName()))){
                    throw new Exception("Field Id to add is already present or will be present on update");
                }

        } else {
            throw new Exception("Entity is Not Present");
        }

    }

    @Transactional
    public EntitySummaryDto createEntityDefinition(EntityRequestDto entityRequestDto, String tenantId) throws Exception {
        validateEntityRequest(entityRequestDto);
        EntityDefinition entityDefinition = mapperService.toEntityDefinition(entityRequestDto);

        liquiBaseService.createTable(entityDefinition, tenantId);

        return mapperService.toEntitySummaryDto(entityDefinitionRepository.save(entityDefinition));
    }

    public List<EntitySummaryDto> getEntitySummary(Optional<String> entityId, Optional<String> entityName){

        List<EntityDefinition> entityDefinitions = entityDefinitionRepository.findAll();
        return entityDefinitions.stream().map(e -> mapperService.toEntitySummaryDto(e)).toList();
    }

    @Transactional
    public EntitySummaryDto updateEntityDefinition(UpdateEntityDto updateEntityDto, String tenantId) throws Exception {
        validateEntityUpdate(updateEntityDto);
        this.liquiBaseService.updateTable(updateEntityDto, tenantId);

        EntityDefinition entityDefinition = this.entityDefinitionRepository.findById(UUID.fromString(updateEntityDto.getEntityId())).get();

        if (StringUtils.hasText(updateEntityDto.getUpdatedEntityName())) {
            entityDefinition.setName(updateEntityDto.getUpdatedEntityName());
        }

        if (!updateEntityDto.getFieldsToUpdate().isEmpty()) {
            for (UpdateFieldDto ufd : updateEntityDto.getFieldsToUpdate()) {
                FieldDefinition field = this.fieldDefinitionRepository.findById(UUID.fromString(ufd.getFieldId())).get();

                if (ufd.getChangeFieldName() != null && ufd.getChangeFieldName().isPresent() && StringUtils.hasText(ufd.getChangeFieldName().get())) {
                    field.setName(ufd.getChangeFieldName().get());
                }

                if (ufd.getChangeMaxLength() != null && ufd.getChangeMaxLength().isPresent()) {
                    field.setMaxLength(ufd.getChangeMaxLength().get());
                }

                if (ufd.getChangeDefaultValue() != null && ufd.getChangeDefaultValue().isPresent()) {
                    field.setDefaultValue(String.valueOf(ufd.getChangeDefaultValue().get()));
                }
            }
        }

        if (!updateEntityDto.getFieldsToAdd().isEmpty()) {
            for (FieldRequestDto fd : updateEntityDto.getFieldsToAdd()) {
                FieldDefinition addField = this.mapperService.convertToFieldDefinition(fd, entityDefinition);
                entityDefinition.addField(addField);
                this.entityDefinitionRepository.save(entityDefinition);
            }
        }

        return this.mapperService.toEntitySummaryDto(this.entityDefinitionRepository.findById(UUID.fromString(updateEntityDto.getEntityId())).get());
    }

    public void deleteEntity(String entityId, String tenantId) throws Exception {
        Optional<EntityDefinition> entityDefinition = entityDefinitionRepository.findById(UUID.fromString(entityId));
        String tableName = entityDefinition.get().getName();
        this.liquiBaseService.deleteTable(tableName, tenantId);
        this.entityDefinitionRepository.delete(entityDefinition.get());
    }

    public void provisionTenant(String tenantId) throws Exception {
        System.out.println("tenantId = " + tenantId);
        List<EntityDefinition> initialObjects = new ArrayList<>();

        for (EntityRequestDto er: seedObjectsService.getOnProvisonObjects()){
            this.validateEntityRequest(er);
            initialObjects.add(this.entityDefinitionRepository.save(mapperService.toEntityDefinition(er)));
        }

        this.liquiBaseService.provisionTenant(tenantId, initialObjects);
    }

    public void deleteFieldsFromEntity(String entityId, List<String> fieldIds, String tenantId) throws Exception {
        EntityDefinition entityDefinition = entityDefinitionRepository.findById(UUID.fromString(entityId)).orElseThrow(() -> new IllegalArgumentException("Entity not found for id: " + entityId));
        String tableName = entityDefinition.getName();

        List<String> fieldsToDelete = new ArrayList<>();
        List<FieldDefinition> fieldDefinitionsToRemove = new ArrayList<>();

        for (String fieldId : fieldIds) {
            FieldDefinition fieldDefinition = fieldDefinitionRepository.findById(UUID.fromString(fieldId)).orElseThrow(() -> new IllegalArgumentException("Field not found for id: " + fieldId));
            String fieldName = fieldDefinition.getName();
            fieldsToDelete.add(fieldName);
            fieldDefinitionsToRemove.add(fieldDefinition);
        }

        this.liquiBaseService.deleteFieldsFromTable(tableName, fieldsToDelete, tenantId);

        // Then update the entity model
        for (FieldDefinition fieldToRemove : fieldDefinitionsToRemove) {
            entityDefinition.removeField(fieldToRemove);
        }

        // Save updated entity
        entityDefinitionRepository.save(entityDefinition);

    }
}
