package com.akademi.finsight.fund.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@Getter
@RequiredArgsConstructor
public enum StockCategory {

    HISSE_ASELS("ASELS"),
    HISSE_BIMAS("BIMAS"),
    HISSE_THYAO("THYAO"),
    HISSE_AKBNK("AKBNK"),
    HISSE_TUPRS("TUPRS"),
    HISSE_YKBNK("YKBNK"),
    HISSE_ISCTR("ISCTR"),
    HISSE_KCHOL("KCHOL"),
    HISSE_SAHOL("SAHOL"),
    HISSE_TCELL("TCELL"),
    HISSE_GARAN("GARAN"),
    HISSE_EREGL("EREGL"),
    HISSE_SISE("SISE"),
    HISSE_FROTO("FROTO"),
    HISSE_PGSUS("PGSUS"),
    HISSE_ENKAI("ENKAI"),
    HISSE_TOASO("TOASO"),
    HISSE_PETKM("PETKM"),
    HISSE_KOZAL("KOZAL"),
    HISSE_ARCLK("ARCLK"),
    HISSE_OYAKC("OYAKC"),
    HISSE_MGROS("MGROS"),
    HISSE_ASTOR("ASTOR"),
    HISSE_EKGYO("EKGYO"),
    HISSE_ALARK("ALARK"),
    HISSE_KONTR("KONTR"),
    HISSE_GUBRF("GUBRF"),
    HISSE_HEKTS("HEKTS"),
    HISSE_ODAS("ODAS"),
    HISSE_VESTL("VESTL"),
    HISSE_DIGER("Others");

    private final String ticker;

    public static StockCategory fromTicker(String ticker) {
        if (ticker == null || ticker.isBlank()) {
            return HISSE_DIGER;
        }
        return Arrays.stream(values())
                .filter(cat -> cat.ticker.equalsIgnoreCase(ticker.trim()) || cat.name().equalsIgnoreCase(ticker.trim()))
                .findFirst()
                .orElse(HISSE_DIGER);
    }
}
