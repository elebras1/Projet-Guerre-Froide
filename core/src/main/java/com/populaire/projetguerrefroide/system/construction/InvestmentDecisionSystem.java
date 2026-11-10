package com.populaire.projetguerrefroide.system.construction;

import com.github.tommyettinger.ds.LongFloatMap;
import com.github.tommyettinger.ds.LongIntMap;
import com.github.tommyettinger.ds.LongList;
import com.github.tommyettinger.ds.LongObjectMap;
import com.github.tommyettinger.ds.ObjectList;
import com.populaire.projetguerrefroide.util.collection.LongFloatSlotMap;
import com.populaire.projetguerrefroide.util.collection.LongIntSlotMap;
import com.populaire.projetguerrefroide.util.collection.LongObjectSlotMap;
import com.populaire.projetguerrefroide.util.collection.ObjectPool;
import io.github.elebras1.flecs.EntityView;
import io.github.elebras1.flecs.Field;
import io.github.elebras1.flecs.Iter;
import io.github.elebras1.flecs.Query;
import io.github.elebras1.flecs.World;
import io.github.elebras1.flecs.callback.EntityCallback;
import io.github.elebras1.flecs.callback.IterCallback;
import com.populaire.projetguerrefroide.component.*;
import com.populaire.projetguerrefroide.simulation.EcsConstants;
import com.populaire.projetguerrefroide.system.population.EmploymentUtils;
import com.populaire.projetguerrefroide.system.market.MarketUtils;

import java.util.Comparator;

import static com.populaire.projetguerrefroide.util.ArrayUtils.zeroed;
import static com.populaire.projetguerrefroide.simulation.Constants.*;
import static com.populaire.projetguerrefroide.system.construction.ConstructionUtils.levelCost;
import static com.populaire.projetguerrefroide.system.construction.ConstructionUtils.startMechanizationSite;
import static com.populaire.projetguerrefroide.system.construction.ConstructionUtils.startSite;
import static com.populaire.projetguerrefroide.system.building.OwnerUtils.ownerIndex;
import static com.populaire.projetguerrefroide.system.building.OwnerUtils.ownerTag;
import static com.populaire.projetguerrefroide.system.state.StateBudgetUtils.stateInterestRate;

public class InvestmentDecisionSystem {

    private static final Comparator<Candidate> CANDIDATE_ORDER = Comparator.comparingDouble((Candidate candidate) -> candidate.annualReturn()).reversed();
    private static final Comparator<TypeReturn> TYPE_RETURN_ORDER = Comparator.comparingDouble((TypeReturn typeReturn) -> typeReturn.annualReturn()).reversed();

    private static final class Candidate {
        private long buildingId;
        private long buildingTypeId;
        private long regionInstanceId;
        private long countryId;
        private long ownerTagId;
        private int owner;
        private int primaryWorkerPopTypeIndex;
        private float workersPerLevel;
        private int freeLevels;
        private double levelCost;
        private float annualReturn;
        private boolean mechanization;
        private double baseValue;

        private Candidate mechanization(double baseValue) {
            this.mechanization = true;
            this.baseValue = baseValue;
            return this;
        }

        private Candidate set(long buildingId, long buildingTypeId, long regionInstanceId, long countryId, long ownerTagId, int owner, int primaryWorkerPopTypeIndex,
                              float workersPerLevel, int freeLevels, double levelCost, float annualReturn) {
            this.buildingId = buildingId;
            this.buildingTypeId = buildingTypeId;
            this.regionInstanceId = regionInstanceId;
            this.countryId = countryId;
            this.ownerTagId = ownerTagId;
            this.owner = owner;
            this.primaryWorkerPopTypeIndex = primaryWorkerPopTypeIndex;
            this.workersPerLevel = workersPerLevel;
            this.freeLevels = freeLevels;
            this.levelCost = levelCost;
            this.annualReturn = annualReturn;
            this.mechanization = false;
            this.baseValue = 0.0;
            return this;
        }

        private boolean isMechanization() {
            return this.mechanization;
        }

        private double baseValue() {
            return this.baseValue;
        }

        private long buildingId() {
            return this.buildingId;
        }

        private long buildingTypeId() {
            return this.buildingTypeId;
        }

