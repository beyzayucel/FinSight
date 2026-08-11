package com.akademi.finsight.ai.entity;

import jakarta.persistence.*;
import java.util.List;

/**
 * Root entity representing a portfolio rebalancing decision snapshot.
 * Table/column names can be adjusted to match your schema conventions.
 */
@Entity
@Table(name = "portfolio_decision")
public class PortfolioDecision {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "contract_version", nullable = false, length = 32)
    private String contractVersion;

    @ElementCollection
    @CollectionTable(
            name = "portfolio_decision_state",
            joinColumns = @JoinColumn(name = "decision_id")
    )
    @Column(name = "value")
    @OrderColumn(name = "position")
    private List<Double> state;

    @Embedded
    private Decision decision;

    @Embedded
    private ProposedTransition proposedTransition;

    @ElementCollection
    @CollectionTable(
            name = "portfolio_decision_warnings",
            joinColumns = @JoinColumn(name = "decision_id")
    )
    @Column(name = "warning")
    @OrderColumn(name = "position")
    private List<String> warnings;

    public PortfolioDecision() {
    }

    // Getters and setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getContractVersion() {
        return contractVersion;
    }

    public void setContractVersion(String contractVersion) {
        this.contractVersion = contractVersion;
    }

    public List<Double> getState() {
        return state;
    }

    public void setState(List<Double> state) {
        this.state = state;
    }

    public Decision getDecision() {
        return decision;
    }

    public void setDecision(Decision decision) {
        this.decision = decision;
    }

    public ProposedTransition getProposedTransition() {
        return proposedTransition;
    }

    public void setProposedTransition(ProposedTransition proposedTransition) {
        this.proposedTransition = proposedTransition;
    }

    public List<String> getWarnings() {
        return warnings;
    }

    public void setWarnings(List<String> warnings) {
        this.warnings = warnings;
    }
}