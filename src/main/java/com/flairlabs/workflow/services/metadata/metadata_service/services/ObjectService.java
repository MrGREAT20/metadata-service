package com.flairlabs.workflow.services.metadata.metadata_service.services;

import com.flairlabs.workflow.services.metadata.metadata_service.dto.EntityRequestDto;
import com.flairlabs.workflow.services.metadata.metadata_service.dto.EntitySummaryDto;
import com.flairlabs.workflow.services.metadata.metadata_service.dto.FieldRequestDto;
import com.flairlabs.workflow.services.metadata.metadata_service.dto.UpdateEnitityDto;
import com.flairlabs.workflow.services.metadata.metadata_service.exception.dto.DuplicateEntityException;
import com.flairlabs.workflow.services.metadata.metadata_service.exception.dto.DuplicateFieldsException;
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

    private void validateEntityUpdate(UpdateEnitityDto updateEnitityDto ){
        /**
         * check entityId and fieldId
         */
        // if table is present

        // field_to_update is present

        //

    }
    public EntitySummaryDto createEntityDefinition(EntityRequestDto entityRequestDto) throws Exception {
        validateEntityRequest(entityRequestDto);

        return new EntitySummaryDto();
    }

    public List<EntitySummaryDto> getEntitySummary(Optional<String> entityId, Optional<String> entityName){
        return new ArrayList<>();
    }

    public EntitySummaryDto updateEntityDefinition(UpdateEnitityDto updateEnitityDto){
        validateEntityUpdate(updateEnitityDto);
        return new EntitySummaryDto();
    }

    public void deleteEntity(Optional<String> entityId, Optional<String> entityName){

    }

    public void deleteField(Optional<String> fieldId, Optional<String> fieldName){

    }
}