        private long regionInstanceId() {
            return this.regionInstanceId;
        }

        private long countryId() {
            return this.countryId;
        }

        private long ownerTagId() {
            return this.ownerTagId;
        }

        private int owner() {
            return this.owner;
        }

        private int primaryWorkerPopTypeIndex() {
            return this.primaryWorkerPopTypeIndex;
        }

        private float workersPerLevel() {
            return this.workersPerLevel;
        }

        private int freeLevels() {
            return this.freeLevels;
        }

        private double levelCost() {
            return this.levelCost;
        }

        private float annualReturn() {
            return this.annualReturn;
        }
    }

    private static final class TypeReturn {
        private long buildingTypeId;
        private float annualReturn;
        private double levelCost;
        private float workersPerLevel;
        private int primaryWorkerPopTypeIndex;

        private TypeReturn set(long buildingTypeId, float annualReturn, double levelCost, float workersPerLevel, int primaryWorkerPopTypeIndex) {
            this.buildingTypeId = buildingTypeId;
            this.annualReturn = annualReturn;
            this.levelCost = levelCost;
            this.workersPerLevel = workersPerLevel;
            this.primaryWorkerPopTypeIndex = primaryWorkerPopTypeIndex;
            return this;
        }

        private long buildingTypeId() {
            return this.buildingTypeId;
        }

        private float annualReturn() {
            return this.annualReturn;
        }

        private double levelCost() {
            return this.levelCost;
        }

        private float workersPerLevel() {
            return this.workersPerLevel;
        }

        private int primaryWorkerPopTypeIndex() {
            return this.primaryWorkerPopTypeIndex;
        }
    }

    private final EcsConstants ecsConstants;
    private final Query buildingQuery;
    private final Query siteQuery;
    private final Query regionQuery;
    private final Query rgoQuery;
    private final LongIntMap queuedLevels = new LongIntMap();
    private final LongFloatMap reservedWorkers = new LongFloatMap();
    private final LongObjectMap<LongIntMap> typesByRegion = new LongObjectMap<>();
    private final LongObjectMap<LongFloatMap> parkUsedLevels = new LongObjectMap<>();
    private final LongObjectMap<LongFloatMap> parkLevels = new LongObjectMap<>();
    private final LongIntSlotMap sitesByType = new LongIntSlotMap();
    private final LongObjectMap<ObjectList<Candidate>> candidatesByCountry = new LongObjectMap<>();
    private final LongObjectMap<LongObjectSlotMap<TypeReturn>> typeReturnsByCountry = new LongObjectMap<>();
    private final LongFloatMap levelOutputs = new LongFloatMap();
    private final LongFloatMap outputsByType = new LongFloatMap();
    private final LongFloatSlotMap levelsByType = new LongFloatSlotMap();
    private final LongObjectMap<LongList> regionsByCountry = new LongObjectMap<>();
    private final float[] pendingOutputs = new float[GOOD_COUNT];
    private final double[] availableFunds = new double[OWNER_COUNT];
    private final ObjectList<Candidate> noCandidates = new ObjectList<>();
    private final ObjectList<TypeReturn> newBuildingTypes = new ObjectList<>();
    private final ObjectList<TypeReturn> scarceBuildingTypes = new ObjectList<>();
    private final ObjectPool<LongIntMap> regionTypesPool = new ObjectPool<>(LongIntMap::new, LongIntMap::clear);
    private final ObjectPool<LongFloatMap> parkPool = new ObjectPool<>(LongFloatMap::new, LongFloatMap::clear);
    private final ObjectPool<ObjectList<Candidate>> candidateListPool = new ObjectPool<>(ObjectList::new, ObjectList::clear);
    private final ObjectPool<LongObjectSlotMap<TypeReturn>> typeReturnMapPool = new ObjectPool<>(LongObjectSlotMap::new, LongObjectSlotMap::clear);
    private final ObjectPool<LongList> regionListPool = new ObjectPool<>(LongList::new, LongList::clear);
    private final ObjectPool<Candidate> candidatePool = new ObjectPool<>(Candidate::new, candidate -> {
    });
    private final ObjectPool<TypeReturn> typeReturnPool = new ObjectPool<>(TypeReturn::new, typeReturn -> {
    });
    private final EntityCallback siteReader = this::readSite;
    private final EntityCallback regionReader = this::readRegion;
    private final IterCallback buildingReader = this::readBuildings;
    private final IterCallback rgoReader = this::readRgos;
    private World world;

