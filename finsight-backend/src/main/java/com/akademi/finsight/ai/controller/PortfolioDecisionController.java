package com.akademi.finsight.ai.controller;

import com.akademi.finsight.ai.dto.response.DecisionResponse;
import com.akademi.finsight.ai.dto.request.PortfolioDecisionApiRequest;
import com.akademi.finsight.ai.dto.PortfolioWeights;
import com.akademi.finsight.ai.service.PortfolioDecisionService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/portfolio-decisions")
public class PortfolioDecisionController {

    private final PortfolioDecisionService decisionService;

    public PortfolioDecisionController(PortfolioDecisionService decisionService) {
        this.decisionService = decisionService;
    }

    /**
     * Güncel piyasa ve fon verilerini alarak AI kararını hesaplar,
     * yeni portföy ağırlıklarını veritabanına kaydeder.
     */
    @PostMapping("/decide")
    public ResponseEntity<DecisionResponse> makeDecision(
            @Valid @RequestBody PortfolioDecisionApiRequest request) {

        DecisionResponse response = decisionService.decideAndPersist(
                request.market(),
                request.fund()
        );

        return ResponseEntity.ok(response);
    }

    /** İlk kez portföy başlangıç ağırlığı kaydı oluşturur. */
    @PostMapping("/initialize")
    public ResponseEntity<Void> initializeState(
            @Valid @RequestBody PortfolioWeights initialWeights) {
        decisionService.initializeState(initialWeights);
        return ResponseEntity.ok().build();
    }
}