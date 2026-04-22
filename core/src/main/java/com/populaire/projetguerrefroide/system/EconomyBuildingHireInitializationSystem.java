package com.populaire.projetguerrefroide.system;

import com.github.elebras1.flecs.EntityView;
import com.github.elebras1.flecs.Field;
import com.github.elebras1.flecs.Iter;
import com.github.elebras1.flecs.World;
import com.github.tommyettinger.ds.IntObjectMap;
import com.github.tommyettinger.ds.LongObjectMap;
import com.populaire.projetguerrefroide.component.*;

import java.util.Arrays;

import static com.populaire.projetguerrefroide.util.Constants.POP_TYPE_COUNT;

public class EconomyBuildingHireInitializationSystem {

    public EconomyBuildingHireInitializationSystem(World ecsWorld, long phaseId) {
        ecsWorld.system("EconomyBuildingHireInitializationSystem")
            .kind(phaseId)
            .with(EconomyBuilding.class)
            .with(Building.class)
            .iter(this::hire);
    }

    private void hire(Iter iter) {
        long buildingTypeId = 0;
        EconomyBuildingTypeView economyBuildingType = null;

        long localMarketId = 0;
        LocalMarketView localMarketData = null;
        DemographicsView demographics = null;

        int primaryWorkerPopTypeIndex = -1;
        int secondaryWorkerPopTypeIndex = -1;

        Field<EconomyBuilding> economyBuildingField = iter.field(EconomyBuilding.class, 0);
        Field<Building> buildingField = iter.field(Building.class, 1);

        LongObjectMap<int[]> workerPopTypeHiredByMarket = new LongObjectMap<>();

        for(int i = 0; i < iter.count(); i++) {
            EconomyBuildingView economyBuilding = economyBuildingField.getMutView(i);
            BuildingView building = buildingField.getMutView(i);

            if(building.typeId() != buildingTypeId) {
                buildingTypeId = building.typeId();
                economyBuildingType = iter.world().obtainEntityView(building.typeId()).getMutView(EconomyBuildingType.class);
                primaryWorkerPopTypeIndex = economyBuildingType.primaryWorkerPopTypeIndex();
                secondaryWorkerPopTypeIndex = economyBuildingType.secondaryWorkerPopTypeIndex();
            }

            if(building.parentId() != localMarketId) {
                localMarketId = building.parentId();
                EntityView localMarket = iter.world().obtainEntityView(building.parentId());
                localMarketData = localMarket.getMutView(LocalMarket.class);
                demographics = localMarket.getMutView(Demographics.class);
            }

            int[] marketWorkerPopTypeHired = workerPopTypeHiredByMarket.computeIfAbsent(building.parentId(), _ -> new int[POP_TYPE_COUNT]);

            int currentPrimaryEmployed = demographics.employmentByPopType(primaryWorkerPopTypeIndex) + marketWorkerPopTypeHired[primaryWorkerPopTypeIndex];
            int currentSecondaryEmployed = demographics.employmentByPopType(secondaryWorkerPopTypeIndex) + marketWorkerPopTypeHired[secondaryWorkerPopTypeIndex];

            int primaryPopTypeWorkerTarget = (int) (economyBuildingType.workforce() * building.size() * economyBuildingType.primaryWorkerPopTypeRatio());
            int secondaryPopTypeWorkerTarget = (int) (economyBuildingType.workforce() * building.size() * economyBuildingType.secondaryWorkerPopTypeRatio());

            int primaryAvailable = Math.max(0, demographics.totalByPopType(primaryWorkerPopTypeIndex) - currentPrimaryEmployed);
            int secondaryAvailable = Math.max(0, demographics.totalByPopType(secondaryWorkerPopTypeIndex) - currentSecondaryEmployed);

            int primaryWorkerAmount = Math.min(primaryPopTypeWorkerTarget, primaryAvailable);
            int secondaryWorkerAmount = Math.min(secondaryPopTypeWorkerTarget, secondaryAvailable);

            economyBuilding.primaryWorkerAmount(primaryWorkerAmount);
            economyBuilding.secondaryWorkerAmount(secondaryWorkerAmount);

            marketWorkerPopTypeHired[primaryWorkerPopTypeIndex] =  marketWorkerPopTypeHired[primaryWorkerPopTypeIndex] + primaryWorkerAmount;
            marketWorkerPopTypeHired[secondaryWorkerPopTypeIndex] = marketWorkerPopTypeHired[secondaryWorkerPopTypeIndex] + secondaryWorkerAmount;

            float primaryWorkerPopTypeEmploymentRatio = (float) (currentPrimaryEmployed + primaryWorkerAmount) / Math.max(1f, demographics.totalByPopType(primaryWorkerPopTypeIndex));
            float secondaryWorkerPopTypeEmploymentRatio = (float) (currentSecondaryEmployed + secondaryWorkerAmount) / Math.max(1f, demographics.totalByPopType(secondaryWorkerPopTypeIndex));

            localMarketData.workerPopTypeEmploymentRatios(primaryWorkerPopTypeIndex, primaryWorkerPopTypeEmploymentRatio);
            localMarketData.workerPopTypeEmploymentRatios(secondaryWorkerPopTypeIndex, secondaryWorkerPopTypeEmploymentRatio);
        }
    }
}
