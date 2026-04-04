package com.populaire.projetguerrefroide.pojo;


public record ProductionType(int workforce, long ownerId, long[] workerPopTypeIds, float[] workerPopTypeRatios, float[] workerPopTypeEffectMultipliers) {
}
