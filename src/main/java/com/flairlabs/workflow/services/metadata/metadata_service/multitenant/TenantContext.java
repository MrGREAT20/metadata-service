package com.flairlabs.workflow.services.metadata.metadata_service.multitenant;

import org.springframework.lang.Nullable;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class TenantContext {
    private static final ThreadLocal<String> tenantIdentifier = new ThreadLocal<>();

    public static void setTenantIdentifier(String tenant) {
        tenantIdentifier.set(tenant);
    }

    @Nullable
    public static String getTenantIdentifier() {
        return tenantIdentifier.get();
    }

    public static void clear() {
        tenantIdentifier.remove();
    }
}
