-- Metriklerin hesaplandığı veri tarihi (T-8). Simülasyon penceresi bugüne değil,
-- fund_period_metric.data_date'e dayanır (bkz. FUND_SYNC_DATA_LAG_DAYS); Karar Geçmişi
-- ekranı "Analiz dönemi"ni bu tarihten türetir. Nullable: bu kolondan önce alınmış
-- kararlarda ve metrik hesabı başarısız olduğunda boş kalır.
ALTER TABLE manual_scenario
    ADD data_date DATE NULL;

ALTER TABLE ai_recommendation
    ADD data_date DATE NULL;