    public InvestmentDecisionSystem(World ecsWorld, EcsConstants ecsConstants, long phaseId) {
        this.ecsConstants = ecsConstants;
        this.buildingQuery = ecsWorld.query().with(Building.class).with(EconomyBuilding.class).with(EconomyBuildingType.class).build();
        this.siteQuery = ecsWorld.query().with(ConstructionSite.class).build();
        this.regionQuery = ecsWorld.query().with(RegionInstance.class).build();
        this.rgoQuery = ecsWorld.query().with(Province.class).with(ResourceGathering.class).with(Demographics.class)
            .with(ResourceGatheringType.class).up(ecsConstants.rgoType()).build();
        ecsWorld.system("InvestmentDecisionSystem")
            .kind(phaseId)
            .with(CountryInvestment.class)
            .with(CountryBudget.class)
            .with(CountryBank.class)
            .with(CountryMarket.class)
            .run(this::decide);
    }

    private void decide(Iter iter) {
        World world = iter.stage();
        this.world = world;
        this.reset();
        boolean[] ruralTypes = this.ecsConstants.ruralPopTypes();
        LongFloatMap reservedWorkers = this.reservedWorkers;
        LongObjectMap<LongIntMap> typesByRegion = this.typesByRegion;
        LongObjectMap<LongFloatMap> parkUsedLevels = this.parkUsedLevels;
        LongObjectMap<LongFloatMap> parkLevels = this.parkLevels;
        this.siteQuery.each(this.siteReader);
        LongObjectMap<ObjectList<Candidate>> candidatesByCountry = this.candidatesByCountry;
        LongObjectMap<LongObjectSlotMap<TypeReturn>> typeReturnsByCountry = this.typeReturnsByCountry;
        LongFloatMap levelOutputs = this.levelOutputs;
        this.collectCandidates();
        float[] pendingOutputs = this.pendingOutputs(world);
        LongObjectMap<LongList> regionsByCountry = this.regionsByCountry;
        this.regionQuery.each(this.regionReader);

        double[] availableFunds = this.availableFunds;
        GlobalMarketView globalMarket = world.obtainEntityView(this.ecsConstants.globalMarket()).getMutView(GlobalMarket.class);
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
                    candidates = this.noCandidates;
                }
                candidates.sort(CANDIDATE_ORDER);
                for (int c = 0; c < candidates.size(); c++) {
                    Candidate candidate = candidates.get(c);
                    int freeLevels = candidate.freeLevels();
                    while (freeLevels > 0
                        && (candidate.isMechanization() || this.availableWorkers(world, candidate, ruralTypes) - reservedWorkers.get(candidate.regionInstanceId()) >= candidate.workersPerLevel())
                        && (parkUtilization(parkUsedLevels, parkLevels, countryId, candidate.buildingTypeId()) >= MINIMUM_UTILIZATION_FOR_EXPANSION
                            ? finance(countryBank, countryInvestment, countryMarket, availableFunds, candidate.owner(), candidate.levelCost(), candidate.annualReturn())
                            : stateOwnedEconomy && candidate.owner() == STATE_OWNER && availableFunds[STATE_OWNER] >= candidate.levelCost())) {
                        if (candidate.isMechanization()) {
                            startMechanizationSite(world, this.ecsConstants, candidate.buildingId(), candidate.regionInstanceId(), candidate.countryId(), candidate.ownerTagId(), candidate.baseValue());
                        } else {
                            startSite(world, this.ecsConstants, candidate.buildingId(), candidate.buildingTypeId(), candidate.regionInstanceId(), candidate.countryId(), candidate.ownerTagId());
                            addPendingOutput(world, pendingOutputs, levelOutputs, candidate.buildingTypeId());
                            this.addLevels(parkLevels, countryId, candidate.buildingTypeId(), 1f);
                        }
                        availableFunds[candidate.owner()] -= candidate.levelCost();
                        reservedWorkers.put(candidate.regionInstanceId(), reservedWorkers.get(candidate.regionInstanceId()) + candidate.workersPerLevel());
                        freeLevels--;
                    }
                }

