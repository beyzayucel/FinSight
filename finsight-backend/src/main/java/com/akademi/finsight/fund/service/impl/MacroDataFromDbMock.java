package com.akademi.finsight.fund.service.impl;

import com.akademi.finsight.fund.dto.MacroDataRow;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Random;

public final class MacroDataFromDbMock {

    private static final Random RANDOM = new Random();

    private static final List<MacroDataRow> MOCK_DATA = List.of(

            new MacroDataRow(
                    LocalDate.of(2016, 1, 5),
                    new BigDecimal("0.01651830316190872"),
                    new BigDecimal("0.0030695273956511393"),
                    new BigDecimal("-0.021493901814082172"),
                    new BigDecimal("0.0013363123090261997"),
                    new BigDecimal("241.93"),
                    new BigDecimal("9.58"),
                    new BigDecimal("7.5")
            ),

            new MacroDataRow(
                    LocalDate.of(2016, 1, 6),
                    new BigDecimal("0.006989800502024357"),
                    new BigDecimal("0.012518545710656026"),
                    new BigDecimal("-0.06013176103277118"),
                    new BigDecimal("-0.03158356926839534"),
                    new BigDecimal("247.37"),
                    new BigDecimal("9.58"),
                    new BigDecimal("7.5")
            ),

            new MacroDataRow(
                    LocalDate.of(2016, 1, 7),
                    new BigDecimal("0.005346654776514637"),
                    new BigDecimal("0.01447012217651622"),
                    new BigDecimal("-0.014022773843279102"),
                    new BigDecimal("-0.011024312780637424"),
                    new BigDecimal("246.16"),
                    new BigDecimal("9.58"),
                    new BigDecimal("7.5")
            ),

            new MacroDataRow(
                    LocalDate.of(2016, 1, 8),
                    new BigDecimal("-0.00019988709080542844"),
                    new BigDecimal("-0.00893735016714281"),
                    new BigDecimal("-0.005925948531539382"),
                    new BigDecimal("-0.010682768538981424"),
                    new BigDecimal("245.01"),
                    new BigDecimal("9.58"),
                    new BigDecimal("7.5")
            ),

            new MacroDataRow(
                    LocalDate.of(2026, 7, 28),
                    new BigDecimal("0.0015"),
                    new BigDecimal("0.0020"),
                    new BigDecimal("-0.0105"),
                    new BigDecimal("0.0005"),
                    new BigDecimal("265.50"),
                    new BigDecimal("35.40"),
                    new BigDecimal("50.00")
            ),

            // Genel varsayılan mock veri
            new MacroDataRow(
                    LocalDate.of(2026, 9, 28),
                    new BigDecimal("0.0000"),
                    new BigDecimal("0.0000"),
                    new BigDecimal("0.0000"),
                    new BigDecimal("0.0000"),
                    new BigDecimal("250.00"),
                    new BigDecimal("35.00"),
                    new BigDecimal("50.00")
            ));

    private MacroDataFromDbMock() {
    }

    public static MacroDataRow fetchRandom() {
        return MOCK_DATA.get(RANDOM.nextInt(MOCK_DATA.size()));
    }
}

