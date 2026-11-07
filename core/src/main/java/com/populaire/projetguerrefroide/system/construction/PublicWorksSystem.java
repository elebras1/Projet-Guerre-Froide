package com.populaire.projetguerrefroide.system.construction;

import com.github.tommyettinger.ds.LongFloatMap;
import com.github.tommyettinger.ds.LongIntMap;
import com.github.tommyettinger.ds.LongLongMap;
import com.github.tommyettinger.ds.LongObjectMap;
import com.github.tommyettinger.ds.ObjectList;
import io.github.elebras1.flecs.EntityView;
import io.github.elebras1.flecs.Field;
import io.github.elebras1.flecs.Iter;
import io.github.elebras1.flecs.Query;
import io.github.elebras1.flecs.World;
import io.github.elebras1.flecs.callback.EntityCallback;
import io.github.elebras1.flecs.callback.IterCallback;
import com.populaire.projetguerrefroide.component.*;
import com.populaire.projetguerrefroide.simulation.EcsConstants;
import com.populaire.projetguerrefroide.system.building.InfrastructureUtils;
import com.populaire.projetguerrefroide.system.military.MilitaryUtils;
import com.populaire.projetguerrefroide.system.population.PopulationMobilityUtils;
import com.populaire.projetguerrefroide.system.state.StateBudgetUtils;
import com.populaire.projetguerrefroide.system.state.StateConsumptionUtils;
import com.populaire.projetguerrefroide.util.collection.ObjectPool;

import java.util.Comparator;

import static com.populaire.projetguerrefroide.simulation.Constants.*;
import static com.populaire.projetguerrefroide.system.construction.ConstructionUtils.levelCost;
import static com.populaire.projetguerrefroide.system.construction.ConstructionUtils.startSite;
import static com.populaire.projetguerrefroide.system.building.EconomyBuildingUtils.infrastructureBonus;
import static com.populaire.projetguerrefroide.system.state.StateBudgetUtils.stateInterestRate;

public class PublicWorksSystem {

    private static final Comparator<Candidate> CANDIDATE_ORDER = Comparator.comparingDouble((Candidate candidate) -> candidate.annualReturn()).reversed();

    private static final class Candidate {
        private long buildingId;
        private long buildingTypeId;
        private long regionInstanceId;
        private float annualReturn;

        private Candidate set(long buildingId, long buildingTypeId, long regionInstanceId, float annualReturn) {
            this.buildingId = buildingId;
            this.buildingTypeId = buildingTypeId;
            this.regionInstanceId = regionInstanceId;
            this.annualReturn = annualReturn;
            return this;
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

        private float annualReturn() {
            return this.annualReturn;
        }
    }

    private static final class StorageBuilding {
        private long buildingId;
        private int size;
        private long regionInstanceId;

        private StorageBuilding set(long buildingId, int size, long regionInstanceId) {
            this.buildingId = buildingId;
            this.size = size;
            this.regionInstanceId = regionInstanceId;
            return this;
        }

        private long buildingId() {
            return this.buildingId;
        }

        private int size() {
            return this.size;
        }

        private long regionInstanceId() {
            return this.regionInstanceId;
        }
    }

    private static final class BaseBuilding {
        private long buildingId;
        private int size;
        private long regionInstanceId;
        private boolean capital;

        private BaseBuilding set(long buildingId, int size, long regionInstanceId, boolean capital) {
            this.buildingId = buildingId;
            this.size = size;
            this.regionInstanceId = regionInstanceId;
            this.capital = capital;
            return this;
        }

        private long buildingId() {
            return this.buildingId;
        }

        private int size() {
            return this.size;
        }

        private long regionInstanceId() {
            return this.regionInstanceId;
        }

        private boolean capital() {
            return this.capital;
        }
    }

