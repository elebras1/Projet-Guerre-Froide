package com.populaire.projetguerrefroide.system;

import com.github.tommyettinger.ds.LongFloatMap;
import com.github.tommyettinger.ds.LongIntMap;
import com.github.tommyettinger.ds.LongObjectMap;
import com.github.tommyettinger.ds.ObjectList;
import io.github.elebras1.flecs.EntityView;
import io.github.elebras1.flecs.Field;
import io.github.elebras1.flecs.Iter;
import io.github.elebras1.flecs.Query;
import io.github.elebras1.flecs.World;
import com.populaire.projetguerrefroide.component.*;
import com.populaire.projetguerrefroide.util.EcsConstants;
import com.populaire.projetguerrefroide.util.InfrastructureUtils;
import com.populaire.projetguerrefroide.util.PopulationMobilityUtils;

import java.util.Comparator;

import static com.populaire.projetguerrefroide.util.Constants.*;
import static com.populaire.projetguerrefroide.util.ConstructionUtils.levelCost;
import static com.populaire.projetguerrefroide.util.ConstructionUtils.startSite;
import static com.populaire.projetguerrefroide.util.EconomyBuildingUtils.infrastructureBonus;
import static com.populaire.projetguerrefroide.util.StateBudgetUtils.stateInterestRate;

public class PublicWorksSystem {

    private record Candidate(long buildingId, long buildingTypeId, long regionInstanceId, float annualReturn) {
    }

    private final EcsConstants ecsConstants;
    private final Query developmentQuery;
    private final Query factoryQuery;
    private final Query siteQuery;

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
        boolean planningDay = PopulationMobilityUtils.isMobilityDay(world);
        LongIntMap queuedLevels = new LongIntMap();
        LongFloatMap dailyCommitments = new LongFloatMap();
        LongIntMap sitesInProgress = new LongIntMap();
        this.readSites(world, queuedLevels, dailyCommitments, sitesInProgress);
        LongObjectMap<ObjectList<Candidate>> candidatesByCountry = new LongObjectMap<>();
        if (planningDay) {
            this.collectCandidates(world, queuedLevels, candidatesByCountry);
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
                    if (candidates != null) {
                        candidates.sort(Comparator.comparingDouble((Candidate candidate) -> candidate.annualReturn()).reversed());
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
                    countryPublicWorks.targetReturn(bestReturn);
                    countryBudget.investableSurplus(Math.max(0.0, ownFunds));
                }
                countryPublicWorks.sitesInProgress(sites);
                countryPublicWorks.dailyCommitment(dailyCommitment);
                countryBudget.publicWorksLoan((float) borrowed);
            }
        }
    }

    private void readSites(World world, LongIntMap queuedLevels, LongFloatMap dailyCommitments, LongIntMap sitesInProgress) {
        long countryTag = this.ecsConstants.countryTag();
        this.siteQuery.each((long siteId) -> {
            ConstructionSiteView site = world.obtainEntityView(siteId).getView(ConstructionSite.class);
            if (site.buildingId() == 0 || !world.obtainEntityView(site.buildingTypeId()).has(DevelopmentBuildingType.class)) {
                return;
            }
            queuedLevels.put(site.buildingId(), queuedLevels.get(site.buildingId()) + 1);
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
        });
    }

    private void collectCandidates(World world, LongIntMap queuedLevels, LongObjectMap<ObjectList<Candidate>> candidatesByCountry) {
        LongFloatMap factoryValues = this.factoryValues();
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
                long buildingId = developmentIter.entity(j);
                if (building.countryId() == 0 || building.size() >= buildingType.maxLevel() || queuedLevels.get(buildingId) > 0) {
                    continue;
                }
                float annualReturn = annualReturn(world, factoryValues, building, buildingType, productionModifier);
                if (annualReturn <= STATE_MINIMUM_RETURN) {
                    continue;
                }
                ObjectList<Candidate> candidates = candidatesByCountry.get(building.countryId());
                if (candidates == null) {
                    candidates = new ObjectList<>();
                    candidatesByCountry.put(building.countryId(), candidates);
                }
                ProvinceView province = world.obtainEntityView(building.parentId()).getView(Province.class);
                candidates.add(new Candidate(buildingId, buildingTypeId, province.regionInstanceId(), annualReturn));
            }
        });
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
        double maintenanceCost = 0.0;
        for (int k = 0; k < buildingType.maintenanceGoodIndexesLength(); k++) {
            int goodIndex = buildingType.maintenanceGoodIndexes(k);
            if (goodIndex < 0) {
                break;
            }
            maintenanceCost += (double) buildingType.maintenanceGoodAmounts(k) * countryMarket.goodBudgetPrices(goodIndex);
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
        return (float) ((productionModifier * baseValue * DAYS_PER_YEAR - maintenanceCost) / levelCost);
    }
}
