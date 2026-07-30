package com.akademi.finsight.auth.ratelimiter.util;

import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

@Component
public class IdentifierHasher {
    public String hash(String value){

        if (value == null || value.isBlank()) {
            return "";
        }

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            byte[] hash = digest.digest(
                    value.getBytes(StandardCharsets.UTF_8)
            );

            return HexFormat.of()
                    .formatHex(hash);


        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
