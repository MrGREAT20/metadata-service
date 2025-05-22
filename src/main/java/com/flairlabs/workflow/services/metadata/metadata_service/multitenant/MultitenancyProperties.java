package com.flairlabs.workflow.services.metadata.metadata_service.multitenant;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@ConfigurationProperties(prefix="multitenancy")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Component
public class MultitenancyProperties {
    
    private String apiHeaderKey; // api-header-key
    private String queueMessageHeaderKey; 
    private String defaultTenantId; 

}
