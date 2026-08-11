package com.akademi.finsight.ai.service;

import com.akademi.finsight.ai.client.PortfolioDecisionClient;
import com.akademi.finsight.ai.dto.*;
import com.akademi.finsight.ai.dto.request.DecisionRequest;
import com.akademi.finsight.ai.dto.response.DecisionResponse;
import com.akademi.finsight.ai.dto.response.WeightsResponse;
import com.akademi.finsight.ai.entity.PortfolioStateEntity;
import com.akademi.finsight.ai.repository.PortfolioStateRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class PortfolioDecisionService {

    private final PortfolioDecisionClient client;
    private final PortfolioStateRepository repository;

    public PortfolioDecisionService(PortfolioDecisionClient client,
                                    PortfolioStateRepository repository) {
        this.client = client;
        this.repository = repository;
    }

    /**
     * Sistemde tek bir aktif portföy durumu olduğu varsayılır (id gerekmez).
     * En güncel kayıt bulunur, karar alınır, weights_after ile güncellenir.
     */
    @Transactional
    public DecisionResponse decideAndPersist(MarketInput market, FundInput fund) {
        PortfolioStateEntity state = repository.findFirstByOrderByUpdatedAtDesc()
                .orElseThrow(() -> new IllegalStateException(
                        "Kayıtlı portföy ağırlığı yok; önce initializeState() ile ilk kayıt oluşturulmalı"));

        PortfolioWeights currentWeights = new PortfolioWeights(
                state.getStockRatio(),
                state.getRepoRatio(),
                state.getCollateralRatio(),
                state.getFundRatio());

        DecisionRequest request = new DecisionRequest(
                market, fund, PortfolioInput.ofWeightsOnly(currentWeights));

        DecisionResponse response = client.decide(request);

        // AI servisinden dönen aksiyon adını/id'sini alıyoruz
        String actionName = response.decision() != null ? String.valueOf(response.decision().action()) : null;

        // weights_after kontrolü: Eğer Python tarafından yeni ağırlıklar gelmişse onları kullan,
        // gelmediyse veritabanındaki mevcut (current) ağırlıkları koru!
        WeightsResponse afterResponse = (response.proposedTransition() != null) ? response.proposedTransition().weightsAfter() : null;

        BigDecimal newStock = (afterResponse != null && afterResponse.stockRatio() != null) ? afterResponse.stockRatio() : state.getStockRatio();
        BigDecimal newRepo = (afterResponse != null && afterResponse.repoRatio() != null) ? afterResponse.repoRatio() : state.getRepoRatio();
        BigDecimal newCollateral = (afterResponse != null && afterResponse.collateralRatio() != null) ? afterResponse.collateralRatio() : state.getCollateralRatio();
        BigDecimal newFund = (afterResponse != null && afterResponse.fundRatio() != null) ? afterResponse.fundRatio() : state.getFundRatio();

        state.applyWeightsAfter(newStock, newRepo, newCollateral, newFund, actionName);
        repository.save(state);

        return response;
    }

    /** İlk kez portföy başlangıç kaydı oluşturur. DB otomatik id üretir. */
    @Transactional
    public PortfolioStateEntity initializeState(PortfolioWeights initialWeights) {
        PortfolioStateEntity entity = new PortfolioStateEntity(
                initialWeights.stockRatio(),
                initialWeights.repoRatio(),
                initialWeights.collateralRatio(),
                initialWeights.fundRatio());
        return repository.save(entity);
    }
}