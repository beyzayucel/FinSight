package com.akademi.finsight.ai.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * q_values olasılık değildir; model en yüksek q_value'ya sahip action'ı seçer.
 */
public record Decision(
        @JsonProperty("action") Integer action,
        @JsonProperty("action_name") String actionName,
        @JsonProperty("action_label_tr") String actionLabelTr,
        @JsonProperty("q_values") List<Double> qValues
) {
}