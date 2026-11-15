package dev.turkermenes.taskmanager.config;

import jakarta.validation.constraints.NotEmpty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@Validated
@ConfigurationProperties(prefix = "app.cors")
public record CorsProperties(

        @NotEmpty(message = "En az bir izinli origin tanımlanmalı (app.cors.allowed-origins")
        List<String> allowedOrigins
) {}
