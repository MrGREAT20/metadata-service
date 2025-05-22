package com.flairlabs.workflow.services.metadata.metadata_service.dto;

import java.util.List;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UpdateEntityDto {

    @JsonProperty("entity_id")
    private String entityId;

    @JsonProperty("updated_entity_name")
    private Optional<String> updatedEnityName;

    @JsonProperty("fields_to_update")
    private List<UpdateFieldDto> fieldsToUpdate;

    @JsonProperty("fields_to_add")
    private List<FieldRequestDto> fieldsToAdd;
}
