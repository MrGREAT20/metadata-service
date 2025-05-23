package com.flairlabs.workflow.services.metadata.metadata_service.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.flairlabs.workflow.services.metadata.metadata_service.utilities.enums.EntityType;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class EntityRequestDto {


    @JsonProperty("entity_name")
    private String entityName;

    @JsonProperty("description")
    private String description;

    @JsonProperty("fields")
    private List<FieldRequestDto> fields;

    @JsonProperty("entity_type")
    private EntityType entityType;
}
