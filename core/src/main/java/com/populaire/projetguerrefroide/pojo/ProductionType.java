package com.populaire.projetguerrefroide.pojo;


public record ProductionType(
    int workforce,
    long ownerId,
    int[] workerPopTypeIndexes,
    long[] workerPopTypeIds,
    float[] workerPopTypeRatios,
    float[] workerPopTypeEffectMultipliers) {
}
