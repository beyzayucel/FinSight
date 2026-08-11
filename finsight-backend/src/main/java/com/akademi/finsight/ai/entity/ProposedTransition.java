package com.akademi.finsight.ai.entity;

import jakarta.persistence.*;
import java.util.List;

@Embeddable
public class ProposedTransition {

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "stockRatio", column = @Column(name = "before_stock_ratio")),
            @AttributeOverride(name = "repoRatio", column = @Column(name = "before_repo_ratio")),
            @AttributeOverride(name = "collateralRatio", column = @Column(name = "before_collateral_ratio")),
            @AttributeOverride(name = "fundRatio", column = @Column(name = "before_fund_ratio"))
    })
    private Weights weightsBefore;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "stockRatio", column = @Column(name = "after_stock_ratio")),
            @AttributeOverride(name = "repoRatio", column = @Column(name = "after_repo_ratio")),
            @AttributeOverride(name = "collateralRatio", column = @Column(name = "after_collateral_ratio")),
            @AttributeOverride(name = "fundRatio", column = @Column(name = "after_fund_ratio"))
    })
    private Weights weightsAfter;

    @Column(name = "weights_sum_ratio")
    private Double weightsSumRatio;

    @Column(name = "clipped")
    private Boolean clipped;

    @Column(name = "trade_amount_ratio")
    private Double tradeAmountRatio;

    @ElementCollection
    @CollectionTable(
            name = "proposed_transition_trade_legs",
            joinColumns = @JoinColumn(name = "decision_id")
    )
    @OrderColumn(name = "position")
    private List<TradeLeg> tradeLegs;

    // Nullable in the sample payload; kept as generic collections of
    // doubles in case they get populated (e.g. per-stock breakdown ratios).
    @ElementCollection
    @CollectionTable(
            name = "proposed_transition_stock_breakdown_before",
            joinColumns = @JoinColumn(name = "decision_id")
    )
    @Column(name = "value")
    @OrderColumn(name = "position")
    private List<Double> stockBreakdownBeforeRatios;

    @ElementCollection
    @CollectionTable(
            name = "proposed_transition_stock_breakdown_after",
            joinColumns = @JoinColumn(name = "decision_id")
    )
    @Column(name = "value")
    @OrderColumn(name = "position")
    private List<Double> stockBreakdownAfterRatios;

    public ProposedTransition() {
    }

    // Getters and setters

    public Weights getWeightsBefore() {
        return weightsBefore;
    }

    public void setWeightsBefore(Weights weightsBefore) {
        this.weightsBefore = weightsBefore;
    }

    public Weights getWeightsAfter() {
        return weightsAfter;
    }

    public void setWeightsAfter(Weights weightsAfter) {
        this.weightsAfter = weightsAfter;
    }

    public Double getWeightsSumRatio() {
        return weightsSumRatio;
    }

    public void setWeightsSumRatio(Double weightsSumRatio) {
        this.weightsSumRatio = weightsSumRatio;
    }

    public Boolean getClipped() {
        return clipped;
    }

    public void setClipped(Boolean clipped) {
        this.clipped = clipped;
    }

    public Double getTradeAmountRatio() {
        return tradeAmountRatio;
    }

    public void setTradeAmountRatio(Double tradeAmountRatio) {
        this.tradeAmountRatio = tradeAmountRatio;
    }

    public List<TradeLeg> getTradeLegs() {
        return tradeLegs;
    }

    public void setTradeLegs(List<TradeLeg> tradeLegs) {
        this.tradeLegs = tradeLegs;
    }

    public List<Double> getStockBreakdownBeforeRatios() {
        return stockBreakdownBeforeRatios;
    }

    public void setStockBreakdownBeforeRatios(List<Double> stockBreakdownBeforeRatios) {
        this.stockBreakdownBeforeRatios = stockBreakdownBeforeRatios;
    }

    public List<Double> getStockBreakdownAfterRatios() {
        return stockBreakdownAfterRatios;
    }

    public void setStockBreakdownAfterRatios(List<Double> stockBreakdownAfterRatios) {
        this.stockBreakdownAfterRatios = stockBreakdownAfterRatios;
    }
}
