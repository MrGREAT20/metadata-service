package com.flairlabs.workflow.services.metadata.metadata_service.multitenant;

import com.flairlabs.workflow.services.metadata.metadata_service.exception.dto.MissingTenantException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

 import org.springframework.lang.NonNull;
 import org.springframework.web.servlet.HandlerInterceptor;
 import org.springframework.web.servlet.ModelAndView;

 import jakarta.servlet.http.HttpServletRequest;
 import jakarta.servlet.http.HttpServletResponse;

 @Component
 public class TenantInterceptor implements HandlerInterceptor {

     @Autowired
     MultitenancyProperties multitenancyProperties;

     @Override
     public boolean preHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler) throws Exception {
         var tenantId = request.getHeader(multitenancyProperties.getApiHeaderKey());
         if (tenantId == null || tenantId.isBlank()) {
             throw new MissingTenantException("Tenant ID is missing");
         }
         TenantContext.setTenantIdentifier(tenantId);
         return true;
     }

     @Override
     public void postHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler, ModelAndView modelAndView) throws Exception {
         clear();
     }

     @Override
     public void afterCompletion(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler, Exception ex) throws Exception {
         clear();
     }

     void clear(){
         TenantContext.clear();
     }
 }

 // SPRING MVC
