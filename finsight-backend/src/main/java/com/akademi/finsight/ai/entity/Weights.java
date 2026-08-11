package com.akademi.finsight.ai.entity;

import jakarta.persistence.Embeddable;

@Embeddable
public class Weights {

    private Double stockRatio;
    private Double repoRatio;
    private Double collateralRatio;
    private Double fundRatio;

    public Weights() {
    }

    public Weights(Double stockRatio, Double repoRatio, Double collateralRatio, Double fundRatio) {
        this.stockRatio = stockRatio;
        this.repoRatio = repoRatio;
        this.collateralRatio = collateralRatio;
        this.fundRatio = fundRatio;
    }

    // Getters and setters

    public Double getStockRatio() {
        return stockRatio;
    }

    public void setStockRatio(Double stockRatio) {
        this.stockRatio = stockRatio;
    }

    public Double getRepoRatio() {
        return repoRatio;
    }

    public void setRepoRatio(Double repoRatio) {
        this.repoRatio = repoRatio;
    }

    public Double getCollateralRatio() {
        return collateralRatio;
    }

    public void setCollateralRatio(Double collateralRatio) {
        this.collateralRatio = collateralRatio;
    }

    public Double getFundRatio() {
        return fundRatio;
    }

    public void setFundRatio(Double fundRatio) {
        this.fundRatio = fundRatio;
    }
}