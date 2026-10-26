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
import com.populaire.projetguerrefroide.util.EmploymentUtils;
import com.populaire.projetguerrefroide.util.MarketUtils;

import java.util.Comparator;

import static com.populaire.projetguerrefroide.util.Constants.*;
import static com.populaire.projetguerrefroide.util.ConstructionUtils.levelCost;
import static com.populaire.projetguerrefroide.util.ConstructionUtils.startSite;
import static com.populaire.projetguerrefroide.util.OwnerUtils.ownerIndex;
import static com.populaire.projetguerrefroide.util.OwnerUtils.ownerTag;
import static com.populaire.projetguerrefroide.util.StateBudgetUtils.stateInterestRate;

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
            .with(CountryBank.class)
            .with(CountryMarket.class)
            .run(this::decide);
    }

    private void decide(Iter iter) {
        World world = iter.world();
        boolean[] ruralTypes = this.readRuralTypes(world);
        LongIntMap queuedLevels = new LongIntMap();
        LongFloatMap reservedWorkers = new LongFloatMap();
        LongObjectMap<LongIntMap> typesByRegion = new LongObjectMap<>();
        LongObjectMap<LongFloatMap> parkUsedLevels = new LongObjectMap<>();
        LongObjectMap<LongFloatMap> parkLevels = new LongObjectMap<>();
        this.readSites(world, queuedLevels, reservedWorkers, typesByRegion, parkLevels);
        LongObjectMap<ObjectList<Candidate>> candidatesByCountry = new LongObjectMap<>();
        LongObjectMap<LongObjectMap<TypeReturn>> typeReturnsByCountry = new LongObjectMap<>();
        this.collectCandidates(world, queuedLevels, typesByRegion, candidatesByCountry, typeReturnsByCountry, parkUsedLevels, parkLevels);
        LongObjectMap<LongList> regionsByCountry = this.readRegions(world);

        double[] availableFunds = new double[OWNER_COUNT];
        GlobalMarketView globalMarket = world.obtainEntityView(world.lookup("global_market")).getMutView(GlobalMarket.class);
        while (iter.next()) {
            Field<CountryInvestment> countryInvestmentField = iter.field(CountryInvestment.class, 0);
            Field<CountryBudget> countryBudgetField = iter.field(CountryBudget.class, 1);
            Field<CountryBank> countryBankField = iter.field(CountryBank.class, 2);
            Field<CountryMarket> countryMarketField = iter.field(CountryMarket.class, 3);
            for (int i = 0; i < iter.count(); i++) {
                long countryId = iter.entity(i);
                CountryInvestmentMutView countryInvestment = countryInvestmentField.getMutView(i);
                CountryBudgetView countryBudget = countryBudgetField.getMutView(i);
                CountryBankMutView countryBank = countryBankField.getMutView(i);
                CountryMarketMutView countryMarket = countryMarketField.getMutView(i);
                for (int o = 0; o < OWNER_COUNT; o++) {
                    availableFunds[o] = countryInvestment.funds(o) - countryInvestment.committed(o);
                }
                availableFunds[STATE_OWNER] = countryBudget.investableSurplus();

                EntityView country = world.obtainEntityView(countryId);
                CountryProfitDistributionPolicyView profitPolicy = country.getView(CountryProfitDistributionPolicy.class);
                CountryProductionPolicyView productionPolicy = country.getView(CountryProductionPolicy.class);
                long countryOwnerTag = ownerTag(profitPolicy.capitalistProfitShareRate(), profitPolicy.workerProfitShareRate(), profitPolicy.stateProfitShareRate(), this.ecsConstants);
                boolean stateOwnedEconomy = countryOwnerTag == this.ecsConstants.countryTag();
                ObjectList<Candidate> candidates = candidatesByCountry.get(countryId);
                if (candidates == null) {
                    candidates = new ObjectList<>();
                }
                candidates.sort(Comparator.comparingDouble((Candidate candidate) -> candidate.annualReturn()).reversed());
                for (int c = 0; c < candidates.size(); c++) {
                    Candidate candidate = candidates.get(c);
                    int freeLevels = candidate.freeLevels();
                    while (freeLevels > 0
                        && this.availableWorkers(world, candidate, ruralTypes) - reservedWorkers.get(candidate.regionInstanceId()) >= candidate.workersPerLevel()
                        && (parkUtilization(parkUsedLevels, parkLevels, countryId, candidate.buildingTypeId()) >= MINIMUM_UTILIZATION_FOR_EXPANSION
                            ? finance(countryBank, countryInvestment, countryMarket, availableFunds, candidate.owner(), candidate.levelCost(), candidate.annualReturn())
                            : stateOwnedEconomy && candidate.owner() == STATE_OWNER && availableFunds[STATE_OWNER] >= candidate.levelCost())) {
                        startSite(world, this.ecsConstants, candidate.buildingId(), candidate.buildingTypeId(), candidate.regionInstanceId(), candidate.countryId(), candidate.ownerTagId());
                        addLevels(parkLevels, countryId, candidate.buildingTypeId(), 1f);
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
                this.buildNewBuildings(world, countryId, countryOwnerTag, stateOwnedEconomy, typeReturns, regions, typesByRegion, reservedWorkers, ruralTypes, availableFunds, parkUsedLevels, parkLevels,
                    countryBank, countryInvestment, countryMarket);
                if (countryOwnerTag != this.ecsConstants.countryTag() && productionPolicy.stateConstruction() > 0f) {
                    this.buildNewBuildings(world, countryId, this.ecsConstants.countryTag(), false, typeReturns, regions, typesByRegion, reservedWorkers, ruralTypes, availableFunds, parkUsedLevels, parkLevels,
                        countryBank, countryInvestment, countryMarket);
                }
                if (productionPolicy.stateConstruction() > 0f) {
                    this.buildScarceBuildings(world, countryId, globalMarket, typeReturns, regions, typesByRegion, reservedWorkers, ruralTypes, availableFunds, parkLevels);
                }
            }
        }
    }

    private void buildNewBuildings(World world, long countryId, long ownerTagId, boolean stateOwnedEconomy, LongObjectMap<TypeReturn> typeReturns, LongList regions,
                                   LongObjectMap<LongIntMap> typesByRegion, LongFloatMap reservedWorkers, boolean[] ruralTypes, double[] availableFunds,
                                   LongObjectMap<LongFloatMap> parkUsedLevels, LongObjectMap<LongFloatMap> parkLevels,
                                   CountryBankMutView countryBank, CountryInvestmentMutView countryInvestment, CountryMarketMutView countryMarket) {
        int owner = ownerIndex(ownerTagId, this.ecsConstants);
        ObjectList<TypeReturn> types = new ObjectList<>();
        for (LongObjectMap.Entry<TypeReturn> entry : typeReturns) {
            TypeReturn typeReturn = entry.value;
            boolean qualifies = qualifies(owner, typeReturn.annualReturn(), countryBank.lendingRate());
            if (qualifies) {
                types.add(typeReturn);
            }
        }
        types.sort(Comparator.comparingDouble((TypeReturn typeReturn) -> typeReturn.annualReturn()).reversed());
        for (int t = 0; t < types.size(); t++) {
            TypeReturn typeReturn = types.get(t);
            boolean underused = parkUtilization(parkUsedLevels, parkLevels, countryId, typeReturn.buildingTypeId()) < MINIMUM_UTILIZATION_FOR_EXPANSION;
            if (underused && !(stateOwnedEconomy && availableFunds[owner] >= typeReturn.levelCost())) {
                continue;
            }
            long bestRegion = this.bestRegion(world, typeReturn, regions, typesByRegion, reservedWorkers, ruralTypes);
            if (bestRegion == 0 || !underused && !finance(countryBank, countryInvestment, countryMarket, availableFunds, owner, typeReturn.levelCost(), typeReturn.annualReturn())) {
                continue;
            }
            this.startNewBuilding(world, countryId, ownerTagId, owner, typeReturn, bestRegion, typesByRegion, reservedWorkers, availableFunds, parkLevels);
        }
    }

    private void buildScarceBuildings(World world, long countryId, GlobalMarketView globalMarket, LongObjectMap<TypeReturn> typeReturns, LongList regions,
                                      LongObjectMap<LongIntMap> typesByRegion, LongFloatMap reservedWorkers, boolean[] ruralTypes, double[] availableFunds,
                                      LongObjectMap<LongFloatMap> parkLevels) {
        ObjectList<TypeReturn> types = new ObjectList<>();
        for (LongObjectMap.Entry<TypeReturn> entry : typeReturns) {
            EconomyBuildingTypeView buildingType = world.obtainEntityView(entry.value.buildingTypeId()).getView(EconomyBuildingType.class);
            if (MarketUtils.clearingGap(globalMarket, buildingType.goodOutputIndex()) > 0f) {
                types.add(entry.value);
            }
        }
        types.sort(Comparator.comparingDouble((TypeReturn typeReturn) -> typeReturn.annualReturn()).reversed());
        for (int t = 0; t < types.size(); t++) {
            TypeReturn typeReturn = types.get(t);
            if (availableFunds[STATE_OWNER] < typeReturn.levelCost()) {
                continue;
            }
            long bestRegion = this.bestRegion(world, typeReturn, regions, typesByRegion, reservedWorkers, ruralTypes);
            if (bestRegion == 0) {
                continue;
            }
            this.startNewBuilding(world, countryId, this.ecsConstants.countryTag(), STATE_OWNER, typeReturn, bestRegion, typesByRegion, reservedWorkers, availableFunds, parkLevels);
        }
    }

    private long bestRegion(World world, TypeReturn typeReturn, LongList regions, LongObjectMap<LongIntMap> typesByRegion, LongFloatMap reservedWorkers, boolean[] ruralTypes) {
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
        return bestRegion;
    }

    private void startNewBuilding(World world, long countryId, long ownerTagId, int owner, TypeReturn typeReturn, long regionInstanceId,
                                  LongObjectMap<LongIntMap> typesByRegion, LongFloatMap reservedWorkers, double[] availableFunds, LongObjectMap<LongFloatMap> parkLevels) {
        startSite(world, this.ecsConstants, 0, typeReturn.buildingTypeId(), regionInstanceId, countryId, ownerTagId);
        addLevels(parkLevels, countryId, typeReturn.buildingTypeId(), 1f);
        availableFunds[owner] -= typeReturn.levelCost();
        reservedWorkers.put(regionInstanceId, reservedWorkers.get(regionInstanceId) + typeReturn.workersPerLevel());
        LongIntMap regionTypes = typesByRegion.get(regionInstanceId);
        if (regionTypes == null) {
            regionTypes = new LongIntMap();
            typesByRegion.put(regionInstanceId, regionTypes);
        }
        regionTypes.put(typeReturn.buildingTypeId(), 1);
    }

    private static boolean finance(CountryBankMutView countryBank, CountryInvestmentMutView countryInvestment, CountryMarketMutView countryMarket,
                                   double[] availableFunds, int owner, double cost, float annualReturn) {
        boolean bankOnly = owner == CAPITALIST_OWNER && annualReturn < CAPITALIST_MINIMUM_RETURN;
        double shortfall = bankOnly ? cost : cost - availableFunds[owner];
        if (shortfall <= 0.0) {
            return true;
        }
        double loans = countryBank.loans(owner);
        boolean state = owner == STATE_OWNER;
        double rate = state ? stateInterestRate(loans, countryBank.creditLimit(), countryBank.lendingRate()) : countryBank.lendingRate();
        if (annualReturn < rate || countryBank.reserves() < shortfall
            || state && loans + shortfall > countryBank.creditLimit()) {
            return false;
        }
        countryBank.reserves(countryBank.reserves() - shortfall);
        countryBank.loans(owner, countryBank.loans(owner) + shortfall);
        countryBank.borrowed(owner, countryBank.borrowed(owner) + shortfall);
        if (state) {
            countryMarket.treasury(countryMarket.treasury() + shortfall);
        } else {
            countryInvestment.funds(owner, countryInvestment.funds(owner) + shortfall);
        }
        availableFunds[owner] += shortfall;
        return true;
    }

    private static boolean qualifies(int owner, float annualReturn, float lendingRate) {
        return owner == CAPITALIST_OWNER ? annualReturn >= Math.min(CAPITALIST_MINIMUM_RETURN, lendingRate) : annualReturn > STATE_MINIMUM_RETURN;
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

    private static float parkUtilization(LongObjectMap<LongFloatMap> parkUsedLevels, LongObjectMap<LongFloatMap> parkLevels, long countryId, long buildingTypeId) {
        LongFloatMap levels = parkLevels.get(countryId);
        float totalLevels = levels != null ? levels.get(buildingTypeId) : 0f;
        if (totalLevels <= 0f) {
            return 1f;
        }
        LongFloatMap usedLevels = parkUsedLevels.get(countryId);
        return (usedLevels != null ? usedLevels.get(buildingTypeId) : 0f) / totalLevels;
    }

    private static void addLevels(LongObjectMap<LongFloatMap> park, long countryId, long buildingTypeId, float levels) {
        LongFloatMap countryPark = park.get(countryId);
        if (countryPark == null) {
            countryPark = new LongFloatMap();
            park.put(countryId, countryPark);
        }
        countryPark.put(buildingTypeId, countryPark.get(buildingTypeId) + levels);
    }

    private void readSites(World world, LongIntMap queuedLevels, LongFloatMap reservedWorkers, LongObjectMap<LongIntMap> typesByRegion, LongObjectMap<LongFloatMap> parkLevels) {
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
                addLevels(parkLevels, site.countryId(), site.buildingTypeId(), 1f);
                EconomyBuildingTypeView typeData = buildingType.getView(EconomyBuildingType.class);
                reservedWorkers.put(regionInstanceId, reservedWorkers.get(regionInstanceId) + typeData.workforce() * typeData.primaryWorkerPopTypeRatio());
            }
        });
    }

    private void collectCandidates(World world, LongIntMap queuedLevels, LongObjectMap<LongIntMap> typesByRegion, LongObjectMap<ObjectList<Candidate>> candidatesByCountry,
                                   LongObjectMap<LongObjectMap<TypeReturn>> typeReturnsByCountry, LongObjectMap<LongFloatMap> parkUsedLevels, LongObjectMap<LongFloatMap> parkLevels) {
        this.buildingQuery.iter(buildingIter -> {
            EconomyBuildingTypeView buildingType = buildingIter.field(EconomyBuildingType.class, 2).getView(0);
            long buildingTypeId = buildingIter.src(2);
            int maxLevel = buildingType.maxLevel();
            int primaryWorkerPopTypeIndex = buildingType.primaryWorkerPopTypeIndex();
            float workersPerLevel = buildingType.workforce() * buildingType.primaryWorkerPopTypeRatio();

            long countryId = 0;
            double levelCost = 0.0;
            float lendingRate = 0f;
            Field<Building> buildingField = buildingIter.field(Building.class, 0);
            Field<EconomyBuilding> economyBuildingField = buildingIter.field(EconomyBuilding.class, 1);
            for (int i = 0; i < buildingIter.count(); i++) {
                BuildingView building = buildingField.getMutView(i);
                EconomyBuildingView economyBuilding = economyBuildingField.getMutView(i);
                if (building.countryId() != countryId) {
                    countryId = building.countryId();
                    EntityView country = world.obtainEntityView(countryId);
                    CountryMarketView countryMarket = country.getView(CountryMarket.class);
                    CountryBankView countryBank = country.getView(CountryBank.class);
                    levelCost = levelCost(world, buildingTypeId, countryMarket);
                    lendingRate = countryBank.lendingRate();
                }

                long buildingId = buildingIter.entity(i);
                LongIntMap regionTypes = typesByRegion.get(building.parentId());
                if (regionTypes == null) {
                    regionTypes = new LongIntMap();
                    typesByRegion.put(building.parentId(), regionTypes);
                }
                regionTypes.put(buildingTypeId, 1);
                addLevels(parkUsedLevels, countryId, buildingTypeId, economyBuilding.utilization() * building.size());
                addLevels(parkLevels, countryId, buildingTypeId, building.size());
                int freeLevels = maxLevel - building.size() - queuedLevels.get(buildingId);
                if (freeLevels <= 0 || levelCost <= 0.0) {
                    continue;
                }

                float annualReturn = (float) (economyBuilding.profitTrend() / building.size() * DAYS_PER_YEAR / levelCost);
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
                boolean qualifies = qualifies(owner, annualReturn, lendingRate);
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
        EntityView regionInstanceEntity = world.obtainEntityView(regionInstanceId);
        RegionDemographicsView regionDemographics = regionInstanceEntity.getView(RegionDemographics.class);
        RegionInstanceView regionInstance = regionInstanceEntity.getView(RegionInstance.class);
        CountryPopulationPolicyView populationPolicy = world.obtainEntityView(regionInstance.ownerId()).getView(CountryPopulationPolicy.class);
        float[] baseWorkforceShares = EmploymentUtils.baseWorkforceShares(world);
        float availableWorkers = 0f;
        for (int p = 0; p < POP_TYPE_COUNT; p++) {
            if (p == primaryWorkerPopTypeIndex || ruralTypes[p]) {
                availableWorkers += Math.max(0f, regionDemographics.totalByPopType(p) * EmploymentUtils.activeShare(baseWorkforceShares[p], populationPolicy.workforceShareModifier()) - regionDemographics.employmentByPopType(p));
            }
        }
        return availableWorkers;
    }
}
