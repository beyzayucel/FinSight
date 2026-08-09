package com.akademi.finsight.fund.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "fund")
public class FundProperties {

    private String code;

    private List<String> periods;

    private final Recommendation recommendation = new Recommendation();
    @Getter
    @Setter
    public static class Recommendation {
        private long ttlHours = 4;
    }

    private final Sync sync = new Sync();
    @Getter
    @Setter
    public static class Sync {
        private int dataLagDays;
    }

    private final Cache cache = new Cache();
    @Getter
    @Setter
    public static class Cache {
        private final PerformanceComparison performanceComparison = new PerformanceComparison();

        @Getter
        @Setter
        public static class PerformanceComparison {
            private int maxSize;
            private int expireHours;
        }
    }
}
