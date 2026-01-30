package com.populaire.projetguerrefroide.dto;

public record BuildingDto(long buildingId, String buildingTypeNameId, String parentNameId, int maxLevel, long[] goodCostIds, float[] goodCostValues, long[] inputGoodIds, float[] inputGoodValues, long outputGoodId, float outputGoodValue) {
}
