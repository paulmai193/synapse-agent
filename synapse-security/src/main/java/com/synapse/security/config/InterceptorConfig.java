package com.synapse.security.config;

import com.synapse.security.authorization.DocumentAccessInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Configuration for registering security interceptors.
 */
@Configuration
public class InterceptorConfig implements WebMvcConfigurer {

    @Autowired
    private DocumentAccessInterceptor documentAccessInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(documentAccessInterceptor)
            .addPathPatterns("/api/documents/**", "/api/search/**", "/api/qa/**")
            .excludePathPatterns("/api/auth/**", "/api/setup/**");
    }
}