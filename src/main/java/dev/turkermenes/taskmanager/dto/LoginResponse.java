package dev.turkermenes.taskmanager.dto;

public record LoginResponse(
        String accessToken,
        String tokenType,
        long expiresIn
) {}