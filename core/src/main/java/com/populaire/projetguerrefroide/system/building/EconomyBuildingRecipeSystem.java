package com.populaire.projetguerrefroide.system.building;

import com.populaire.projetguerrefroide.component.*;
import com.populaire.projetguerrefroide.simulation.EcsConstants;
import com.populaire.projetguerrefroide.system.population.PopulationMobilityUtils;
import io.github.elebras1.flecs.EntityView;
import io.github.elebras1.flecs.Field;
import io.github.elebras1.flecs.Iter;
import io.github.elebras1.flecs.World;

import java.util.Arrays;

import static com.populaire.projetguerrefroide.simulation.Constants.MAX_GOODS;
import static com.populaire.projetguerrefroide.simulation.Constants.RECIPE_SWITCH_MARGIN;
import static com.populaire.projetguerrefroide.system.building.EconomyBuildingUtils.recipeCost;

/**
 * Once a month, the owner of a factory whose country has the technology of its advanced recipe takes the recipe whose
 * inputs cost less at the country's prices, when the gap exceeds RECIPE_SWITCH_MARGIN (decision T13).
 */
public class EconomyBuildingRecipeSystem {
    private final EcsConstants ecsConstants;
    private final int[] newGoodIndexes = new int[MAX_GOODS];
    private final float[] newAmounts = new float[MAX_GOODS];
    private final float[] newStocks = new float[MAX_GOODS];

    public EconomyBuildingRecipeSystem(World ecsWorld, EcsConstants ecsConstants, long phaseId) {
        this.ecsConstants = ecsConstants;
        ecsWorld.system("EconomyBuildingRecipeSystem")
            .kind(phaseId)
            .with(Building.class)
            .with(EconomyBuilding.class)
            .with(EconomyBuildingType.class)
            .iter(this::choose);
    }

    private void choose(Iter iter) {
        World world = iter.stage();
        if (iter.count() == 0 || !PopulationMobilityUtils.isMobilityDay(world, this.ecsConstants)) {
            return;
        }
        EconomyBuildingTypeView buildingType = iter.field(EconomyBuildingType.class, 2).getView(0);
        if (buildingType.advancedGoodInputIndexes(0) < 0) {
            return;
        }
        int typeIndex = buildingType.typeIndex();
        Field<Building> buildingField = iter.field(Building.class, 0);
        Field<EconomyBuilding> economyBuildingField = iter.field(EconomyBuilding.class, 1);

        long countryId = 0;
        boolean available = false;
        boolean preferAdvanced = false;
        boolean preferBasic = false;
        CountryMarketMutView countryMarket = null;
        for (int i = 0; i < iter.count(); i++) {
            BuildingView building = buildingField.getMutView(i);
            if (building.countryId() != countryId) {
                countryId = building.countryId();
                EntityView country = world.obtainEntityView(countryId);
                available = country.<CountryProductionTechnologyView>getView(CountryProductionTechnology.class).advancedRecipes(typeIndex);
                countryMarket = country.getMutView(CountryMarket.class);
                double basicCost = recipeCost(buildingType, false, countryMarket);
                double advancedCost = recipeCost(buildingType, true, countryMarket);
                preferAdvanced = advancedCost < basicCost * (1.0 - RECIPE_SWITCH_MARGIN);
                preferBasic = basicCost < advancedCost * (1.0 - RECIPE_SWITCH_MARGIN);
            }
            if (!available) {
                continue;
            }
            EconomyBuildingMutView economyBuilding = economyBuildingField.getMutView(i);
            if (economyBuilding.advancedRecipe() ? preferBasic : preferAdvanced) {
                this.switchRecipe(economyBuilding, buildingType, !economyBuilding.advancedRecipe(), countryMarket);
            }
        }
    }

    private void switchRecipe(EconomyBuildingMutView economyBuilding, EconomyBuildingTypeView buildingType, boolean advanced, CountryMarketMutView countryMarket) {
        Arrays.fill(this.newGoodIndexes, -1);
        Arrays.fill(this.newAmounts, 0f);
        Arrays.fill(this.newStocks, 0f);
        for (int g = 0; g < MAX_GOODS; g++) {
            this.newGoodIndexes[g] = advanced ? buildingType.advancedGoodInputIndexes(g) : buildingType.goodInputIndexes(g);
            this.newAmounts[g] = advanced ? buildingType.advancedGoodInputAmounts(g) : buildingType.goodInputAmounts(g);
        }
        for (int slot = 0; slot < MAX_GOODS; slot++) {
            int goodIndex = economyBuilding.activeInputGoodIndexes(slot);
            if (goodIndex < 0) {
                break;
            }
            float stock = economyBuilding.inputStocks(slot);
            int kept = -1;
            for (int g = 0; g < MAX_GOODS && this.newGoodIndexes[g] >= 0; g++) {
                if (this.newGoodIndexes[g] == goodIndex) {
                    kept = g;
                    break;
                }
            }
            if (kept >= 0) {
                this.newStocks[kept] = stock;
            } else if (stock > 0f) {
                countryMarket.goodStocks(goodIndex, countryMarket.goodStocks(goodIndex) + stock);
                countryMarket.goodReturnedAmounts(goodIndex, countryMarket.goodReturnedAmounts(goodIndex) + stock);
            }
        }
        for (int g = 0; g < MAX_GOODS; g++) {
            economyBuilding.activeInputGoodIndexes(g, this.newGoodIndexes[g]);
            economyBuilding.activeInputAmounts(g, this.newAmounts[g]);
            economyBuilding.inputStocks(g, this.newStocks[g]);
            economyBuilding.goodInputDemandAmounts(g, 0f);
        }
        economyBuilding.advancedRecipe(advanced);
    }
}
