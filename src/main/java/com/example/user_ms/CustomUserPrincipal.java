package com.example.user_ms;

public record CustomUserPrincipal(
    Long userId,
    String iss,
    String sub,
    String username,
    String email
) {}