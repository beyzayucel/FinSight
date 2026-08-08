package com.akademi.finsight.fund.constant;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MarketConstants {

    public static final String USD_TRY_CODE = "USD/TRY";
    public static final String GOLD_CODE = "XAUUSD";
    public static final String BRENT_CODE = "CL=F";
    public static final String BOND_10Y_CODE = "TAHVIL10Y";
    public static final String INFLATION_CODE = "TUCPIY";
    public static final String POLICY_RATE_CODE = "TCMBRPO";

    public static final BigDecimal DEFAULT_INFLATION = new BigDecimal("31.75");
    public static final BigDecimal DEFAULT_POLICY_RATE = new BigDecimal("50.00");
    public static final BigDecimal DEFAULT_CDS = new BigDecimal("250.00");
}
