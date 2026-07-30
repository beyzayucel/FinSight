package com.akademi.finsight.auth.refreshtoken.dto;


import com.akademi.finsight.auth.refreshtoken.entity.RefreshToken;

/**
 * Holds the raw (unhashed) token for client response
 * alongside the persisted entity (which stores the SHA-256 hash).
 */
public record RefreshTokenResult(String rawToken, RefreshToken refreshToken) {
}
