package com.akademi.finsight.stresstest.constant;

import java.util.Map;

public class OnnxModelConstants {
    private OnnxModelConstants (){}
    public static final String INTEREST_STRESS = "INTEREST_STRESS";
    public static final String SHARE_STRESS = "HISSE_STRESS";

    public static final Map<String,String> MODEL_PATHS = Map.of(
            INTEREST_STRESS, "model/faiz_stress_model.onnx",
            SHARE_STRESS, "model/hisse_stress_model.onnx"
    );
}
