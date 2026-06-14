package com.populaire.projetguerrefroide.pojo;


public record ProductionType(
    int workforce,
    int[] workerPopTypeIndexes,
    long[] workerPopTypeIds,
    float[] workerPopTypeRatios,
    float[] workerPopTypeEffectMultipliers) {
}
