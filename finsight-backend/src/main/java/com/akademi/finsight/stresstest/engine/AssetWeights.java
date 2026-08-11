package com.akademi.finsight.stresstest.engine;

public record AssetWeights(
        float equity,
        float bond,
        float fx,
        float cash
) {}