package com.populaire.projetguerrefroide.system;

import com.github.tommyettinger.ds.LongFloatMap;
import com.github.tommyettinger.ds.LongIntMap;
import com.github.tommyettinger.ds.LongList;
import com.github.tommyettinger.ds.LongObjectMap;
import com.github.tommyettinger.ds.ObjectList;
import io.github.elebras1.flecs.EntityView;
import io.github.elebras1.flecs.Field;
import io.github.elebras1.flecs.Iter;
import io.github.elebras1.flecs.Query;
import io.github.elebras1.flecs.World;
import com.populaire.projetguerrefroide.component.*;
import com.populaire.projetguerrefroide.util.EcsConstants;

import java.util.Comparator;

import static com.populaire.projetguerrefroide.util.Constants.*;
import static com.populaire.projetguerrefroide.util.ConstructionUtils.levelCost;
import static com.populaire.projetguerrefroide.util.ConstructionUtils.startSite;
import static com.populaire.projetguerrefroide.util.OwnerUtils.ownerIndex;
import static com.populaire.projetguerrefroide.util.OwnerUtils.ownerTag;

public class InvestmentDecisionSystem {

    private record Candidate(long buildingId, long buildingTypeId, long regionInstanceId, long countryId, long ownerTagId, int owner, int primaryWorkerPopTypeIndex, float workersPerLevel, int freeLevels, double levelCost, float annualReturn) {
    }

    private record TypeReturn(long buildingTypeId, float annualReturn, double levelCost, float workersPerLevel, int primaryWorkerPopTypeIndex) {
    }

    private final EcsConstants ecsConstants;
    private final Query buildingQuery;
    private final Query siteQuery;
    private final Query regionQuery;

    public InvestmentDecisionSystem(World ecsWorld, EcsConstants ecsConstants, long phaseId) {
        this.ecsConstants = ecsConstants;
        this.buildingQuery = ecsWorld.query().with(Building.class).with(EconomyBuilding.class).with(EconomyBuildingType.class).build();
        this.siteQuery = ecsWorld.query().with(ConstructionSite.class).build();
        this.regionQuery = ecsWorld.query().with(RegionInstance.class).build();
        ecsWorld.system("InvestmentDecisionSystem")
            .kind(phaseId)
            .with(CountryInvestment.class)
            .with(CountryBudget.class)
            .run(this::decide);
    }

