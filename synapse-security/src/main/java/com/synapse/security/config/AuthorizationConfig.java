package com.synapse.security.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

/**
 * Configuration for enabling AOP-based authorization.
 */
@Configuration
@EnableAspectJAutoProxy
public class AuthorizationConfig {
}