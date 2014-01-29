package com.rivermeadow.scheduler.web;

import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.SpringBootServletInitializer;

/**
 * Provides application configuration and components.
 * See: {@link SpringBootServletInitializer#configure(org.springframework.boot.builder.SpringApplicationBuilder)}
 */
public class WebXml extends SpringBootServletInitializer {
    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(Application.class);
    }
}
