package com.testplatform.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.stereotype.Controller;

@Configuration
@ComponentScan(
    basePackages = "com.testplatform",
    includeFilters = @ComponentScan.Filter(type = FilterType.ANNOTATION, classes = Controller.class)
    // Removed excludeFilters to allow all controllers to be scanned
)
public class ControllerConfig {
}