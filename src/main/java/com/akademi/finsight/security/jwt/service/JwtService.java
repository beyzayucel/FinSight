package com.akademi.finsight.security.jwt.service;


import com.akademi.finsight.security.jwt.config.JwtProperties;
import com.akademi.finsight.security.jwt.exception.JwtErrorType;
import com.akademi.finsight.security.jwt.exception.JwtTokenException;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.crypto.SecretKey;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
public class JwtService {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String CLAIM_ROLES = "roles";
    private static final String CLAIM_FIRST_LOGIN = "firstLogin";
    private static final String BEARER_PREFIX = "Bearer ";

    public String extractToken(HttpServletRequest request) {
        String header = request.getHeader(AUTHORIZATION_HEADER);
        if (StringUtils.hasText(header) && header.startsWith(BEARER_PREFIX)) {
            return header.substring(BEARER_PREFIX.length());
        }
        return null;
    }

    private final JwtProperties jwtProperties;

    public String generateAccessToken(UserDetails userDetails, boolean firstLogin) {
        var roles = userDetails.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        long expirationMillis = jwtProperties.getAccessTokenExpiry().toMillis();

        return Jwts.builder()
                .claims(Map.of(CLAIM_ROLES, roles, CLAIM_FIRST_LOGIN, firstLogin))
                .subject(userDetails.getUsername())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expirationMillis))
                .signWith(getSigningKey(), Jwts.SIG.HS256)
                .compact();
    }

    public long getAccessTokenExpiryMinutes() {
        return jwtProperties.getAccessTokenExpiry().toMinutes();
    }

    public String getUsernameFromToken(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public List<String> getRolesFromToken(String token) {
        return extractClaim(token, claims -> {
            List<String> roles = claims.get(CLAIM_ROLES, List.class);
            return roles != null ? roles : Collections.emptyList();
        });
    }

    public boolean isFirstLogin(String token) {
        return Boolean.TRUE.equals(extractClaim(token, claims -> claims.get(CLAIM_FIRST_LOGIN, Boolean.class)));
    }

    public boolean isTokenValid(String token) {
        try {
            extractAllClaims(token);
            return true;
        } catch (ExpiredJwtException e) {
            throw new JwtTokenException(JwtErrorType.JWT_EXPIRED, e);
        } catch (SignatureException e) {
            throw new JwtTokenException(JwtErrorType.JWT_INVALID_SIGNATURE, e);
        } catch (MalformedJwtException e) {
            throw new JwtTokenException(JwtErrorType.JWT_MALFORMED, e);
        } catch (UnsupportedJwtException e) {
            throw new JwtTokenException(JwtErrorType.JWT_UNSUPPORTED, e);
        } catch (Exception e) {
            throw new JwtTokenException(JwtErrorType.JWT_GENERAL, e);
        }
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtProperties.getSecret());
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
