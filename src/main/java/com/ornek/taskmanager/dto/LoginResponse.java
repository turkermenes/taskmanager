package com.ornek.taskmanager.dto;

public record LoginResponse(
        String accessToken,
        String tokenType,
        long expiresIn
) {}