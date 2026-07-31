package com.akademi.finsight.otp.constant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class OtpKeyConstants {
    public static final String PREFIX = "otp:email:";
    public static final String CODE_SUFFIX = "code:%s";
    public static final String COOLDOWN_SUFFIX = "cooldown:%s";
}
