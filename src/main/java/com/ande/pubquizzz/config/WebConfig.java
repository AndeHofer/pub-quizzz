package com.ande.pubquizzz.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${app.upload.dir:/data/uploads}")
    private String uploadDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String location = uploadDir.startsWith("/")
                ? "file:" + uploadDir + "/"
                : "file:./" + uploadDir + "/";
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(location);

        // Explicitly serve static resources from classpath:/static/
        registry.addResourceHandler("/**")
                .addResourceLocations("classpath:/static/");
    }

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        // Simple forwarder for SPA routes, excluding the root path "/"
        // Path matches anything that doesn't contain a dot and is not empty
        registry.addViewController("/{path:[^.]+}")
                .setViewName("forward:/index.html");
    }
}
