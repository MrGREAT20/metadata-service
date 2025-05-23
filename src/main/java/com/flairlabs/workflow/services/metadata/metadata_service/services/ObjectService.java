package com.flairlabs.workflow.services.metadata.metadata_service.services;

import com.flairlabs.workflow.services.metadata.metadata_service.dto.*;
import com.flairlabs.workflow.services.metadata.metadata_service.models.EntityDefinition;
import com.flairlabs.workflow.services.metadata.metadata_service.models.FieldDefinition;
import com.flairlabs.workflow.services.metadata.metadata_service.repositories.IEntityDefinitionRepository;
import com.flairlabs.workflow.services.metadata.metadata_service.repositories.IFieldDefinitionRepository;
import com.flairlabs.workflow.services.metadata.metadata_service.utilities.LiquibaseUtility;
import com.flairlabs.workflow.services.metadata.metadata_service.utilities.enums.FieldType;

import liquibase.exception.LiquibaseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.util.*;
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

        if(entityRequestDto.getFields().stream().noneMatch(fd -> fd.getFieldType().equals(FieldType.PRIMARY_KEY))){
            throw new Exception("Primary Key Field is Required");
        };

        List<FieldRequestDto> foreignKeys = entityRequestDto.getFields().stream().filter(fd -> fd.getFieldType().equals(FieldType.FOREIGN_KEY)).toList();

        if(!foreignKeys.isEmpty() && foreignKeys.stream().filter(fd -> fd.getReferenceEntityId() != null).toList().size() < foreignKeys.size()){
            throw new Exception("Foreign Keys Configuration is Invalid");
        }

        List<FieldDefinition> fieldDefinitions = this.fieldDefinitionRepository.findAllById(foreignKeys.stream().map(fd -> UUID.fromString(fd.getReferenceFieldId())).collect(Collectors.toSet()));

        for (FieldRequestDto ft: foreignKeys){
            Optional<FieldDefinition> fd = fieldDefinitions.stream().filter(e-> ft.getReferenceFieldId() != null && e.getId().equals(UUID.fromString(ft.getReferenceFieldId()))).findFirst();

            if(fd.isEmpty() || !fd.get().getEntity().getEntityId().equals(UUID.fromString(ft.getReferenceEntityId()))){
                throw new Exception("Foreign Keys Configuration is Invalid");
            }
        }


    }

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
                        if(temp.get().getFieldType().equals(FieldType.COLUMN)){

                            if(uf.getChangeFieldName().isPresent()){
                                newLabels.add(uf.getChangeFieldName().get());
                            } else {
                                newLabels.add(temp.get().getName());
                            }

                            if(uf.getChangeMaxLength().isPresent() && uf.getChangeMaxLength().get() < temp.get().getMaxLength()){
                                throw new Exception("Field Id to update Max length should be greater than existing");
                            }

                        } else {
                            throw new Exception("Field Id to update should be only column");
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

    public EntitySummaryDto updateEntityDefinition(UpdateEntityDto updateEnitityDto) throws Exception {
        validateEntityUpdate(updateEnitityDto);
        // UPDATE TABLE
        return new EntitySummaryDto();
    }

    public void deleteEntity(Optional<String> entityId, Optional<String> entityName){

    }

    public void deleteField(Optional<String> fieldId, Optional<String> fieldName){

    }

    public void provisionTenant(String tenantId) throws Exception {
        System.out.println("tenantId = " + tenantId);
        List<EntityDefinition> initialObjects = new ArrayList<>();

        for (EntityRequestDto er: seedObjectsService.getOnProvisonObjects()){
            //this.validateEntityRequest(er);
            initialObjects.add(this.entityDefinitionRepository.save(mapperService.toEntityDefinition(er)));
        }

        this.liquiBaseService.provisionTenant(tenantId, initialObjects);

    }
}
