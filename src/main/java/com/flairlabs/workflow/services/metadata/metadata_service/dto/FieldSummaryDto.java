package com.flairlabs.workflow.services.metadata.metadata_service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.flairlabs.workflow.services.metadata.metadata_service.models.FieldDefinition.FieldDataType;
import com.flairlabs.workflow.services.metadata.metadata_service.models.FieldDefinition.FieldType;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class FieldSummaryDto {

    @JsonProperty("field_id")
    private String fieldId;

    @JsonProperty("field_name")
    private String fieldName;

    @JsonProperty("field_data_type")
    private FieldDataType fieldDataType;

    @JsonProperty("field_type")
    private FieldType fieldType;

    @JsonProperty("max_length")
    private Integer maxLength;

    // Only for FOREIGN_KEY
    @JsonProperty("reference_entity_id")
    private String referenceEntityId;

    @JsonProperty("reference_field_id")
    private String referenceFieldId;
    
}
