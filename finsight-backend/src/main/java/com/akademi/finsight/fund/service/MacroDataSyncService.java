package com.akademi.finsight.fund.service;

import com.akademi.finsight.fund.entity.MacroData;

import java.time.LocalDate;

public interface MacroDataSyncService {
    MacroData sync(LocalDate date);
}
