package com.flairlabs.workflow.services.metadata.metadata_service.multitenant;
 import org.springframework.lang.NonNull;
 import org.springframework.stereotype.Component;
 import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
 import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

 @Component
 public class WebConfig implements WebMvcConfigurer{

     private final TenantInterceptor tenantInterceptor;

     public WebConfig(TenantInterceptor tenantInterceptor){
         this.tenantInterceptor = tenantInterceptor;
     }

     @Override
     public void addInterceptors(@NonNull InterceptorRegistry registry) {
         registry.addInterceptor(tenantInterceptor);
     }
 }
