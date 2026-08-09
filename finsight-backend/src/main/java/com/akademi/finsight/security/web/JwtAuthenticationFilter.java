package com.akademi.finsight.security.web;


import com.akademi.finsight.security.jwt.exception.JwtErrorType;
import com.akademi.finsight.security.jwt.exception.JwtTokenException;
import com.akademi.finsight.security.jwt.service.JwtService;
import com.akademi.finsight.security.jwt.service.TokenInvalidationService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final TokenInvalidationService tokenInvalidationService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String token = jwtService.extractToken(request);

        if (token != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                if (jwtService.isTokenValid(token)) {
                    String username = jwtService.getUsernameFromToken(token);

                    // Sifre degistiyse bu token'in suresi dolmamis olsa da artik gecerli degil
                    if (tokenInvalidationService.isInvalidated(username, jwtService.getIssuedAtFromToken(token))) {
                        log.info("JWT rejected: event=TOKEN_INVALIDATED_BY_PASSWORD_CHANGE");
                        filterChain.doFilter(request, response);
                        return;
                    }

                    var authorities = jwtService.getRolesFromToken(token).stream()
                            .map(SimpleGrantedAuthority::new)
                            .toList();

                    var authentication = new UsernamePasswordAuthenticationToken(
                            username, null, authorities);

                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            } catch (JwtTokenException e) {
                if (e.getErrorType() == JwtErrorType.JWT_EXPIRED) {
                    log.debug("JWT expired: event=TOKEN_EXPIRED");
                } else {
                    log.warn("JWT authentication failed: event=JWT_INVALID, reason={}", e.getErrorType().getCode());
                }
            } catch (Exception e) {
                log.warn("JWT authentication failed: event=JWT_UNEXPECTED, reason={}", e.getMessage());
            }
        }

        filterChain.doFilter(request, response);
    }
}
