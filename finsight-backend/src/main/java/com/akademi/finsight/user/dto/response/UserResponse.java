package com.akademi.finsight.user.dto.response;

import java.time.Instant;
import java.util.UUID;

public record UserResponse(
        UUID id,
        String email,
        String username,
        String firstName,
        String lastName,
        String phoneNumber,
        boolean emailVerified,
        boolean enabled,
        boolean firstLogin,
        String role,
        Instant lastLoginAt,
        Instant createdAt
) {}
