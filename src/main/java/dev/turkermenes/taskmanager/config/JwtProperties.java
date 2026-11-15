package dev.turkermenes.taskmanager.config;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

@Validated
@ConfigurationProperties(prefix = "app.jwt")
public record JwtProperties(

        @Pattern(
                regexp = "^[A-Za-z0-9+/]{43,}=?$",
                message = "app.jwt.secret en az 32 byte'lık Base64 bir değer olmalı. JWT_SECRET ortam değişkeni tanımlı mı?"

        )
        String secret,

        @NotNull
        Duration expiration
) {}