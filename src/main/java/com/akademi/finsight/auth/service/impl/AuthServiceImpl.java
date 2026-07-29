package com.akademi.finsight.auth.service.impl;


import com.akademi.finsight.auth.exception.AuthErrorType;
import com.akademi.finsight.auth.exception.AuthException;
import com.akademi.finsight.auth.dto.login.LoginRequest;
import com.akademi.finsight.auth.dto.login.LoginResponse;
import com.akademi.finsight.auth.service.AuthService;
import com.akademi.finsight.common.masking.MaskType;
import com.akademi.finsight.security.jwt.service.JwtService;
import com.akademi.finsight.user.entity.User;
import com.akademi.finsight.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    @Override
    @Transactional
    public LoginResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.identifier(), request.password()));

        if (!(authentication.getPrincipal() instanceof UserDetails userDetails)) {
            throw new AuthException(AuthErrorType.INVALID_CREDENTIALS);
        }

        User user = userService.findByEmail(userDetails.getUsername());
        String accessToken = jwtService.generateAccessToken(userDetails, user.isFirstLogin());
        userService.updateLastLogin(user);

        log.info("User logged in: event=USER_LOGGED_IN, email={}", MaskType.EMAIL.mask(user.getEmail()));

        return new LoginResponse(accessToken, "result.rawToken()", jwtService.getAccessTokenExpiryMinutes(), user.isFirstLogin());
    }


}
