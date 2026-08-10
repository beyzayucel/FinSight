package com.akademi.finsight.fund.service;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface CdsDataService {

    BigDecimal getCdsSpreadForDate(LocalDate targetDate);
}
