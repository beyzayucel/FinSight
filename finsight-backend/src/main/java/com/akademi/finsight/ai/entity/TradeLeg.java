package com.akademi.finsight.ai.entity;

import jakarta.persistence.Embeddable;

@Embeddable
public class TradeLeg {

    private String source;
    private String target;
    private Double amountRatio;

    public TradeLeg() {
    }

    public TradeLeg(String source, String target, Double amountRatio) {
        this.source = source;
        this.target = target;
        this.amountRatio = amountRatio;
    }

    // Getters and setters

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public Double getAmountRatio() {
        return amountRatio;
    }

    public void setAmountRatio(Double amountRatio) {
        this.amountRatio = amountRatio;
    }
}
