package com.ecommerce.user.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan(basePackages = {"com.ecommerce.common.utils.security"})
public class JwtConfig {
    // JwtTokenUtil will be auto-discovered as it's annotated with @Component
}