package com.akademi.finsight.ai.model.service;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface CdsDataService {

    BigDecimal getCdsSpreadForDate(LocalDate targetDate);
}
