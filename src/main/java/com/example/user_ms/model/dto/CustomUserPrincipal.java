package com.example.user_ms.model.dto;

public record CustomUserPrincipal(
    Long userId,
    String iss,
    String sub,
    String username,
    String email
) {}