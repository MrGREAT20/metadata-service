package com.flairlabs.workflow.services.metadata.metadata_service.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class EntitySummaryDto {

    @JsonProperty("entity_name")
    private String entityName;

    @JsonProperty("entity_id")
    private String entityId;

    @JsonProperty("fields")
    private List<FieldSummaryDto> fields; // fieldId and fieldName
}