    private void decide(Iter iter) {
        World world = iter.world();
        boolean[] ruralTypes = this.readRuralTypes(world);
        LongIntMap queuedLevels = new LongIntMap();
        LongFloatMap reservedWorkers = new LongFloatMap();
        LongObjectMap<LongIntMap> typesByRegion = new LongObjectMap<>();
        this.readSites(world, queuedLevels, reservedWorkers, typesByRegion);
        LongObjectMap<ObjectList<Candidate>> candidatesByCountry = new LongObjectMap<>();
        LongObjectMap<LongObjectMap<TypeReturn>> typeReturnsByCountry = new LongObjectMap<>();
        this.collectCandidates(world, queuedLevels, typesByRegion, candidatesByCountry, typeReturnsByCountry);
        LongObjectMap<LongList> regionsByCountry = this.readRegions(world);

        double[] availableFunds = new double[OWNER_COUNT];
        while (iter.next()) {
            Field<CountryInvestment> countryInvestmentField = iter.field(CountryInvestment.class, 0);
            Field<CountryBudget> countryBudgetField = iter.field(CountryBudget.class, 1);
            for (int i = 0; i < iter.count(); i++) {
                long countryId = iter.entity(i);
                CountryInvestmentView countryInvestment = countryInvestmentField.getMutView(i);
                CountryBudgetView countryBudget = countryBudgetField.getMutView(i);
                for (int o = 0; o < OWNER_COUNT; o++) {
                    availableFunds[o] = countryInvestment.funds(o) - countryInvestment.committed(o);
                }
                availableFunds[STATE_OWNER] = countryBudget.investableSurplus();

                ObjectList<Candidate> candidates = candidatesByCountry.get(countryId);
                if (candidates == null) {
                    candidates = new ObjectList<>();
                }
                candidates.sort(Comparator.comparingDouble((Candidate candidate) -> candidate.annualReturn()).reversed());
                for (int c = 0; c < candidates.size(); c++) {
                    Candidate candidate = candidates.get(c);
                    int freeLevels = candidate.freeLevels();
                    while (freeLevels > 0 && availableFunds[candidate.owner()] >= candidate.levelCost()
                        && this.availableWorkers(world, candidate, ruralTypes) - reservedWorkers.get(candidate.regionInstanceId()) >= candidate.workersPerLevel()) {
                        startSite(world, this.ecsConstants, candidate.buildingId(), candidate.buildingTypeId(), candidate.regionInstanceId(), candidate.countryId(), candidate.ownerTagId());
                        availableFunds[candidate.owner()] -= candidate.levelCost();
                        reservedWorkers.put(candidate.regionInstanceId(), reservedWorkers.get(candidate.regionInstanceId()) + candidate.workersPerLevel());
                        freeLevels--;
                    }
                }

                LongObjectMap<TypeReturn> typeReturns = typeReturnsByCountry.get(countryId);
                LongList regions = regionsByCountry.get(countryId);
                if (typeReturns == null || regions == null) {
                    continue;
                }
                EntityView country = world.obtainEntityView(countryId);
                CountryProfitDistributionPolicyView profitPolicy = country.getView(CountryProfitDistributionPolicy.class);
                CountryProductionPolicyView productionPolicy = country.getView(CountryProductionPolicy.class);
                long countryOwnerTag = ownerTag(profitPolicy.capitalistProfitShareRate(), profitPolicy.workerProfitShareRate(), profitPolicy.stateProfitShareRate(), this.ecsConstants);
                this.buildNewBuildings(world, countryId, countryOwnerTag, typeReturns, regions, typesByRegion, reservedWorkers, ruralTypes, availableFunds);
                if (countryOwnerTag != this.ecsConstants.countryTag() && productionPolicy.stateConstruction() > 0f) {
                    this.buildNewBuildings(world, countryId, this.ecsConstants.countryTag(), typeReturns, regions, typesByRegion, reservedWorkers, ruralTypes, availableFunds);
                }
            }
        }
    }

    private void buildNewBuildings(World world, long countryId, long ownerTagId, LongObjectMap<TypeReturn> typeReturns, LongList regions,
                                   LongObjectMap<LongIntMap> typesByRegion, LongFloatMap reservedWorkers, boolean[] ruralTypes, double[] availableFunds) {
        int owner = ownerIndex(ownerTagId, this.ecsConstants);
        ObjectList<TypeReturn> types = new ObjectList<>();
        for (LongObjectMap.Entry<TypeReturn> entry : typeReturns) {
            TypeReturn typeReturn = entry.value;
            boolean qualifies = owner == CAPITALIST_OWNER ? typeReturn.annualReturn() >= CAPITALIST_MINIMUM_RETURN : typeReturn.annualReturn() > STATE_MINIMUM_RETURN;
            if (qualifies) {
                types.add(typeReturn);
            }
        }
        types.sort(Comparator.comparingDouble((TypeReturn typeReturn) -> typeReturn.annualReturn()).reversed());
        for (int t = 0; t < types.size(); t++) {
            TypeReturn typeReturn = types.get(t);
            if (availableFunds[owner] < typeReturn.levelCost()) {
                continue;
            }
            long bestRegion = 0;
            float bestWorkers = typeReturn.workersPerLevel();
            for (int r = 0; r < regions.size(); r++) {
                long regionInstanceId = regions.get(r);
                LongIntMap regionTypes = typesByRegion.get(regionInstanceId);
                if (regionTypes != null && regionTypes.containsKey(typeReturn.buildingTypeId())) {
                    continue;
                }
                float workers = this.availableWorkers(world, regionInstanceId, typeReturn.primaryWorkerPopTypeIndex(), ruralTypes) - reservedWorkers.get(regionInstanceId);
                if (workers >= bestWorkers) {
                    bestWorkers = workers;
                    bestRegion = regionInstanceId;
                }
            }
            if (bestRegion == 0) {
                continue;
            }
            startSite(world, this.ecsConstants, 0, typeReturn.buildingTypeId(), bestRegion, countryId, ownerTagId);
            availableFunds[owner] -= typeReturn.levelCost();
            reservedWorkers.put(bestRegion, reservedWorkers.get(bestRegion) + typeReturn.workersPerLevel());
            LongIntMap regionTypes = typesByRegion.get(bestRegion);
            if (regionTypes == null) {
                regionTypes = new LongIntMap();
                typesByRegion.put(bestRegion, regionTypes);
            }
            regionTypes.put(typeReturn.buildingTypeId(), 1);
        }
    }

