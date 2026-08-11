package com.akademi.finsight.common.masking;

public enum MaskType {

    // al***@hotmail.com
    EMAIL {
        @Override
        public String mask(String value) {
            int at = value.indexOf('@');
            if (at <= 2) return "***" + value.substring(at);
            return value.substring(0, 2) + "***" + value.substring(at);
        }
    },
    // +90***4567
    PHONE {
        @Override
        public String mask(String value) {
            if (value.length() <= 4) return "****";
            return value.substring(0, 3) + "***" + value.substring(value.length() - 4);
        }
    },
    // admin → ad***
    USERNAME {
        @Override
        public String mask(String value) {
            if (value.length() <= 2) return value.charAt(0) + "***";
            return value.substring(0, 2) + "***";
        }
    },
    FULL {
        @Override
        public String mask(String value) {
            return "****";
        }
    };

    public abstract String mask(String value);

    public static String mask(MaskType type, String value) {
        if (value == null || value.isBlank()) return "****";
        return type.mask(value);
    }
}
