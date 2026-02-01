package com.populaire.projetguerrefroide.dto;

public record BuildingDto(long buildingId, String buildingTypeNameId, String parentNameId, int maxLevel, String[] goodCostNameIds, float[] goodCostValues, String[] inputGoodNameIds, float[] inputGoodValues, String outputGoodNameId, float outputGoodValue) {
}
