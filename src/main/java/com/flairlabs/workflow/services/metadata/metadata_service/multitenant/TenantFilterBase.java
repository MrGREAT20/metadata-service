package com.flairlabs.workflow.services.metadata.metadata_service.multitenant;

import java.io.IOException;

import com.flairlabs.workflow.services.metadata.metadata_service.dto.BaseResponseDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;


import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class TenantFilterBase extends OncePerRequestFilter {

    @Autowired
    MultitenancyProperties multitenancyProperties;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException, RuntimeException {
        String tenant = request.getHeader(multitenancyProperties.getApiHeaderKey());
        if(tenant == null || tenant.isEmpty()){
            //throw new RuntimeException("TENANT IS NOT PRESENT");
            // SCHEMA PRESENT HAI KI NHI
            BaseResponseDto errorResponse = new BaseResponseDto("TENANT IS NOT PRESENT");
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.setContentType("application/json");
            response.getWriter().write(errorResponse.getMessage());
            return;
        }
        TenantContext.setTenantIdentifier(tenant);
        try {
            filterChain.doFilter(request, response);
        } finally {
            TenantContext.clear();
        }
    }

}