                LongObjectSlotMap<TypeReturn> typeReturns = typeReturnsByCountry.get(countryId);
                LongList regions = regionsByCountry.get(countryId);
                if (typeReturns == null || regions == null) {
                    continue;
                }
                this.buildNewBuildings(world, countryId, countryOwnerTag, stateOwnedEconomy, typeReturns, regions, typesByRegion, reservedWorkers, ruralTypes, availableFunds, parkUsedLevels, parkLevels,
                    pendingOutputs, levelOutputs, countryBank, countryInvestment, countryMarket);
                if (countryOwnerTag != this.ecsConstants.countryTag() && productionPolicy.stateConstruction() > 0f) {
                    this.buildNewBuildings(world, countryId, this.ecsConstants.countryTag(), false, typeReturns, regions, typesByRegion, reservedWorkers, ruralTypes, availableFunds, parkUsedLevels, parkLevels,
                        pendingOutputs, levelOutputs, countryBank, countryInvestment, countryMarket);
                }
                if (productionPolicy.stateConstruction() > 0f) {
                    this.buildScarceBuildings(world, countryId, globalMarket, typeReturns, regions, typesByRegion, reservedWorkers, ruralTypes, availableFunds, parkUsedLevels, parkLevels,
                        pendingOutputs, levelOutputs);
                }
            }
        }
    }

    private void buildNewBuildings(World world, long countryId, long ownerTagId, boolean stateOwnedEconomy, LongObjectSlotMap<TypeReturn> typeReturns, LongList regions,
                                   LongObjectMap<LongIntMap> typesByRegion, LongFloatMap reservedWorkers, boolean[] ruralTypes, double[] availableFunds,
                                   LongObjectMap<LongFloatMap> parkUsedLevels, LongObjectMap<LongFloatMap> parkLevels, float[] pendingOutputs, LongFloatMap levelOutputs,
                                   CountryBankMutView countryBank, CountryInvestmentMutView countryInvestment, CountryMarketMutView countryMarket) {
        int owner = ownerIndex(ownerTagId, this.ecsConstants);
        ObjectList<TypeReturn> types = this.newBuildingTypes;
        types.clear();
        for (int slot = 0; slot < typeReturns.slotCount(); slot++) {
            if (typeReturns.slotKey(slot) == 0) {
                continue;
            }
            TypeReturn typeReturn = typeReturns.slotValue(slot);
            boolean qualifies = qualifies(owner, typeReturn.annualReturn(), countryBank.lendingRate());
            if (qualifies) {
                types.add(typeReturn);
            }
        }
        types.sort(TYPE_RETURN_ORDER);
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
            this.startNewBuilding(world, countryId, ownerTagId, owner, typeReturn, bestRegion, typesByRegion, reservedWorkers, availableFunds, parkLevels, pendingOutputs, levelOutputs);
        }
    }

    private void buildScarceBuildings(World world, long countryId, GlobalMarketView globalMarket, LongObjectSlotMap<TypeReturn> typeReturns, LongList regions,
                                      LongObjectMap<LongIntMap> typesByRegion, LongFloatMap reservedWorkers, boolean[] ruralTypes, double[] availableFunds,
                                      LongObjectMap<LongFloatMap> parkUsedLevels, LongObjectMap<LongFloatMap> parkLevels, float[] pendingOutputs, LongFloatMap levelOutputs) {
        ObjectList<TypeReturn> types = this.scarceBuildingTypes;
        types.clear();
        for (int slot = 0; slot < typeReturns.slotCount(); slot++) {
            if (typeReturns.slotKey(slot) == 0) {
                continue;
            }
            TypeReturn typeReturn = typeReturns.slotValue(slot);
            if (parkUtilization(parkUsedLevels, parkLevels, countryId, typeReturn.buildingTypeId()) < MINIMUM_UTILIZATION_FOR_EXPANSION) {
                continue;
            }
            EconomyBuildingTypeView buildingType = world.obtainEntityView(typeReturn.buildingTypeId()).getView(EconomyBuildingType.class);
            int goodIndex = buildingType.goodOutputIndex();
            if (MarketUtils.clearingProduction(globalMarket, goodIndex) > globalMarket.goodProductionAmounts(goodIndex) + pendingOutputs[goodIndex]) {
                types.add(typeReturn);
            }
        }
        types.sort(TYPE_RETURN_ORDER);
        for (int t = 0; t < types.size(); t++) {
            TypeReturn typeReturn = types.get(t);
            if (availableFunds[STATE_OWNER] < typeReturn.levelCost()) {
                continue;
            }
            long bestRegion = this.bestRegion(world, typeReturn, regions, typesByRegion, reservedWorkers, ruralTypes);
            if (bestRegion == 0) {
                continue;
            }
            this.startNewBuilding(world, countryId, this.ecsConstants.countryTag(), STATE_OWNER, typeReturn, bestRegion, typesByRegion, reservedWorkers, availableFunds, parkLevels,
                pendingOutputs, levelOutputs);
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
                                  LongObjectMap<LongIntMap> typesByRegion, LongFloatMap reservedWorkers, double[] availableFunds, LongObjectMap<LongFloatMap> parkLevels,
                                  float[] pendingOutputs, LongFloatMap levelOutputs) {
        startSite(world, this.ecsConstants, 0, typeReturn.buildingTypeId(), regionInstanceId, countryId, ownerTagId);
        addPendingOutput(world, pendingOutputs, levelOutputs, typeReturn.buildingTypeId());
        this.addLevels(parkLevels, countryId, typeReturn.buildingTypeId(), 1f);
        availableFunds[owner] -= typeReturn.levelCost();
        reservedWorkers.put(regionInstanceId, reservedWorkers.get(regionInstanceId) + typeReturn.workersPerLevel());
        LongIntMap regionTypes = typesByRegion.get(regionInstanceId);
        if (regionTypes == null) {
            regionTypes = this.regionTypesPool.obtain();
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

    private void reset() {
        this.queuedLevels.clear();
        this.reservedWorkers.clear();
        this.typesByRegion.clear();
        this.parkUsedLevels.clear();
        this.parkLevels.clear();
        this.sitesByType.clear();
        this.candidatesByCountry.clear();
        this.typeReturnsByCountry.clear();
        this.levelOutputs.clear();
        this.outputsByType.clear();
        this.levelsByType.clear();
        this.regionsByCountry.clear();
        this.regionTypesPool.releaseAll();
        this.parkPool.releaseAll();
        this.candidateListPool.releaseAll();
        this.typeReturnMapPool.releaseAll();
        this.regionListPool.releaseAll();
        this.candidatePool.releaseAll();
        this.typeReturnPool.releaseAll();
    }

    private void readRegion(long regionInstanceId) {
        RegionInstanceView regionInstance = this.world.obtainEntityView(regionInstanceId).getView(RegionInstance.class);
        LongList regions = this.regionsByCountry.get(regionInstance.ownerId());
        if (regions == null) {
            regions = this.regionListPool.obtain();
            this.regionsByCountry.put(regionInstance.ownerId(), regions);
        }
        regions.add(regionInstanceId);
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

    private float[] pendingOutputs(World world) {
        float[] pendingOutputs = zeroed(this.pendingOutputs);
        LongIntSlotMap sitesByType = this.sitesByType;
        for (int slot = 0; slot < sitesByType.slotCount(); slot++) {
            long buildingTypeId = sitesByType.slotKey(slot);
            if (buildingTypeId == 0) {
                continue;
            }
            EconomyBuildingTypeView buildingType = world.obtainEntityView(buildingTypeId).getView(EconomyBuildingType.class);
            pendingOutputs[buildingType.goodOutputIndex()] += sitesByType.slotValue(slot) * this.levelOutputs.get(buildingTypeId);
        }
        return pendingOutputs;
    }

    private static void addPendingOutput(World world, float[] pendingOutputs, LongFloatMap levelOutputs, long buildingTypeId) {
        EconomyBuildingTypeView buildingType = world.obtainEntityView(buildingTypeId).getView(EconomyBuildingType.class);
        pendingOutputs[buildingType.goodOutputIndex()] += levelOutputs.get(buildingTypeId);
    }

    private void addLevels(LongObjectMap<LongFloatMap> park, long countryId, long buildingTypeId, float levels) {
        LongFloatMap countryPark = park.get(countryId);
        if (countryPark == null) {
            countryPark = this.parkPool.obtain();
            park.put(countryId, countryPark);
        }
        countryPark.put(buildingTypeId, countryPark.get(buildingTypeId) + levels);
    }

    private void readSite(long siteId) {
        World world = this.world;
        LongIntMap queuedLevels = this.queuedLevels;
        LongFloatMap reservedWorkers = this.reservedWorkers;
        LongObjectMap<LongIntMap> typesByRegion = this.typesByRegion;
        LongIntSlotMap sitesByType = this.sitesByType;
        ConstructionSiteView site = world.obtainEntityView(siteId).getView(ConstructionSite.class);
        long buildingId = site.buildingId();
        long regionInstanceId = site.regionInstanceId();
        EntityView buildingType = world.obtainEntityView(site.buildingTypeId());
        if (buildingId != 0) {
            queuedLevels.put(buildingId, queuedLevels.get(buildingId) + 1);
        } else {
            LongIntMap types = typesByRegion.get(regionInstanceId);
            if (types == null) {
                types = this.regionTypesPool.obtain();
                typesByRegion.put(regionInstanceId, types);
            }
            types.put(site.buildingTypeId(), 1);
        }
        if (buildingType.has(EconomyBuildingType.class)) {
            this.addLevels(this.parkLevels, site.countryId(), site.buildingTypeId(), 1f);
            sitesByType.put(site.buildingTypeId(), sitesByType.get(site.buildingTypeId()) + 1);
            EconomyBuildingTypeView typeData = buildingType.getView(EconomyBuildingType.class);
            reservedWorkers.put(regionInstanceId, reservedWorkers.get(regionInstanceId) + typeData.workforce() * typeData.primaryWorkerPopTypeRatio());
        }
    }

    private void collectCandidates() {
        this.buildingQuery.iter(this.buildingReader);
        this.rgoQuery.iter(this.rgoReader);
        LongFloatSlotMap levelsByType = this.levelsByType;
        for (int slot = 0; slot < levelsByType.slotCount(); slot++) {
            long buildingTypeId = levelsByType.slotKey(slot);
            if (buildingTypeId == 0) {
                continue;
            }
            float levels = levelsByType.slotValue(slot);
            if (levels > 0f) {
                this.levelOutputs.put(buildingTypeId, this.outputsByType.get(buildingTypeId) / levels);
            }
        }
    }

    private void readBuildings(Iter buildingIter) {
        World world = this.world;
        LongIntMap queuedLevels = this.queuedLevels;
        LongObjectMap<LongIntMap> typesByRegion = this.typesByRegion;
        LongObjectMap<ObjectList<Candidate>> candidatesByCountry = this.candidatesByCountry;
        LongObjectMap<LongObjectSlotMap<TypeReturn>> typeReturnsByCountry = this.typeReturnsByCountry;
        LongObjectMap<LongFloatMap> parkUsedLevels = this.parkUsedLevels;
        LongObjectMap<LongFloatMap> parkLevels = this.parkLevels;
        LongFloatMap outputsByType = this.outputsByType;
        LongFloatSlotMap levelsByType = this.levelsByType;
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
                regionTypes = this.regionTypesPool.obtain();
                typesByRegion.put(building.parentId(), regionTypes);
            }
            regionTypes.put(buildingTypeId, 1);
            this.addLevels(parkUsedLevels, countryId, buildingTypeId, economyBuilding.utilization() * building.size());
            this.addLevels(parkLevels, countryId, buildingTypeId, building.size());
            outputsByType.put(buildingTypeId, outputsByType.get(buildingTypeId) + economyBuilding.production());
            levelsByType.put(buildingTypeId, levelsByType.get(buildingTypeId) + building.size());
            int freeLevels = maxLevel - building.size() - queuedLevels.get(buildingId);
            if (freeLevels <= 0 || levelCost <= 0.0) {
                continue;
            }

            float annualReturn = (float) (economyBuilding.profitTrend() / building.size() * DAYS_PER_YEAR / levelCost);
            LongObjectSlotMap<TypeReturn> typeReturns = typeReturnsByCountry.get(countryId);
            if (typeReturns == null) {
                typeReturns = this.typeReturnMapPool.obtain();
                typeReturnsByCountry.put(countryId, typeReturns);
            }
            TypeReturn typeReturn = typeReturns.get(buildingTypeId);
            if (typeReturn == null) {
                typeReturns.put(buildingTypeId, this.typeReturnPool.obtain().set(buildingTypeId, annualReturn, levelCost, workersPerLevel, primaryWorkerPopTypeIndex));
            } else if (annualReturn > typeReturn.annualReturn()) {
                typeReturn.set(buildingTypeId, annualReturn, levelCost, workersPerLevel, primaryWorkerPopTypeIndex);
            }
            int owner = ownerIndex(economyBuilding.ownerTagId(), this.ecsConstants);
            boolean qualifies = qualifies(owner, annualReturn, lendingRate);
            if (!qualifies) {
                continue;
            }

            ObjectList<Candidate> candidates = candidatesByCountry.get(countryId);
            if (candidates == null) {
                candidates = this.candidateListPool.obtain();
                candidatesByCountry.put(countryId, candidates);
            }
            candidates.add(this.candidatePool.obtain().set(buildingId, buildingTypeId, building.parentId(), countryId, economyBuilding.ownerTagId(), owner,
                primaryWorkerPopTypeIndex, workersPerLevel, freeLevels, levelCost, annualReturn));
        }
    }

    /**
     * Mechanization levels of RGOs (decision T14 e, f): below the country's cap, all available workers of the type employed,
     * return = (extra output x price - upkeep) x share of the investing group in the RGO profit / level cost.
     */
    private void readRgos(Iter rgoIter) {
        World world = this.world;
        Field<Province> provinceField = rgoIter.field(Province.class, 0);
        Field<ResourceGathering> rgoField = rgoIter.field(ResourceGathering.class, 1);
        Field<Demographics> demographicsField = rgoIter.field(Demographics.class, 2);
        ResourceGatheringTypeView rgoType = rgoIter.field(ResourceGatheringType.class, 3).getView(0);
        int workerIndex = rgoType.workerPopTypeIndex();
        float baseShare = this.ecsConstants.baseWorkforceShares()[workerIndex];
        float[] goodCosts = this.ecsConstants.goodCosts();
        int[] costGoods = this.ecsConstants.mechanizationCostGoods();
        int[] upkeepGoods = this.ecsConstants.mechanizationUpkeepGoods();
        long countryId = 0;
        CountryProductionTechnologyView production = null;
        CountryMarketView countryMarket = null;
        float lendingRate = 0f;
        long ownerTagId = 0;
        int owner = 0;
        float ownerShare = 0f;
        float workforceShare = 0f;
        double costPerBaseValue = 0.0;
        double upkeepPerBaseValue = 0.0;
        for (int i = 0; i < rgoIter.count(); i++) {
            ProvinceView province = provinceField.getMutView(i);
            if (province.ownerId() != countryId) {
                countryId = province.ownerId();
                EntityView country = world.obtainEntityView(countryId);
                production = country.getView(CountryProductionTechnology.class);
                countryMarket = country.getView(CountryMarket.class);
                lendingRate = country.<CountryBankView>getView(CountryBank.class).lendingRate();
                CountryProfitDistributionPolicyView profitPolicy = country.getView(CountryProfitDistributionPolicy.class);
                float capitalistShare = profitPolicy.capitalistProfitShareRate();
                float workerShare = profitPolicy.workerProfitShareRate();
                float stateShare = capitalistShare > 0f ? 0f : profitPolicy.stateProfitShareRate();
                float total = capitalistShare + workerShare + stateShare + profitPolicy.aristocratProfitShareRate();
                ownerTagId = ownerTag(capitalistShare, workerShare, stateShare, this.ecsConstants);
                owner = ownerIndex(ownerTagId, this.ecsConstants);
                float share = owner == CAPITALIST_OWNER ? capitalistShare : owner == WORKERS_OWNER ? workerShare : stateShare;
                ownerShare = total > 0f ? share / total : 1f;
                workforceShare = EmploymentUtils.activeShare(baseShare, country.<CountryPopulationPolicyView>getView(CountryPopulationPolicy.class).workforceShareModifier());
                costPerBaseValue = 0.0;
                for (int k = 0; k < costGoods.length; k++) {
                    costPerBaseValue += RGO_MECHANIZATION_COST_SHARES[k] * countryMarket.goodBudgetPrices(costGoods[k]) / goodCosts[costGoods[k]];
                }
                upkeepPerBaseValue = 0.0;
                for (int k = 0; k < upkeepGoods.length; k++) {
                    upkeepPerBaseValue += RGO_MECHANIZATION_UPKEEP_SHARES[k] * countryMarket.goodBudgetPrices(upkeepGoods[k]) / goodCosts[upkeepGoods[k]];
                }
            }
            ResourceGatheringView rgo = rgoField.getMutView(i);
            int good = rgo.goodIndex();
            long provinceId = rgoIter.entity(i);
            int freeLevels = production.mechanizationCaps(good) - rgo.mechanization() - this.queuedLevels.get(provinceId);
            if (freeLevels <= 0 || rgo.production() <= 0f || ownerShare <= 0f) {
                continue;
            }
            DemographicsView demographics = demographicsField.getMutView(i);
            int activeWorkers = EmploymentUtils.activeWorkers(demographics.totalByPopType(workerIndex), workforceShare);
            int maximumWorkers = Math.min((int) (rgo.size() * rgoType.workforce() * rgoType.workerPopTypeRatio()), activeWorkers);
            if (maximumWorkers <= 0 || rgo.workerAmount() < maximumWorkers) {
                continue;
            }
            float extraDaily = rgo.production() / Math.max(1e-6f, rgo.efficiency()) * RGO_MECHANIZATION_EFFICIENCY;
            double baseValue = (double) extraDaily * DAYS_PER_YEAR * goodCosts[good];
            double levelCost = baseValue * costPerBaseValue;
            if (levelCost <= 0.0) {
                continue;
            }
            double upkeepDaily = baseValue * RGO_MECHANIZATION_UPKEEP_RATE / DAYS_PER_YEAR * upkeepPerBaseValue;
            double grossAnnual = ((double) extraDaily * countryMarket.goodPrices(good) - upkeepDaily) * DAYS_PER_YEAR;
            float annualReturn = (float) (grossAnnual * ownerShare / levelCost);
            if (!qualifies(owner, annualReturn, lendingRate)) {
                continue;
            }
            ObjectList<Candidate> candidates = this.candidatesByCountry.get(countryId);
            if (candidates == null) {
                candidates = this.candidateListPool.obtain();
                this.candidatesByCountry.put(countryId, candidates);
            }
            candidates.add(this.candidatePool.obtain().set(provinceId, this.ecsConstants.rgoMechanization(), province.regionInstanceId(), countryId, ownerTagId, owner,
                workerIndex, 0f, freeLevels, levelCost, annualReturn).mechanization(baseValue));
        }
    }

    private float availableWorkers(World world, Candidate candidate, boolean[] ruralTypes) {
        return this.availableWorkers(world, candidate.regionInstanceId(), candidate.primaryWorkerPopTypeIndex(), ruralTypes);
    }

    private float availableWorkers(World world, long regionInstanceId, int primaryWorkerPopTypeIndex, boolean[] ruralTypes) {
        EntityView regionInstanceEntity = world.obtainEntityView(regionInstanceId);
        RegionDemographicsView regionDemographics = regionInstanceEntity.getView(RegionDemographics.class);
        RegionInstanceView regionInstance = regionInstanceEntity.getView(RegionInstance.class);
        CountryPopulationPolicyView populationPolicy = world.obtainEntityView(regionInstance.ownerId()).getView(CountryPopulationPolicy.class);
        float[] baseWorkforceShares = this.ecsConstants.baseWorkforceShares();
        float availableWorkers = 0f;
        for (int p = 0; p < POP_TYPE_COUNT; p++) {
            if (p == primaryWorkerPopTypeIndex || ruralTypes[p]) {
                availableWorkers += Math.max(0f, regionDemographics.totalByPopType(p) * EmploymentUtils.activeShare(baseWorkforceShares[p], populationPolicy.workforceShareModifier()) - regionDemographics.employmentByPopType(p));
            }
        }
        return availableWorkers;
    }
}