    private final EcsConstants ecsConstants;
    private final Query developmentQuery;
    private final Query factoryQuery;
    private final Query siteQuery;
    private final Query specialQuery;
    private final Query regionQuery;
    private final LongIntMap queuedLevels = new LongIntMap();
    private final LongFloatMap dailyCommitments = new LongFloatMap();
    private final LongIntMap sitesInProgress = new LongIntMap();
    private final LongIntMap storageSites = new LongIntMap();
    private final LongIntMap navalBaseSites = new LongIntMap();
    private final LongIntMap airBaseSites = new LongIntMap();
    private final LongIntMap academySites = new LongIntMap();
    private final LongObjectMap<ObjectList<Candidate>> candidatesByCountry = new LongObjectMap<>();
    private final LongObjectMap<ObjectList<StorageBuilding>> storageBuildings = new LongObjectMap<>();
    private final LongIntMap storageRegions = new LongIntMap();
    private final LongLongMap largestFreeRegions = new LongLongMap();
    private final LongObjectMap<BaseBuilding> navalBases = new LongObjectMap<>();
    private final LongObjectMap<BaseBuilding> airBases = new LongObjectMap<>();
    private final LongObjectMap<StorageBuilding> academies = new LongObjectMap<>();
    private final LongIntMap academyRegions = new LongIntMap();
    private final LongFloatMap factoryValues = new LongFloatMap();
    private final LongFloatMap largestAdults = new LongFloatMap();
    private final long[] baseTypeIds = new long[2];
    private final float[] coverages = new float[2];
    private final BaseBuilding[] bases = new BaseBuilding[2];
    private final int[] baseSitesInProgress = new int[2];
    private final ObjectPool<Candidate> candidatePool = new ObjectPool<>(Candidate::new, candidate -> {
    });
    private final ObjectPool<StorageBuilding> storageBuildingPool = new ObjectPool<>(StorageBuilding::new, building -> {
    });
    private final ObjectPool<BaseBuilding> baseBuildingPool = new ObjectPool<>(BaseBuilding::new, building -> {
    });
    private final ObjectPool<ObjectList<Candidate>> candidateListPool = new ObjectPool<>(ObjectList::new, ObjectList::clear);
    private final ObjectPool<ObjectList<StorageBuilding>> storageListPool = new ObjectPool<>(ObjectList::new, ObjectList::clear);
    private final EntityCallback siteReader = this::readSite;
    private final IterCallback developmentReader = this::readDevelopment;
    private final IterCallback baseReader = this::readBase;
    private final IterCallback academyReader = this::readAcademy;
    private final EntityCallback academySiteReader = this::readAcademySite;
    private final IterCallback storageReader = this::readStorage;
    private final EntityCallback storageSiteReader = this::readStorageSite;
    private final IterCallback freeRegionReader = this::readFreeRegion;
    private final IterCallback factoryReader = this::readFactory;
    private World world;
    private long storageTypeId;
    private long navalBaseTypeId;
    private long airBaseTypeId;
    private long academyTypeId;
    private int academyMaxLevel;

    public PublicWorksSystem(World ecsWorld, EcsConstants ecsConstants, long phaseId) {
        this.ecsConstants = ecsConstants;
        this.developmentQuery = ecsWorld.query()
            .with(Building.class)
            .with(DevelopmentBuildingType.class)
            .build();
        this.factoryQuery = ecsWorld.query()
            .with(Building.class)
            .with(EconomyBuilding.class)
            .build();
        this.siteQuery = ecsWorld.query().with(ConstructionSite.class).build();
        this.specialQuery = ecsWorld.query()
            .with(Building.class)
            .with(SpecialBuilding.class)
            .with(SpecialBuildingType.class)
            .build();
        this.regionQuery = ecsWorld.query()
            .with(RegionInstance.class)
            .with(RegionDemographics.class)
            .build();
        ecsWorld.system("PublicWorksSystem")
            .kind(phaseId)
            .with(CountryPublicWorks.class)
            .with(CountryBudget.class)
            .with(CountryBank.class)
            .with(CountryMarket.class)
            .with(CountryProductionPolicy.class)
            .run(this::plan);
    }

