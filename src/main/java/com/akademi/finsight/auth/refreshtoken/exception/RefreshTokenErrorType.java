package com.akademi.finsight.auth.refreshtoken.exception;

import com.akademi.finsight.common.exception.BaseErrorType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum RefreshTokenErrorType implements BaseErrorType {

    REFRESH_TOKEN_NOT_FOUND("error.refresh.token.not.found", HttpStatus.NOT_FOUND),
    REFRESH_TOKEN_EXPIRED("error.refresh.token.expired", HttpStatus.UNAUTHORIZED),
    REFRESH_TOKEN_REVOKED("error.refresh.token.revoked", HttpStatus.UNAUTHORIZED);

    private final String messageKey;
    private final HttpStatus httpStatus;

    @Override
    public String getCode() {
        return name();
    }
}
