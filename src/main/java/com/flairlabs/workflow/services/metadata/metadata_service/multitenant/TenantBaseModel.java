package com.flairlabs.workflow.services.metadata.metadata_service.multitenant;

import org.hibernate.annotations.TenantId;
import org.springframework.lang.NonNull;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@MappedSuperclass
public class TenantBaseModel {

    @JsonProperty("tenant_id")
    @Column(name="tenant_id")
    @NonNull
    @TenantId
    private String tenantId;

}