    private void plan(Iter iter) {
        World world = iter.world();
        this.world = world;
        this.reset();
        boolean planningDay = PopulationMobilityUtils.isMobilityDay(world, this.ecsConstants);
        LongFloatMap dailyCommitments = this.dailyCommitments;
        LongIntMap sitesInProgress = this.sitesInProgress;
        EntityView globalStorage = world.obtainEntityView(this.ecsConstants.globalStrategicStorage());
        long storageTypeId = globalStorage.has(GlobalStrategicStorage.class) ? globalStorage.<GlobalStrategicStorageView>getView(GlobalStrategicStorage.class).buildingTypeId() : 0;
        LongIntMap storageSites = this.storageSites;
        EntityView globalBases = world.obtainEntityView(this.ecsConstants.globalMilitaryFacilities());
        GlobalMilitaryFacilitiesView militaryBases = globalBases.has(GlobalMilitaryFacilities.class) ? globalBases.getView(GlobalMilitaryFacilities.class) : null;
        long navalBaseTypeId = militaryBases != null ? militaryBases.navalBaseTypeId() : 0;
        long airBaseTypeId = militaryBases != null ? militaryBases.airBaseTypeId() : 0;
        LongIntMap navalBaseSites = this.navalBaseSites;
        LongIntMap airBaseSites = this.airBaseSites;
        long academyTypeId = militaryBases != null ? militaryBases.academyTypeId() : 0;
        int academyStaffIndex = militaryBases != null ? militaryBases.academyStaffPopTypeIndex() : -1;
        LongIntMap academySites = this.academySites;
        this.storageTypeId = storageTypeId;
        this.navalBaseTypeId = navalBaseTypeId;
        this.airBaseTypeId = airBaseTypeId;
        this.academyTypeId = academyTypeId;
        this.siteQuery.each(this.siteReader);
        LongObjectMap<ObjectList<Candidate>> candidatesByCountry = this.candidatesByCountry;
        LongObjectMap<ObjectList<StorageBuilding>> storageBuildings = this.storageBuildings;
        LongIntMap storageRegions = this.storageRegions;
        LongLongMap largestFreeRegions = this.largestFreeRegions;
        LongObjectMap<BaseBuilding> navalBases = this.navalBases;
        LongObjectMap<BaseBuilding> airBases = this.airBases;
        LongObjectMap<StorageBuilding> academies = this.academies;
        LongIntMap academyRegions = this.academyRegions;
        if (planningDay) {
            this.collectCandidates();
            this.collectBases();
            if (academyTypeId != 0) {
                this.collectAcademies();
            }
            if (storageTypeId != 0) {
                this.collectStorage();
            }
        }

        while (iter.next()) {
            Field<CountryPublicWorks> countryPublicWorksField = iter.field(CountryPublicWorks.class, 0);
            Field<CountryBudget> countryBudgetField = iter.field(CountryBudget.class, 1);
            Field<CountryBank> countryBankField = iter.field(CountryBank.class, 2);
            Field<CountryMarket> countryMarketField = iter.field(CountryMarket.class, 3);
            Field<CountryProductionPolicy> countryProductionPolicyField = iter.field(CountryProductionPolicy.class, 4);
            for (int i = 0; i < iter.count(); i++) {
                long countryId = iter.entity(i);
                CountryPublicWorksMutView countryPublicWorks = countryPublicWorksField.getMutView(i);
                CountryBudgetMutView countryBudget = countryBudgetField.getMutView(i);
                CountryBankMutView countryBank = countryBankField.getMutView(i);
                CountryMarketMutView countryMarket = countryMarketField.getMutView(i);
                CountryProductionPolicyView productionPolicy = countryProductionPolicyField.getMutView(i);
                double dailyCommitment = dailyCommitments.get(countryId);
                int sites = sitesInProgress.get(countryId);
                double borrowed = 0.0;

                ObjectList<Candidate> candidates = candidatesByCountry.get(countryId);
                if (planningDay) {
                    double ownFunds = countryBudget.investableSurplus();
                    double stateLoans = countryBank.loans(STATE_OWNER);
                    float stateRate = (float) stateInterestRate(stateLoans, countryBank.creditLimit(), countryBank.lendingRate());
                    double headroom = Math.max(0.0, Math.min(countryBank.reserves(), countryBank.creditLimit() - stateLoans));
                    double monthlyShare = Math.min(1.0, PUBLIC_WORKS_RATE * Math.max(0f, 1f + productionPolicy.publicWorksModifier()) * DAYS_PER_MONTH);
                    double ownBudget = monthlyShare * ownFunds;
                    double loanBudget = monthlyShare * headroom;
                    float bestReturn = 0f;
                    CountryMilitaryView countryMilitary = world.obtainEntityView(countryId).getView(CountryMilitary.class);
                    long[] baseTypeIds = this.baseTypeIds;
                    baseTypeIds[0] = navalBaseTypeId;
                    baseTypeIds[1] = airBaseTypeId;
                    float[] coverages = this.coverages;
                    coverages[0] = MilitaryUtils.coverage(countryMilitary.navalCapacity(), countryMilitary.navalLoad());
                    coverages[1] = MilitaryUtils.coverage(countryMilitary.airCapacity(), countryMilitary.airLoad());
                    BaseBuilding[] bases = this.bases;
                    bases[0] = navalBases.get(countryId);
                    bases[1] = airBases.get(countryId);
                    int[] baseSitesInProgress = this.baseSitesInProgress;
                    baseSitesInProgress[0] = navalBaseSites.get(countryId);
                    baseSitesInProgress[1] = airBaseSites.get(countryId);
                    for (int b = 0; b < baseTypeIds.length; b++) {
                        if (baseTypeIds[b] == 0 || coverages[b] >= 1f || bases[b] == null || baseSitesInProgress[b] > 0) {
                            continue;
                        }
                        double baseCost = this.planBase(world, countryId, baseTypeIds[b], bases[b], countryBudget, countryMarket, ownBudget);
                        if (baseCost > 0.0) {
                            DevelopmentBuildingTypeView baseType = world.obtainEntityView(baseTypeIds[b]).getView(DevelopmentBuildingType.class);
                            int duration = Math.max(1, (int) (baseType.time() * (1f - productionPolicy.constructionSpeed())));
                            ownBudget -= baseCost;
                            ownFunds -= baseCost;
                            dailyCommitment += baseCost / duration;
                            sites++;
                        }
                    }
                    if (academyTypeId != 0 && academySites.get(countryId) == 0 && MilitaryUtils.trainingCoverage(countryMilitary, academyStaffIndex) < 1f) {
                        double academyCost = this.planAcademy(world, countryId, academyTypeId, countryBudget, countryMarket, ownBudget, academies.get(countryId), academyRegions);
                        if (academyCost > 0.0) {
                            SpecialBuildingTypeView academyType = world.obtainEntityView(academyTypeId).getView(SpecialBuildingType.class);
                            int duration = Math.max(1, (int) (academyType.time() * (1f - productionPolicy.constructionSpeed())));
                            ownBudget -= academyCost;
                            ownFunds -= academyCost;
                            dailyCommitment += academyCost / duration;
                            sites++;
                        }
                    }
                    if (candidates != null) {
                        candidates.sort(CANDIDATE_ORDER);
                        bestReturn = candidates.get(0).annualReturn();
                        for (int c = 0; c < candidates.size(); c++) {
                            Candidate candidate = candidates.get(c);
                            DevelopmentBuildingTypeView buildingType = world.obtainEntityView(candidate.buildingTypeId()).getView(DevelopmentBuildingType.class);
                            int duration = Math.max(1, (int) (buildingType.time() * (1f - productionPolicy.constructionSpeed())));
                            double cost = levelCost(world, candidate.buildingTypeId(), countryMarket);
                            double shortfall = Math.max(0.0, cost - ownBudget);
                            if (shortfall > 0.0 && (candidate.annualReturn() < stateRate || shortfall > loanBudget)) {
                                break;
                            }
                            if (shortfall > 0.0) {
                                countryBank.reserves(countryBank.reserves() - shortfall);
                                countryBank.loans(STATE_OWNER, countryBank.loans(STATE_OWNER) + shortfall);
                                countryBank.borrowed(STATE_OWNER, countryBank.borrowed(STATE_OWNER) + shortfall);
                                countryMarket.treasury(countryMarket.treasury() + shortfall);
                                loanBudget -= shortfall;
                                borrowed += shortfall;
                            }
                            ownBudget -= cost - shortfall;
                            ownFunds -= cost - shortfall;
                            startSite(world, this.ecsConstants, candidate.buildingId(), candidate.buildingTypeId(), candidate.regionInstanceId(), countryId, this.ecsConstants.countryTag());
                            dailyCommitment += cost / duration;
                            sites++;
                        }
                    }
                    if (storageTypeId != 0 && storageSites.get(countryId) == 0) {
                        double storageCost = this.planStorage(world, countryId, storageTypeId, countryBudget, countryMarket, ownBudget,
                            storageBuildings.get(countryId), storageRegions, largestFreeRegions.get(countryId));
                        if (storageCost > 0.0) {
                            SpecialBuildingTypeView storageType = world.obtainEntityView(storageTypeId).getView(SpecialBuildingType.class);
                            int duration = Math.max(1, (int) (storageType.time() * (1f - productionPolicy.constructionSpeed())));
                            ownBudget -= storageCost;
                            ownFunds -= storageCost;
                            dailyCommitment += storageCost / duration;
                            sites++;
                        }
                    }
                    countryPublicWorks.targetReturn(bestReturn);
                    countryBudget.investableSurplus(Math.max(0.0, ownFunds));
                }
                countryPublicWorks.sitesInProgress(sites);
                countryPublicWorks.dailyCommitment(dailyCommitment);
                countryBudget.publicWorksLoan((float) borrowed);
            }
        }
    }

