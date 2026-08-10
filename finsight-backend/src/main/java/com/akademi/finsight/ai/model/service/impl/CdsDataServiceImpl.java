package com.akademi.finsight.ai.model.service.impl;

import com.akademi.finsight.ai.model.constant.MarketConstants;
import com.akademi.finsight.ai.model.service.CdsDataService;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.NavigableMap;
import java.util.Objects;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.regex.Pattern;

@Slf4j
@Service
public class CdsDataServiceImpl implements CdsDataService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private static final String CSV_FILE_PATH = "data/TRGV5YUSAC=R.csv";
    private static final Pattern CSV_DELIMITER_OUTSIDE_QUOTES =
            Pattern.compile(",(?=(?:[^\"]*+\"[^\"]*+\")*+[^\"]*+$)");
    private final NavigableMap<LocalDate, BigDecimal> cdsSpreadMap = new ConcurrentSkipListMap<>();

    @PostConstruct
    public void init() {
        loadCdsData();
    }

    private void loadCdsData() {
        try {
            ClassPathResource resource = new ClassPathResource(CSV_FILE_PATH);

            if (!resource.exists()) {
                log.warn("CDS CSV file not found at {}, using default CDS value: {}", CSV_FILE_PATH, MarketConstants.DEFAULT_CDS);
                return;
            }

            log.info("Loading CDS data from file: {}", CSV_FILE_PATH);

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                boolean isHeader = true;

                while ((line = reader.readLine()) != null) {
                    line = line.trim();
                    if (line.isEmpty()) {
                        continue;
                    }

                    if (isHeader) {
                        isHeader = false;
                        continue;
                    }

                    parseAndStoreLine(line);
                }
            }
            log.info("Successfully loaded {} CDS data points from CSV.", cdsSpreadMap.size());
        } catch (Exception e) {
            log.error("Failed to load CDS CSV data, falling back to default values", e);
        }
    }

    private void parseAndStoreLine(String line) {
        try {
            //sadece çift tırnakların dışında kalan virgüllerden böl
            String[] tokens = line.split(CSV_DELIMITER_OUTSIDE_QUOTES.pattern());
            if (tokens.length < 2) {
                return;
            }
            String dateStr = cleanToken(tokens[0]);
            String priceStr = cleanToken(tokens[1]);

            LocalDate date = LocalDate.parse(dateStr, DATE_FORMATTER);
            BigDecimal spreadBps = new BigDecimal(priceStr.replace(',', '.'));
            cdsSpreadMap.put(date, spreadBps);

        } catch (Exception e) {
            log.debug("Skipping unparseable CDS CSV line: '{}'", line);
        }
    }

    private String cleanToken(String token) {
        return token.replace("\"", "").trim();
    }

    @Override
    public BigDecimal getCdsSpreadForDate(LocalDate targetDate) {
        if (Objects.nonNull(targetDate)) {
            BigDecimal spread = cdsSpreadMap.get(targetDate);
            if (Objects.nonNull(spread)) {
                log.info("Found CDS spread for date {}: {} bps", targetDate, spread);
                return spread;
            }
            log.warn("No CDS spread found in CSV for date {}, falling back to default: {}", targetDate, MarketConstants.DEFAULT_CDS);
        }
        return MarketConstants.DEFAULT_CDS;
    }
}
