package com.akademi.finsight.common.constants;


import lombok.Getter;

@Getter
public enum SupportedLanguage {
    TR("tr"),
    EN("en");

    private final String code;

    SupportedLanguage(String code) {
        this.code = code;
    }
}