    private void reset() {
        this.queuedLevels.clear();
        this.dailyCommitments.clear();
        this.sitesInProgress.clear();
        this.storageSites.clear();
        this.navalBaseSites.clear();
        this.airBaseSites.clear();
        this.academySites.clear();
        this.candidatesByCountry.clear();
        this.storageBuildings.clear();
        this.storageRegions.clear();
        this.largestFreeRegions.clear();
        this.navalBases.clear();
        this.airBases.clear();
        this.academies.clear();
        this.academyRegions.clear();
        this.candidatePool.releaseAll();
        this.storageBuildingPool.releaseAll();
        this.baseBuildingPool.releaseAll();
        this.candidateListPool.releaseAll();
        this.storageListPool.releaseAll();
    }

    private void readSite(long siteId) {
        World world = this.world;
        long navalBaseTypeId = this.navalBaseTypeId;
        long airBaseTypeId = this.airBaseTypeId;
        long storageTypeId = this.storageTypeId;
        long academyTypeId = this.academyTypeId;
        LongIntMap queuedLevels = this.queuedLevels;
        LongFloatMap dailyCommitments = this.dailyCommitments;
        LongIntMap sitesInProgress = this.sitesInProgress;
        LongIntMap storageSites = this.storageSites;
        LongIntMap navalBaseSites = this.navalBaseSites;
        LongIntMap airBaseSites = this.airBaseSites;
        LongIntMap academySites = this.academySites;
        long countryTag = this.ecsConstants.countryTag();
        ConstructionSiteView site = world.obtainEntityView(siteId).getView(ConstructionSite.class);
        if (navalBaseTypeId != 0 && site.buildingTypeId() == navalBaseTypeId) {
            navalBaseSites.put(site.countryId(), navalBaseSites.get(site.countryId()) + 1);
        } else if (airBaseTypeId != 0 && site.buildingTypeId() == airBaseTypeId) {
            airBaseSites.put(site.countryId(), airBaseSites.get(site.countryId()) + 1);
        }
        boolean storage = storageTypeId != 0 && site.buildingTypeId() == storageTypeId;
        boolean academy = academyTypeId != 0 && site.buildingTypeId() == academyTypeId;
        if (storage) {
            storageSites.put(site.countryId(), storageSites.get(site.countryId()) + 1);
        } else if (academy) {
            academySites.put(site.countryId(), academySites.get(site.countryId()) + 1);
        } else if (site.buildingId() == 0 || !world.obtainEntityView(site.buildingTypeId()).has(DevelopmentBuildingType.class)) {
            return;
        } else {
            queuedLevels.put(site.buildingId(), queuedLevels.get(site.buildingId()) + 1);
        }
        if (site.ownerTagId() != countryTag) {
            return;
        }
        CountryMarketView countryMarket = world.obtainEntityView(site.countryId()).getView(CountryMarket.class);
        double dailyCost = 0.0;
        for (int k = 0; k < site.goodIndexesLength(); k++) {
            int goodIndex = site.goodIndexes(k);
            if (goodIndex < 0) {
                break;
            }
            dailyCost += (double) Math.min(site.goodsLeft(k), site.dailyGoods(k)) * countryMarket.goodBudgetPrices(goodIndex);
        }
        dailyCommitments.put(site.countryId(), dailyCommitments.get(site.countryId()) + (float) dailyCost);
        sitesInProgress.put(site.countryId(), sitesInProgress.get(site.countryId()) + 1);
    }

