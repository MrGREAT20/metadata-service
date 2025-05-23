package com.flairlabs.workflow.services.metadata.metadata_service.dto;

import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.flairlabs.workflow.services.metadata.metadata_service.utils.FieldDataType;
import com.flairlabs.workflow.services.metadata.metadata_service.utils.FieldType;

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
    private Optional<Boolean> required;

    @JsonProperty("auto_generate")
    private Optional<Boolean> autoGenerate;

    @JsonProperty("max_length")
    private Optional<Integer> maxLength;

    @JsonProperty("default_value")
    private Optional<String> defaultValue;

    // Only for FOREIGN_KEY
    @JsonProperty("reference_entity_id")
    private Optional<String> referenceEntityId;

    @JsonProperty("reference_field_id")
    private Optional<String> referenceFieldId;
}
