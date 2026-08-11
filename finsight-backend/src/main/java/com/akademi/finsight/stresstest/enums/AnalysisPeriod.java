package com.akademi.finsight.stresstest.enums;

public enum AnalysisPeriod {
    DAYS_10(10),
    DAYS_20(20),
    DAYS_30(30),
    DAYS_90(90);

    private final int days;

    AnalysisPeriod(int days) {
        this.days = days;
    }

    public int getDays() {
        return days;
    }
}
