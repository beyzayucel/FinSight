-- Karar Geçmişi ekranı için — hesaplama frontend'deki simülasyon motorunda yapılır,
-- backend sadece sonucu saklar (nullable: karar anında henüz bilinmeyebilir, ayrı
-- bir "metrik/stres testi ekle" akışıyla sonradan doldurulur).
ALTER TABLE manual_scenario
    ADD total_return_pct     DECIMAL(6, 2) NULL,
        benchmark_diff_pct   DECIMAL(6, 2) NULL,
        max_drawdown_pct     DECIMAL(6, 2) NULL,
        daily_volatility_pct DECIMAL(6, 2) NULL,
        analysis_window_days INT           NULL,
        stress_test_result_id UNIQUEIDENTIFIER NULL,
        CONSTRAINT fk_manual_scenario_stress_test_result
            FOREIGN KEY (stress_test_result_id) REFERENCES stress_test_results (id);