    private void collectCandidates() {
        this.computeFactoryValues();
        this.developmentQuery.iter(this.developmentReader);
    }

    private void readDevelopment(Iter developmentIter) {
        World world = this.world;
        LongIntMap queuedLevels = this.queuedLevels;
        LongObjectMap<ObjectList<Candidate>> candidatesByCountry = this.candidatesByCountry;
        LongFloatMap factoryValues = this.factoryValues;
        DevelopmentBuildingTypeView buildingType = developmentIter.field(DevelopmentBuildingType.class, 1).getView(0);
        long buildingTypeId = developmentIter.src(1);
        float productionModifier = InfrastructureUtils.productionModifier(world, buildingTypeId);
        if (productionModifier <= 0f) {
            return;
        }
        Field<Building> buildingField = developmentIter.field(Building.class, 0);
        for (int j = 0; j < developmentIter.count(); j++) {
            BuildingView building = buildingField.getMutView(j);
            long buildingId = developmentIter.entity(j);
            if (building.countryId() == 0 || building.size() >= buildingType.maxLevel() || queuedLevels.get(buildingId) > 0) {
                continue;
            }
            double baseValue = baseValue(world, factoryValues, building);
            if (baseValue <= 0.0) {
                continue;
            }
            float annualReturn = annualReturn(world, building, buildingType, productionModifier, baseValue);
            ObjectList<Candidate> candidates = candidatesByCountry.get(building.countryId());
            if (candidates == null) {
                candidates = this.candidateListPool.obtain();
                candidatesByCountry.put(building.countryId(), candidates);
            }
            ProvinceView province = world.obtainEntityView(building.parentId()).getView(Province.class);
            candidates.add(this.candidatePool.obtain().set(buildingId, buildingTypeId, province.regionInstanceId(), annualReturn));
        }
    }

    private void collectBases() {
        if (this.navalBaseTypeId == 0 && this.airBaseTypeId == 0) {
            return;
        }
        this.developmentQuery.iter(this.baseReader);
    }

