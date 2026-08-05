package com.akademi.finsight.fund.service;

import com.akademi.finsight.fund.dto.request.ManualScenarioRequest;

public interface ManualScenarioService {

    void applyManualScenario(String email, ManualScenarioRequest request);
}
