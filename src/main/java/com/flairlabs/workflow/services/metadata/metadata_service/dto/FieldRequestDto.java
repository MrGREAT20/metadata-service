package com.flairlabs.workflow.services.metadata.metadata_service.dto;

import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.flairlabs.workflow.services.metadata.metadata_service.utilities.enums.FieldDataType;
import com.flairlabs.workflow.services.metadata.metadata_service.utilities.enums.FieldType;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class FieldRequestDto {

    @JsonProperty("field_name")
    private String fieldName;

    @JsonProperty("field_data_type")
    private FieldDataType fieldDataType;

    @JsonProperty("field_type")
    private FieldType fieldType;

    @JsonProperty("required")
    private Boolean required;

    @JsonProperty("auto_generate")
    private Boolean autoGenerate;

    @JsonProperty("max_length")
    private Integer maxLength;

    @JsonProperty("default_value")
    private String defaultValue;

    // Only for FOREIGN_KEY
    @JsonProperty("reference_entity_id")
    private String referenceEntityId;

    @JsonProperty("reference_field_id")
    private String referenceFieldId;
}
