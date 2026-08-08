package com.akademi.finsight.fund.config;

import com.akademi.finsight.fund.repository.MacroDataRepository;
import com.akademi.finsight.fund.service.MacroDataSyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Slf4j
@Component
@RequiredArgsConstructor
public class MacroDataInitializer implements CommandLineRunner {

    private final MacroDataSyncService macroDataSyncService;
    private final MacroDataRepository macroDataRepository;

    @Override
    public void run(String... args) {
        long count = macroDataRepository.count();
        if (count == 0) {
            log.info("MacroData database table is empty. Pre-populating historical macro data for demo (2026-07-01 to 2026-08-08)...");
            
            LocalDate startDate = LocalDate.of(2026, 7, 1);
            LocalDate endDate = LocalDate.of(2026, 8, 8);

            LocalDate current = startDate;
            int successCount = 0;
            
            while (!current.isAfter(endDate)) {
                try {
                    macroDataSyncService.sync(current);
                    successCount++;
                } catch (Exception e) {
                    log.warn("Failed to sync macro data for date: {}", current, e);
                }
                current = current.plusDays(1);
            }
            
            log.info("Database pre-population complete. Successfully synced {} days of macro data.", successCount);
        } else {
            log.info("MacroData table already contains {} records. Skipping database pre-population.", count);
        }
    }
}