    private void readBase(Iter developmentIter) {
        World world = this.world;
        long navalBaseTypeId = this.navalBaseTypeId;
        long airBaseTypeId = this.airBaseTypeId;
        LongIntMap queuedLevels = this.queuedLevels;
        LongObjectMap<BaseBuilding> navalBases = this.navalBases;
        LongObjectMap<BaseBuilding> airBases = this.airBases;
        long buildingTypeId = developmentIter.src(1);
        boolean naval = buildingTypeId == navalBaseTypeId;
        if (!naval && buildingTypeId != airBaseTypeId) {
            return;
        }
        LongObjectMap<BaseBuilding> bases = naval ? navalBases : airBases;
        DevelopmentBuildingTypeView buildingType = developmentIter.field(DevelopmentBuildingType.class, 1).getView(0);
        Field<Building> buildingField = developmentIter.field(Building.class, 0);
        for (int j = 0; j < developmentIter.count(); j++) {
            BuildingView building = buildingField.getMutView(j);
            long buildingId = developmentIter.entity(j);
            if (building.countryId() == 0 || building.size() >= buildingType.maxLevel() || queuedLevels.get(buildingId) > 0) {
                continue;
            }
            boolean capital = world.obtainEntityView(building.countryId()).<CountryView>getView(Country.class).capitalId() == building.parentId();
            BaseBuilding best = bases.get(building.countryId());
            if (best != null && (building.size() < best.size() || building.size() == best.size() && (best.capital() || !capital))) {
                continue;
            }
            ProvinceView province = world.obtainEntityView(building.parentId()).getView(Province.class);
            if (best == null) {
                best = this.baseBuildingPool.obtain();
                bases.put(building.countryId(), best);
            }
            best.set(buildingId, building.size(), province.regionInstanceId(), capital);
        }
    }

    private double planBase(World world, long countryId, long baseTypeId, BaseBuilding base, CountryBudgetView countryBudget, CountryMarketView countryMarket, double ownBudget) {
        double cost = levelCost(world, baseTypeId, countryMarket);
        if (cost <= 0.0 || cost > ownBudget) {
            return 0.0;
        }
        DevelopmentBuildingTypeView baseType = world.obtainEntityView(baseTypeId).getView(DevelopmentBuildingType.class);
        double maintenanceCost = 0.0;
        for (int k = 0; k < baseType.maintenanceGoodIndexesLength(); k++) {
            int goodIndex = baseType.maintenanceGoodIndexes(k);
            if (goodIndex < 0) {
                break;
            }
            maintenanceCost += (double) baseType.maintenanceGoodAmounts(k) / DAYS_PER_YEAR * countryMarket.goodBudgetPrices(goodIndex);
        }
        if (countryBudget.revenueTrend() - countryBudget.reservedNeeds() < maintenanceCost) {
            return 0.0;
        }
        startSite(world, this.ecsConstants, base.buildingId(), baseTypeId, base.regionInstanceId(), countryId, this.ecsConstants.countryTag());
        return cost;
    }

    private void collectAcademies() {
        SpecialBuildingTypeView academyType = this.world.obtainEntityView(this.academyTypeId).getView(SpecialBuildingType.class);
        this.academyMaxLevel = academyType.maxLevel();
        this.specialQuery.iter(this.academyReader);
        this.siteQuery.each(this.academySiteReader);
    }

    private void readAcademy(Iter specialIter) {
        World world = this.world;
        long academyTypeId = this.academyTypeId;
        int maxLevel = this.academyMaxLevel;
        LongObjectMap<StorageBuilding> academies = this.academies;
        LongIntMap academyRegions = this.academyRegions;
        if (specialIter.src(2) != academyTypeId) {
            return;
        }
        Field<Building> buildingField = specialIter.field(Building.class, 0);
        for (int j = 0; j < specialIter.count(); j++) {
            BuildingView building = buildingField.getMutView(j);
            if (building.countryId() == 0) {
                continue;
            }
            academyRegions.put(building.parentId(), 1);
            if (building.size() >= maxLevel) {
                continue;
            }
            EntityView country = world.obtainEntityView(building.countryId());
            boolean capital = capitalRegion(world, country, building.countryId()) == building.parentId();
            StorageBuilding best = academies.get(building.countryId());
            boolean bestCapital = best != null && capitalRegion(world, country, building.countryId()) == best.regionInstanceId();
            if (best != null && (building.size() < best.size() || building.size() == best.size() && (bestCapital || !capital))) {
                continue;
            }
            if (best == null) {
                best = this.storageBuildingPool.obtain();
                academies.put(building.countryId(), best);
            }
            best.set(specialIter.entity(j), building.size(), building.parentId());
        }
    }

    private void readAcademySite(long siteId) {
        ConstructionSiteView site = this.world.obtainEntityView(siteId).getView(ConstructionSite.class);
        if (site.buildingTypeId() == this.academyTypeId && site.buildingId() == 0) {
            this.academyRegions.put(site.regionInstanceId(), 1);
        }
    }

    private double planAcademy(World world, long countryId, long academyTypeId, CountryBudgetView countryBudget, CountryMarketView countryMarket, double ownBudget,
                               StorageBuilding academy, LongIntMap academyRegions) {
        EntityView country = world.obtainEntityView(countryId);
        long buildingId = 0;
        long regionInstanceId;
        if (academy != null) {
            buildingId = academy.buildingId();
            regionInstanceId = academy.regionInstanceId();
        } else {
            regionInstanceId = capitalRegion(world, country, countryId);
            if (regionInstanceId == 0 || academyRegions.get(regionInstanceId) > 0) {
                return 0.0;
            }
        }
        double cost = levelCost(world, academyTypeId, countryMarket);
        if (cost <= 0.0 || cost > ownBudget) {
            return 0.0;
        }
        SpecialBuildingTypeView academyType = world.obtainEntityView(academyTypeId).getView(SpecialBuildingType.class);
        if (countryBudget.revenueTrend() - countryBudget.reservedNeeds() < this.operatingCost(world, country, academyType, countryBudget, countryMarket)) {
            return 0.0;
        }
        if (buildingId == 0) {
            academyRegions.put(regionInstanceId, 1);
        }
        startSite(world, this.ecsConstants, buildingId, academyTypeId, regionInstanceId, countryId, this.ecsConstants.countryTag());
        return cost;
    }

