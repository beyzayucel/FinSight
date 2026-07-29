package com.akademi.finsight.user.dto;

public record CreateUserResponse(
        String email,
        String username,
        String temporaryPassword
) {}
