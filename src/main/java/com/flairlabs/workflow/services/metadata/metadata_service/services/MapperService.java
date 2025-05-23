package com.flairlabs.workflow.services.metadata.metadata_service.services;

import com.flairlabs.workflow.services.metadata.metadata_service.dto.EntityRequestDto;
import com.flairlabs.workflow.services.metadata.metadata_service.dto.EntitySummaryDto;
import com.flairlabs.workflow.services.metadata.metadata_service.dto.FieldRequestDto;
import com.flairlabs.workflow.services.metadata.metadata_service.dto.FieldSummaryDto;
import com.flairlabs.workflow.services.metadata.metadata_service.models.EntityDefinition;
import com.flairlabs.workflow.services.metadata.metadata_service.models.FieldDefinition;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class MapperService {

    public EntityDefinition toEntityDefinition(EntityRequestDto entityRequestDto){

        EntityDefinition entityDefinition = EntityDefinition.builder()
                .name(entityRequestDto.getEntityName())
                .description(entityRequestDto.getDescription())
                .entityType(entityRequestDto.getEntityType())
                .build();

        if (entityRequestDto.getFields() != null) {
            List<FieldDefinition> fields = entityRequestDto.getFields().stream()
                    .map(fieldDto -> convertToFieldDefinition(fieldDto, entityDefinition))
                    .collect(Collectors.toList());

            entityDefinition.setFields(fields);
        }

        return entityDefinition;
    }

    public EntitySummaryDto toEntitySummaryDto(EntityDefinition entityDefinition){
        List<FieldSummaryDto> fieldSummaries = entityDefinition.getFields().stream()
                .map(this::convertToFieldSummaryDto)
                .collect(Collectors.toList());

        return new EntitySummaryDto(
                entityDefinition.getName(),
                entityDefinition.getEntityId().toString(),
                fieldSummaries,
                entityDefinition.getEntityType()
        );
    }

    //helper

    private FieldDefinition convertToFieldDefinition(FieldRequestDto fieldDto, EntityDefinition entityDefinition) {
        return FieldDefinition.builder()
                .entity(entityDefinition)
                .name(fieldDto.getFieldName())
                .fieldDataType(fieldDto.getFieldDataType())
                .fieldType(fieldDto.getFieldType())
                .required(fieldDto.getRequired())
                .maxLength(fieldDto.getMaxLength())
                .defaultValue(fieldDto.getDefaultValue())
                .autoGenerate(fieldDto.getAutoGenerate())
                .referenceEntityId(fieldDto.getReferenceEntityId())
                .referenceFieldId(fieldDto.getReferenceFieldId())
                .build();
    }

    private FieldSummaryDto convertToFieldSummaryDto(FieldDefinition fieldDefinition){
        return new FieldSummaryDto(
                fieldDefinition.getId().toString(),
                fieldDefinition.getName(),
                fieldDefinition.getFieldDataType(),
                fieldDefinition.getFieldType(),
                fieldDefinition.getMaxLength(),
                fieldDefinition.getAutoGenerate(),
                fieldDefinition.getReferenceEntityId(),
                fieldDefinition.getReferenceFieldId());
    }
}