    private LongObjectMap<LongList> readRegions(World world) {
        LongObjectMap<LongList> regionsByCountry = new LongObjectMap<>();
        this.regionQuery.each((long regionInstanceId) -> {
            RegionInstanceView regionInstance = world.obtainEntityView(regionInstanceId).getView(RegionInstance.class);
            LongList regions = regionsByCountry.get(regionInstance.ownerId());
            if (regions == null) {
                regions = new LongList();
                regionsByCountry.put(regionInstance.ownerId(), regions);
            }
            regions.add(regionInstanceId);
        });
        return regionsByCountry;
    }

    private boolean[] readRuralTypes(World world) {
        boolean[] ruralTypes = new boolean[POP_TYPE_COUNT];
        GlobalPopulationTypeView globalPopulationType = world.obtainEntityView(world.lookup("global_population_type")).getMutView(GlobalPopulationType.class);
        for (int p = 0; p < POP_TYPE_COUNT; p++) {
            ruralTypes[p] = world.obtainEntityView(globalPopulationType.popTypeIds(p)).has(this.ecsConstants.ruralTag());
        }
        return ruralTypes;
    }

    private void readSites(World world, LongIntMap queuedLevels, LongFloatMap reservedWorkers, LongObjectMap<LongIntMap> typesByRegion) {
        this.siteQuery.each((long siteId) -> {
            ConstructionSiteView site = world.obtainEntityView(siteId).getView(ConstructionSite.class);
            long buildingId = site.buildingId();
            long regionInstanceId = site.regionInstanceId();
            EntityView buildingType = world.obtainEntityView(site.buildingTypeId());
            if (buildingId != 0) {
                queuedLevels.put(buildingId, queuedLevels.get(buildingId) + 1);
            } else {
                LongIntMap types = typesByRegion.get(regionInstanceId);
                if (types == null) {
                    types = new LongIntMap();
                    typesByRegion.put(regionInstanceId, types);
                }
                types.put(site.buildingTypeId(), 1);
            }
            if (buildingType.has(EconomyBuildingType.class)) {
                EconomyBuildingTypeView typeData = buildingType.getView(EconomyBuildingType.class);
                reservedWorkers.put(regionInstanceId, reservedWorkers.get(regionInstanceId) + typeData.workforce() * typeData.primaryWorkerPopTypeRatio());
            }
        });
    }

