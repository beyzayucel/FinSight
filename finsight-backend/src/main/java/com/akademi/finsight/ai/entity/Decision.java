package com.akademi.finsight.ai.entity;

import jakarta.persistence.*;
import java.util.List;

@Embeddable
public class Decision {

    @Column(name = "action")
    private Integer action;

    @Column(name = "action_name", length = 64)
    private String actionName;

    @Column(name = "action_label_tr", length = 512)
    private String actionLabelTr;

    @ElementCollection
    @CollectionTable(
            name = "decision_q_values",
            joinColumns = @JoinColumn(name = "decision_id")
    )
    @Column(name = "value")
    @OrderColumn(name = "position")
    private List<Double> qValues;

    public Decision() {
    }

    // Getters and setters

    public Integer getAction() {
        return action;
    }

    public void setAction(Integer action) {
        this.action = action;
    }

    public String getActionName() {
        return actionName;
    }

    public void setActionName(String actionName) {
        this.actionName = actionName;
    }

    public String getActionLabelTr() {
        return actionLabelTr;
    }

    public void setActionLabelTr(String actionLabelTr) {
        this.actionLabelTr = actionLabelTr;
    }

    public List<Double> getQValues() {
        return qValues;
    }

    public void setQValues(List<Double> qValues) {
        this.qValues = qValues;
    }
}
