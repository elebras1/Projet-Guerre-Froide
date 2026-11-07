package com.populaire.projetguerrefroide.loader;


public record ProductionType(
    int workforce,
    int[] workerPopTypeIndexes,
    long[] workerPopTypeIds,
    float[] workerPopTypeRatios,
    float[] workerPopTypeEffectMultipliers) {
}
