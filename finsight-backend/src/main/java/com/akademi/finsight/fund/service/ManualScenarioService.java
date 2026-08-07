package com.akademi.finsight.fund.service;

import com.akademi.finsight.fund.dto.request.ManualScenarioRequest;
import com.akademi.finsight.fund.dto.response.ManualScenarioResponse;

import java.util.List;
import java.util.UUID;

public interface ManualScenarioService {

    void applyManualScenario(String email, ManualScenarioRequest request);

    List<ManualScenarioResponse> getScenarioHistory(String email, UUID fundId);
}
