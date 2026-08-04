package com.akademi.finsight.auth.otp.constant;

public class OtpKeyConstants {
    private OtpKeyConstants() {}
    public static final String PREFIX = "otp:email:";
    public static final String CODE_SUFFIX = "code:%s";
    public static final String COOLDOWN_SUFFIX = "cooldown:%s";
    public static final String ATTEMPTS_SUFFIX = "attempts:%s";
}
