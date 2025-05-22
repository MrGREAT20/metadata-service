package com.flairlabs.workflow.services.metadata.metadata_service.dto;

import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.flairlabs.workflow.services.metadata.metadata_service.models.FieldDefinition.FieldDataType;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UpdateFieldDto {

    @JsonProperty("field_id")
    private String fieldId;

    @JsonProperty("field_name")
    private Optional<String> changeFieldName;

    @JsonProperty("changeField_data_type")
    private Optional<FieldDataType> changeFieldDataType;

    @JsonProperty("change_max_length")
    private Optional<Integer> changeMaxLength;

    @JsonProperty("change_default_value")
    private Optional<Integer> changeDefaultValue;

}
