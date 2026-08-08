ALTER TABLE manual_scenario
    ADD simulated_portfolio_value DECIMAL(18, 2) NULL;

ALTER TABLE ai_recommendation
    ADD simulated_portfolio_value DECIMAL(18, 2) NULL;
