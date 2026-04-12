package com.populaire.projetguerrefroide.component;

import com.github.elebras1.flecs.annotation.Component;

@Component
public record ResourceGatheringType(
    int workforce,
    long ownerId,
    int workerPopTypeIndex,
    long workerPopTypeId,
    float workerPopTypeRatio,
    float workerEffectMultiplier,
    int slavePopTypeIndex,
    long slavePopTypeId,
    float slavePopTypeRatio,
    float slaveEffectMultiplier) {

}
