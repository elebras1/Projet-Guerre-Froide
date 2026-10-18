package com.populaire.projetguerrefroide.util;

import com.populaire.projetguerrefroide.component.EconomyBuilding;
import com.populaire.projetguerrefroide.component.EconomyBuildingTypeView;

import static com.populaire.projetguerrefroide.util.Constants.MAX_GOODS;

public class EconomyBuildingUtils {

    public static EconomyBuilding newEconomyBuilding(EconomyBuildingTypeView buildingType, boolean advancedRecipe, long ownerTagId) {
        int[] activeInputGoodIndexes = new int[MAX_GOODS];
        float[] activeInputAmounts = new float[MAX_GOODS];
        for (int g = 0; g < MAX_GOODS; g++) {
            activeInputGoodIndexes[g] = advancedRecipe ? buildingType.advancedGoodInputIndexes(g) : buildingType.goodInputIndexes(g);
            activeInputAmounts[g] = advancedRecipe ? buildingType.advancedGoodInputAmounts(g) : buildingType.goodInputAmounts(g);
        }
        return new EconomyBuilding(ownerTagId, 0f, 1f, 0f, 0d, 1f, 0, 0, 0f, 0f, new float[MAX_GOODS], activeInputGoodIndexes, activeInputAmounts, advancedRecipe);
    }
}
