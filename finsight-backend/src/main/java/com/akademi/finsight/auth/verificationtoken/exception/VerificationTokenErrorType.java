package com.akademi.finsight.auth.verificationtoken.exception;

import com.akademi.finsight.common.exception.BaseErrorType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum VerificationTokenErrorType implements BaseErrorType {

    VERIFICATION_TOKEN_INVALID("error.verification.token.invalid", HttpStatus.BAD_REQUEST);

    private final String messageKey;
    private final HttpStatus httpStatus;

    @Override
    public String getCode() {
        return name();
    }
}