    private void collectCandidates(World world, LongIntMap queuedLevels, LongObjectMap<LongIntMap> typesByRegion, LongObjectMap<ObjectList<Candidate>> candidatesByCountry,
                                   LongObjectMap<LongObjectMap<TypeReturn>> typeReturnsByCountry) {
        this.buildingQuery.iter(buildingIter -> {
            EconomyBuildingTypeView buildingType = buildingIter.field(EconomyBuildingType.class, 2).getView(0);
            long buildingTypeId = buildingIter.src(2);
            int maxLevel = buildingType.maxLevel();
            int primaryWorkerPopTypeIndex = buildingType.primaryWorkerPopTypeIndex();
            float workersPerLevel = buildingType.workforce() * buildingType.primaryWorkerPopTypeRatio();

            long countryId = 0;
            double levelCost = 0.0;
            Field<Building> buildingField = buildingIter.field(Building.class, 0);
            Field<EconomyBuilding> economyBuildingField = buildingIter.field(EconomyBuilding.class, 1);
            for (int i = 0; i < buildingIter.count(); i++) {
                BuildingView building = buildingField.getMutView(i);
                EconomyBuildingView economyBuilding = economyBuildingField.getMutView(i);
                if (building.countryId() != countryId) {
                    countryId = building.countryId();
                    CountryMarketView countryMarket = world.obtainEntityView(countryId).getView(CountryMarket.class);
                    levelCost = levelCost(world, buildingTypeId, countryMarket);
                }

                long buildingId = buildingIter.entity(i);
                LongIntMap regionTypes = typesByRegion.get(building.parentId());
                if (regionTypes == null) {
                    regionTypes = new LongIntMap();
                    typesByRegion.put(building.parentId(), regionTypes);
                }
                regionTypes.put(buildingTypeId, 1);
                int freeLevels = maxLevel - building.size() - queuedLevels.get(buildingId);
                if (freeLevels <= 0 || economyBuilding.utilization() < MINIMUM_UTILIZATION_FOR_EXPANSION || levelCost <= 0.0) {
                    continue;
                }

                float annualReturn = (float) (economyBuilding.profit() / building.size() * DAYS_PER_YEAR / levelCost);
                LongObjectMap<TypeReturn> typeReturns = typeReturnsByCountry.get(countryId);
                if (typeReturns == null) {
                    typeReturns = new LongObjectMap<>();
                    typeReturnsByCountry.put(countryId, typeReturns);
                }
                TypeReturn typeReturn = typeReturns.get(buildingTypeId);
                if (typeReturn == null || annualReturn > typeReturn.annualReturn()) {
                    typeReturns.put(buildingTypeId, new TypeReturn(buildingTypeId, annualReturn, levelCost, workersPerLevel, primaryWorkerPopTypeIndex));
                }
                int owner = ownerIndex(economyBuilding.ownerTagId(), this.ecsConstants);
                boolean qualifies = owner == CAPITALIST_OWNER ? annualReturn >= CAPITALIST_MINIMUM_RETURN : annualReturn > STATE_MINIMUM_RETURN;
                if (!qualifies) {
                    continue;
                }

                ObjectList<Candidate> candidates = candidatesByCountry.get(countryId);
                if (candidates == null) {
                    candidates = new ObjectList<>();
                    candidatesByCountry.put(countryId, candidates);
                }
                candidates.add(new Candidate(buildingId, buildingTypeId, building.parentId(), countryId, economyBuilding.ownerTagId(), owner,
                    primaryWorkerPopTypeIndex, workersPerLevel, freeLevels, levelCost, annualReturn));
            }
        });
    }

    private float availableWorkers(World world, Candidate candidate, boolean[] ruralTypes) {
        return this.availableWorkers(world, candidate.regionInstanceId(), candidate.primaryWorkerPopTypeIndex(), ruralTypes);
    }

    private float availableWorkers(World world, long regionInstanceId, int primaryWorkerPopTypeIndex, boolean[] ruralTypes) {
        RegionDemographicsView regionDemographics = world.obtainEntityView(regionInstanceId).getView(RegionDemographics.class);
        float availableWorkers = 0f;
        for (int p = 0; p < POP_TYPE_COUNT; p++) {
            if (p == primaryWorkerPopTypeIndex || ruralTypes[p]) {
                availableWorkers += Math.max(0, regionDemographics.totalByPopType(p) - regionDemographics.employmentByPopType(p));
            }
        }
        return availableWorkers;
    }
}