    private void collectStorage() {
        this.specialQuery.iter(this.storageReader);
        this.siteQuery.each(this.storageSiteReader);
        this.largestAdults.clear();
        this.regionQuery.iter(this.freeRegionReader);
    }

    private void readStorage(Iter specialIter) {
        long storageTypeId = this.storageTypeId;
        LongObjectMap<ObjectList<StorageBuilding>> storageBuildings = this.storageBuildings;
        LongIntMap storageRegions = this.storageRegions;
        if (specialIter.src(2) != storageTypeId) {
            return;
        }
        Field<Building> buildingField = specialIter.field(Building.class, 0);
        for (int j = 0; j < specialIter.count(); j++) {
            BuildingView building = buildingField.getMutView(j);
            if (building.countryId() == 0) {
                continue;
            }
            ObjectList<StorageBuilding> buildings = storageBuildings.get(building.countryId());
            if (buildings == null) {
                buildings = this.storageListPool.obtain();
                storageBuildings.put(building.countryId(), buildings);
            }
            buildings.add(this.storageBuildingPool.obtain().set(specialIter.entity(j), building.size(), building.parentId()));
            storageRegions.put(building.parentId(), 1);
        }
    }

    private void readStorageSite(long siteId) {
        ConstructionSiteView site = this.world.obtainEntityView(siteId).getView(ConstructionSite.class);
        if (site.buildingTypeId() == this.storageTypeId && site.buildingId() == 0) {
            this.storageRegions.put(site.regionInstanceId(), 1);
        }
    }

    private void readFreeRegion(Iter regionIter) {
        LongIntMap storageRegions = this.storageRegions;
        LongFloatMap largestAdults = this.largestAdults;
        LongLongMap largestFreeRegions = this.largestFreeRegions;
        Field<RegionInstance> regionInstanceField = regionIter.field(RegionInstance.class, 0);
        Field<RegionDemographics> regionDemographicsField = regionIter.field(RegionDemographics.class, 1);
        for (int j = 0; j < regionIter.count(); j++) {
            long regionInstanceId = regionIter.entity(j);
            if (storageRegions.get(regionInstanceId) > 0) {
                continue;
            }
            RegionInstanceView regionInstance = regionInstanceField.getMutView(j);
            RegionDemographicsView regionDemographics = regionDemographicsField.getMutView(j);
            long ownerId = regionInstance.ownerId();
            float adults = regionDemographics.totalAdults();
            if (ownerId != 0 && adults > largestAdults.getOrDefault(ownerId, 0f)) {
                largestAdults.put(ownerId, adults);
                largestFreeRegions.put(ownerId, regionInstanceId);
            }
        }
    }

