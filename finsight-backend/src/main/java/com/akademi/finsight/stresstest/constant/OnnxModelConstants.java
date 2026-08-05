package com.akademi.finsight.stresstest.constant;

import java.util.Map;

public class OnnxModelConstants {
    private OnnxModelConstants (){}
    public static final String FAIZ_STRESS = "FAIZ_STRESS";
    public static final String HISSE_STRESS = "HISSE_STRESS";

    public static final Map<String,String> MODEL_PATHS = Map.of(
            FAIZ_STRESS, "model/faiz_stress_model.onnx",
            HISSE_STRESS, "model/hisse_stress_model.onnx"
    );
}
