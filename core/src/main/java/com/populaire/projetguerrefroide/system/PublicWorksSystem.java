package com.populaire.projetguerrefroide.system;

import com.github.tommyettinger.ds.LongFloatMap;
import com.github.tommyettinger.ds.LongObjectMap;
import io.github.elebras1.flecs.EntityView;
import io.github.elebras1.flecs.Field;
import io.github.elebras1.flecs.Iter;
import io.github.elebras1.flecs.Query;
import io.github.elebras1.flecs.World;
import com.populaire.projetguerrefroide.component.*;
import com.populaire.projetguerrefroide.util.InfrastructureUtils;

import static com.populaire.projetguerrefroide.util.Constants.DAYS_PER_YEAR;
import static com.populaire.projetguerrefroide.util.EconomyBuildingUtils.infrastructureBonus;

public class PublicWorksSystem {

    private final Query developmentQuery;
    private final Query factoryQuery;

    public PublicWorksSystem(World ecsWorld, long phaseId) {
        this.developmentQuery = ecsWorld.query()
            .with(Building.class)
            .with(DevelopmentBuildingType.class)
            .build();
        this.factoryQuery = ecsWorld.query()
            .with(Building.class)
            .with(EconomyBuilding.class)
            .build();
        ecsWorld.system("PublicWorksSystem")
            .kind(phaseId)
            .with(CountryPublicWorks.class)
            .iter(this::target);
    }

    private void target(Iter iter) {
        World world = iter.world();
        Field<CountryPublicWorks> countryPublicWorksField = iter.field(CountryPublicWorks.class, 0);
        LongFloatMap factoryValues = this.factoryValues();

        LongFloatMap bestReturns = new LongFloatMap();
        for (int i = 0; i < iter.count(); i++) {
            CountryPublicWorksView countryPublicWorks = countryPublicWorksField.getMutView(i);
            if (countryPublicWorks.targetBuildingId() == 0) {
                bestReturns.put(iter.entity(i), 0f);
            }
        }

        if (!bestReturns.isEmpty()) {
            LongObjectMap<long[]> bestCandidates = new LongObjectMap<>();
            this.developmentQuery.iter(developmentIter -> {
                DevelopmentBuildingTypeView buildingType = developmentIter.field(DevelopmentBuildingType.class, 1).getView(0);
                long buildingTypeId = developmentIter.src(1);
                float productionModifier = InfrastructureUtils.productionModifier(world, buildingTypeId);
                if (productionModifier <= 0f) {
                    return;
                }
                Field<Building> buildingField = developmentIter.field(Building.class, 0);
                for (int j = 0; j < developmentIter.count(); j++) {
                    BuildingView building = buildingField.getMutView(j);
                    long countryId = building.countryId();
                    if (building.size() >= buildingType.maxLevel() || !bestReturns.containsKey(countryId)) {
                        continue;
                    }
                    float annualReturn = annualReturn(world, factoryValues, building, buildingType, productionModifier);
                    if (annualReturn > bestReturns.get(countryId)) {
                        bestReturns.put(countryId, annualReturn);
                        bestCandidates.put(countryId, new long[] {developmentIter.entity(j), buildingTypeId});
                    }
                }
            });

            for (int i = 0; i < iter.count(); i++) {
                CountryPublicWorksMutView countryPublicWorks = countryPublicWorksField.getMutView(i);
                if (countryPublicWorks.targetBuildingId() != 0) {
                    continue;
                }
                long[] candidate = bestCandidates.get(iter.entity(i));
                if (candidate != null) {
                    countryPublicWorks.targetBuildingId(candidate[0]);
                    countryPublicWorks.targetBuildingTypeId(candidate[1]);
                }
            }
        }

        for (int i = 0; i < iter.count(); i++) {
            CountryPublicWorksMutView countryPublicWorks = countryPublicWorksField.getMutView(i);
            float annualReturn = 0f;
            if (countryPublicWorks.targetBuildingId() != 0) {
                long buildingTypeId = countryPublicWorks.targetBuildingTypeId();
                BuildingView building = world.obtainEntityView(countryPublicWorks.targetBuildingId()).getView(Building.class);
                DevelopmentBuildingTypeView buildingType = world.obtainEntityView(buildingTypeId).getView(DevelopmentBuildingType.class);
                annualReturn = annualReturn(world, factoryValues, building, buildingType, InfrastructureUtils.productionModifier(world, buildingTypeId));
            }
            countryPublicWorks.targetReturn(annualReturn);
        }
    }

    private LongFloatMap factoryValues() {
        LongFloatMap factoryValues = new LongFloatMap();
        this.factoryQuery.iter(factoryIter -> {
            Field<Building> buildingField = factoryIter.field(Building.class, 0);
            Field<EconomyBuilding> economyBuildingField = factoryIter.field(EconomyBuilding.class, 1);
            for (int j = 0; j < factoryIter.count(); j++) {
                BuildingView building = buildingField.getMutView(j);
                EconomyBuildingView economyBuilding = economyBuildingField.getMutView(j);
                float value = economyBuilding.production() * economyBuilding.unitRevenue();
                if (value > 0f) {
                    factoryValues.put(building.parentId(), factoryValues.getOrDefault(building.parentId(), 0f) + value);
                }
            }
        });
        return factoryValues;
    }

    private static float annualReturn(World world, LongFloatMap factoryValues, BuildingView building, DevelopmentBuildingTypeView buildingType, float productionModifier) {
        CountryMarketView countryMarket = world.obtainEntityView(building.countryId()).getView(CountryMarket.class);
        double levelCost = 0.0;
        for (int k = 0; k < buildingType.goodCostIndexesLength(); k++) {
            int goodIndex = buildingType.goodCostIndexes(k);
            if (goodIndex < 0) {
                break;
            }
            levelCost += (double) buildingType.goodCostAmounts(k) * countryMarket.goodBudgetPrices(goodIndex);
        }
        if (levelCost <= 0.0) {
            return 0f;
        }

        EntityView province = world.obtainEntityView(building.parentId());
        double baseValue = 0.0;
        if (province.has(ResourceGathering.class) && province.has(ProvinceInfrastructure.class)) {
            ResourceGatheringView resourceGathering = province.getView(ResourceGathering.class);
            ProvinceInfrastructureView provinceInfrastructure = province.getView(ProvinceInfrastructure.class);
            baseValue += resourceGathering.production() * countryMarket.goodPrices(resourceGathering.goodIndex()) / (1f + provinceInfrastructure.productionBonus());
        }
        ProvinceView provinceData = province.getView(Province.class);
        long regionInstanceId = provinceData.regionInstanceId();
        if (regionInstanceId != 0) {
            RegionInstanceView regionInstance = world.obtainEntityView(regionInstanceId).getView(RegionInstance.class);
            if (regionInstance.provinceCount() > 0) {
                baseValue += factoryValues.getOrDefault(regionInstanceId, 0f) / (1f + infrastructureBonus(regionInstance)) / regionInstance.provinceCount();
            }
        }
        return (float) (productionModifier * baseValue * DAYS_PER_YEAR / levelCost);
    }
}
