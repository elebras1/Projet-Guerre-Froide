package com.populaire.projetguerrefroide.dto;

public record BuildingSummaryDto(
    long id,
    String nameId,
    int size,
    int maxLevel,
    int amountWorkers,
    float productionAmount,
    int levelsQueued,
    boolean isSuspended) {

}
