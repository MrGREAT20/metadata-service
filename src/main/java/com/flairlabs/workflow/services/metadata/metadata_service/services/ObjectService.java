package com.flairlabs.workflow.services.metadata.metadata_service.services;

import com.flairlabs.workflow.services.metadata.metadata_service.dto.*;
import com.flairlabs.workflow.services.metadata.metadata_service.exception.dto.GenericException;
import com.flairlabs.workflow.services.metadata.metadata_service.exception.dto.InvalidFieldNameException;
import com.flairlabs.workflow.services.metadata.metadata_service.models.EntityDefinition;
import com.flairlabs.workflow.services.metadata.metadata_service.models.FieldDefinition;
import com.flairlabs.workflow.services.metadata.metadata_service.repositories.IEntityDefinitionRepository;
import com.flairlabs.workflow.services.metadata.metadata_service.repositories.IFieldDefinitionRepository;
import com.flairlabs.workflow.services.metadata.metadata_service.utils.FieldType;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

        if(!foreignKeys.isEmpty() && foreignKeys.stream().filter(fd -> fd.getReferenceEntityId().isPresent() && fd.getReferenceFieldId().isPresent()).toList().size() < foreignKeys.size()){
            throw new Exception("Foreign Keys Configuration is Invalid");
        }

        List<FieldDefinition> fieldDefinitions = this.fieldDefinitionRepository.findAllById(foreignKeys.stream().map(fd -> UUID.fromString(fd.getReferenceFieldId().get())).collect(Collectors.toSet()));

        for (FieldRequestDto ft: foreignKeys){
            Optional<FieldDefinition> fd = fieldDefinitions.stream().filter(e-> ft.getReferenceFieldId().isPresent() && e.getId().equals(UUID.fromString(ft.getReferenceFieldId().get()))).findFirst();

            if(fd.isEmpty() || !fd.get().getEntity().getEntityId().equals(UUID.fromString(ft.getReferenceEntityId().get()))){
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
                        if(temp.get().getFieldType().equals(FieldDefinition.FieldType.COLUMN)){

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

    public EntitySummaryDto createEntityDefinition(EntityRequestDto entityRequestDto) throws Exception {
        validateEntityRequest(entityRequestDto);
        EntityDefinition entityDefinition = mapperService.toEntityDefinition(entityRequestDto);
        return mapperService.toEntitySummaryDto(entityDefinitionRepository.save(entityDefinition));
    }

    public List<EntitySummaryDto> getEntitySummary(Optional<String> entityId, Optional<String> entityName){

        List<EntityDefinition> entityDefinitions = entityDefinitionRepository.findAll();
        return entityDefinitions.stream().map(e -> mapperService.toEntitySummaryDto(e)).toList();
    }

    public EntitySummaryDto updateEntityDefinition(UpdateEntityDto updateEnitityDto){
        //validateEntityUpdate(updateEnitityDto);
        // UPDATE TABLE
        return new EntitySummaryDto();
    }

    public void deleteEntity(Optional<String> entityId, Optional<String> entityName){

    }

    public void deleteField(Optional<String> fieldId, Optional<String> fieldName){

    }
}
