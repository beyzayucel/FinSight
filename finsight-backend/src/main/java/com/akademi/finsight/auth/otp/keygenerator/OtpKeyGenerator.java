package com.akademi.finsight.auth.otp.keygenerator;

import com.akademi.finsight.auth.ratelimiter.util.IdentifierHasher;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

import static com.akademi.finsight.auth.otp.constant.OtpKeyConstants.*;

@Component
@RequiredArgsConstructor
public class OtpKeyGenerator {
    private final IdentifierHasher identifierHasher;

    public String generateCodeKey(String email) {
        return generateKey(CODE_SUFFIX, email);
    }

    public String generateCooldownKey(String email) {
        return generateKey(COOLDOWN_SUFFIX, email);
    }

    public String generateAttemptsKey(String email) {
        return generateKey(ATTEMPTS_SUFFIX, email);
    }

    private String generateKey(String suffixPattern, String email) {
        return PREFIX + String.format(suffixPattern, hashEmail(email));
    }

    private String hashEmail(String email){
        return Optional.ofNullable(email)
                .map(String::trim)
                .filter(trimmed -> !trimmed.isBlank())
                .map(String::toLowerCase)
                .map(identifierHasher::hash)
                .orElseThrow(() -> new IllegalArgumentException("Email address cannot be null or empty for key generation."));
    }

}
