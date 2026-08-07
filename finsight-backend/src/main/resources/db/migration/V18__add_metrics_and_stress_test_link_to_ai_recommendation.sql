-- Karar Geçmişi ekranı için — manual_scenario'daki (V17) aynı desen, AI önerileri için.
ALTER TABLE ai_recommendation
    ADD total_return_pct     DECIMAL(6, 2) NULL,
        benchmark_diff_pct   DECIMAL(6, 2) NULL,
        max_drawdown_pct     DECIMAL(6, 2) NULL,
        daily_volatility_pct DECIMAL(6, 2) NULL,
        analysis_window_days INT           NULL,
        stress_test_result_id UNIQUEIDENTIFIER NULL,
        CONSTRAINT fk_ai_recommendation_stress_test_result
            FOREIGN KEY (stress_test_result_id) REFERENCES stress_test_results (id);