    private double planStorage(World world, long countryId, long storageTypeId, CountryBudgetView countryBudget, CountryMarketView countryMarket, double ownBudget,
                               ObjectList<StorageBuilding> buildings, LongIntMap storageRegions, long largestFreeRegion) {
        GlobalStrategicStorageView strategicStorage = world.obtainEntityView(this.ecsConstants.globalStrategicStorage()).getView(GlobalStrategicStorage.class);
        SpecialBuildingTypeView storageType = world.obtainEntityView(storageTypeId).getView(SpecialBuildingType.class);
        EntityView country = world.obtainEntityView(countryId);
        CountryStrategicStockView stock = country.getView(CountryStrategicStock.class);
        GlobalGoodView globalGood = world.obtainEntityView(this.ecsConstants.globalGood()).getView(GlobalGood.class);
        double consumptionValue = 0.0;
        double stockValue = 0.0;
        boolean cheapGood = false;
        for (int g = 0; g < globalGood.goodIdsLength(); g++) {
            if (!strategicStorage.storedGoods(g)) {
                continue;
            }
            float cost = world.obtainEntityView(globalGood.goodIds(g)).<GoodView>getView(Good.class).cost();
            if (cost <= 0f) {
                continue;
            }
            consumptionValue += (double) Math.max(0f, countryMarket.goodConsumedAmounts(g) - stock.purchasedAmounts(g)) * cost;
            stockValue += (double) stock.stocks(g) * cost;
            if (countryMarket.goodPrices(g) / cost < STORAGE_BUY_PRICE_RATIO) {
                cheapGood = true;
            }
        }
        double builtCapacity = stock.levels() * strategicStorage.capacityPerLevel();
        if (builtCapacity >= STORAGE_TARGET_DAYS * consumptionValue) {
            return 0.0;
        }
        boolean needed = stock.levels() > 0f
            ? stock.capacity() >= STORAGE_FULL_SHARE * builtCapacity && stockValue >= STORAGE_FULL_SHARE * stock.capacity()
            : cheapGood;
        if (!needed) {
            return 0.0;
        }
        double cost = levelCost(world, storageTypeId, countryMarket);
        if (cost <= 0.0 || cost > ownBudget) {
            return 0.0;
        }
        if (countryBudget.revenueTrend() - countryBudget.reservedNeeds() < this.operatingCost(world, country, storageType, countryBudget, countryMarket)) {
            return 0.0;
        }

        long buildingId = 0;
        long regionInstanceId = 0;
        if (buildings != null) {
            for (int b = 0; b < buildings.size(); b++) {
                StorageBuilding building = buildings.get(b);
                if (building.size() < storageType.maxLevel()) {
                    buildingId = building.buildingId();
                    regionInstanceId = building.regionInstanceId();
                    break;
                }
            }
        }
        if (buildingId == 0) {
            regionInstanceId = capitalRegion(world, country, countryId);
            if (regionInstanceId == 0 || storageRegions.get(regionInstanceId) > 0) {
                regionInstanceId = largestFreeRegion;
            }
            if (regionInstanceId == 0) {
                return 0.0;
            }
            storageRegions.put(regionInstanceId, 1);
        }
        startSite(world, this.ecsConstants, buildingId, storageTypeId, regionInstanceId, countryId, this.ecsConstants.countryTag());
        return cost;
    }

    private static long capitalRegion(World world, EntityView country, long countryId) {
        long capitalId = country.<CountryView>getView(Country.class).capitalId();
        if (capitalId == 0) {
            return 0;
        }
        EntityView capital = world.obtainEntityView(capitalId);
        if (!capital.has(Province.class)) {
            return 0;
        }
        ProvinceView province = capital.getView(Province.class);
        return province.ownerId() == countryId ? province.regionInstanceId() : 0;
    }

    private double operatingCost(World world, EntityView country, SpecialBuildingTypeView buildingType, CountryBudgetView countryBudget, CountryMarketView countryMarket) {
        double cost = 0.0;
        for (int k = 0; k < buildingType.maintenanceGoodIndexesLength(); k++) {
            int goodIndex = buildingType.maintenanceGoodIndexes(k);
            if (goodIndex < 0) {
                break;
            }
            cost += (double) buildingType.maintenanceGoodAmounts(k) / DAYS_PER_YEAR * countryMarket.goodBudgetPrices(goodIndex);
        }
        if (buildingType.staffPopTypeIndex() >= 0 && buildingType.staffPosts() > 0f) {
            float units = buildingType.staffPosts() / NEEDS_SCALING_FACTOR;
            CountryBudgetPolicyView budgetPolicy = country.getView(CountryBudgetPolicy.class);
            cost += (double) units * countryBudget.wageRatesByPopType(buildingType.staffPopTypeIndex());
            int statePostIndex = this.ecsConstants.statePostIndex();
            if (buildingType.staffPopTypeIndex() == statePostIndex) {
                cost += StateConsumptionUtils.dailyCost(StateConsumptionUtils.globalStateConsumption(world, this.ecsConstants), countryMarket,
                    units * StateBudgetUtils.slider(budgetPolicy.administrationSpendingRate()), 0f);
            }
        }
        return cost;
    }

    private void computeFactoryValues() {
        this.factoryValues.clear();
        this.factoryQuery.iter(this.factoryReader);
    }

    private void readFactory(Iter factoryIter) {
        LongFloatMap factoryValues = this.factoryValues;
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
    }

    private static float annualReturn(World world, BuildingView building, DevelopmentBuildingTypeView buildingType, float productionModifier, double baseValue) {
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
        double maintenanceCost = 0.0;
        for (int k = 0; k < buildingType.maintenanceGoodIndexesLength(); k++) {
            int goodIndex = buildingType.maintenanceGoodIndexes(k);
            if (goodIndex < 0) {
                break;
            }
            maintenanceCost += (double) buildingType.maintenanceGoodAmounts(k) * countryMarket.goodBudgetPrices(goodIndex);
        }
        return (float) ((productionModifier * baseValue * DAYS_PER_YEAR - maintenanceCost) / levelCost);
    }

    private static double baseValue(World world, LongFloatMap factoryValues, BuildingView building) {
        CountryMarketView countryMarket = world.obtainEntityView(building.countryId()).getView(CountryMarket.class);
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
        return baseValue;
    }
}
