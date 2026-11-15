package com.populaire.projetguerrefroide.system.population;

import io.github.elebras1.flecs.EntityView;
import io.github.elebras1.flecs.Field;
import io.github.elebras1.flecs.Iter;
import io.github.elebras1.flecs.World;
import com.populaire.projetguerrefroide.component.*;
import com.populaire.projetguerrefroide.simulation.EcsConstants;
import com.populaire.projetguerrefroide.system.state.StateBudgetUtils;

import static com.populaire.projetguerrefroide.simulation.Constants.*;
import static com.populaire.projetguerrefroide.system.population.StrataUtils.*;

public class PopulationIncomeDistributionSystem {
    private final EcsConstants ecsConstants;

    public PopulationIncomeDistributionSystem(World ecsWorld, EcsConstants ecsConstants, long phaseId) {
        this.ecsConstants = ecsConstants;
        ecsWorld.system("PopulationIncomeDistributionSystem")
            .with(Population.class)
            .with(PopulationType.class)
            .with(ecsConstants.capitalistTag()).optional()
            .with(ecsConstants.aristocratTag()).optional()
            .with(ecsConstants.ruralTag()).optional()
            .kind(phaseId)
            .iter(this::distribute);
    }

    public void distribute(Iter iter) {
        Field<PopulationType> populationTypeField = iter.field(PopulationType.class, 1);
        PopulationTypeView populationTypeData = populationTypeField.getView(0);
        int typeIdx = populationTypeData.index();
        int strata = populationTypeData.strata();
        boolean capitalist = iter.isSet(2);
        boolean aristocrat = iter.isSet(3);
        boolean rural = iter.isSet(4);
        boolean paidOnPosts = StateBudgetUtils.paidOnPosts(typeIdx, this.ecsConstants.statePostIndex(), populationTypeData.incomeType());
        GlobalPopulationTypeView globalPopulationType = iter.stage().obtainEntityView(this.ecsConstants.globalPopulationType()).getView(GlobalPopulationType.class);
        int religiousIndex = globalPopulationType.religiousPopTypeIndex();
        boolean donor = religiousIndex >= 0 && typeIdx != religiousIndex;
        float baseWorkforceShare = globalPopulationType.workforceShareByPopType(typeIdx);

        long regionInstanceId = 0;
        RegionInstanceView regionInstance = null;
        RegionInstanceIncomeMutView regionInstanceIncome = null;
        RegionDemographicsView regionDemographics = null;

        long provinceId = 0;
        ProvinceView province = null;
        float provinceRgoPay = 0f;
        int provinceRgoWorkerIndex = -1;

        long countryId = 0;
        CountryMarketMutView countryMarket = null;
        CountryTaxPolicyView countryTaxPolicy = null;
        CountryCulturePolicyView countryCulturePolicy = null;
        CountryPopulationPolicyView countryPopulationPolicy = null;
        CountryBudgetView countryBudget = null;
        CountryBankMutView countryBank = null;
        CountryInvestmentMutView countryInvestment = null;

        Field<Population> populationField = iter.field(Population.class, 0);
        for (int i = 0; i < iter.count(); i++) {
            PopulationMutView population = populationField.getMutView(i);

            if (population.regionInstanceId() != regionInstanceId) {
                regionInstanceId = population.regionInstanceId();
                EntityView regionEntity = iter.stage().obtainEntityView(regionInstanceId);
                regionInstance = regionEntity.getView(RegionInstance.class);
                regionInstanceIncome = regionEntity.getMutView(RegionInstanceIncome.class);
                regionDemographics = regionEntity.getMutView(RegionDemographics.class);
            }

            if (population.provinceId() != provinceId) {
                provinceId = population.provinceId();
                EntityView provinceEntity = iter.stage().obtainEntityView(provinceId);
                province = provinceEntity.getView(Province.class);
                provinceRgoPay = 0f;
                provinceRgoWorkerIndex = -1;
                if (rural && provinceEntity.has(ResourceGathering.class)) {
                    provinceRgoPay = provinceEntity.<ResourceGatheringView>getView(ResourceGathering.class).workerPay();
                    long rgoTypeId = provinceEntity.target(this.ecsConstants.rgoType(), 0);
                    provinceRgoWorkerIndex = iter.stage().obtainEntityView(rgoTypeId).<ResourceGatheringTypeView>getView(ResourceGatheringType.class).workerPopTypeIndex();
                }
            }

            if (population.countryId() != countryId) {
                countryId = population.countryId();
                EntityView country = iter.stage().obtainEntityView(countryId);
                countryMarket = country.getMutView(CountryMarket.class);
                countryTaxPolicy = country.getMutView(CountryTaxPolicy.class);
                countryCulturePolicy = country.getView(CountryCulturePolicy.class);
                countryPopulationPolicy = country.getView(CountryPopulationPolicy.class);
                countryBudget = country.getMutView(CountryBudget.class);
                countryBank = country.getMutView(CountryBank.class);
                countryInvestment = country.getMutView(CountryInvestment.class);
            }

            float grossIncome = 0f;

            float popAmount = population.amount();

            float totalMinWages = regionInstanceIncome.minWagesByPopType(typeIdx);
            float totalBonus = regionInstanceIncome.profitShareByPopType(typeIdx);
            int totalPopType = regionDemographics.totalByPopType(typeIdx);
            if (rural) {
                // rural workers are paid by the RGO of their own province only (decision 201 b): those of a province whose
                // RGO employs another type, or nobody, get nothing from the wages of the others
                float pay = typeIdx == provinceRgoWorkerIndex ? provinceRgoPay : 0f;
                grossIncome += pay;
                regionInstanceIncome.claimedIncomeByPopType(typeIdx, regionInstanceIncome.claimedIncomeByPopType(typeIdx) + pay);
            } else if (totalPopType > 0) {
                // the other types share the region's wages per head, which is per employment: every pop of a type has the
                // region's employment rate (PopulationEmploymentSynchronizationSystem)
                float distributedIncome = (totalMinWages + totalBonus) * popAmount / totalPopType;
                grossIncome += distributedIncome;
                regionInstanceIncome.claimedIncomeByPopType(typeIdx, regionInstanceIncome.claimedIncomeByPopType(typeIdx) + distributedIncome);
            }

            if (capitalist) {
                float capitalistTotal = regionInstanceIncome.capitalistProfitShare();
                int totalCapitalists = regionDemographics.totalByPopType(typeIdx);
                if (totalCapitalists > 0) {
                    float distributedIncome = capitalistTotal * popAmount / totalCapitalists;
                    grossIncome += distributedIncome;
                    regionInstanceIncome.claimedCapitalistShare(regionInstanceIncome.claimedCapitalistShare() + distributedIncome);
                }
            }

            if (aristocrat) {
                float aristocratTotal = regionInstanceIncome.aristocratProfitShare();
                int totalAristocrats = regionDemographics.totalByPopType(typeIdx);
                if (totalAristocrats > 0) {
                    float distributedIncome = aristocratTotal * popAmount / totalAristocrats;
                    grossIncome += distributedIncome;
                    regionInstanceIncome.claimedAristocratShare(regionInstanceIncome.claimedAristocratShare() + distributedIncome);
                }
            }

            float taxRate = 0f;
            switch (strata) {
                case POOR_STRATA -> taxRate = countryTaxPolicy.poorTaxRate();
                case MIDDLE_STRATA -> taxRate = countryTaxPolicy.middleTaxRate();
                case RICH_STRATA -> taxRate = countryTaxPolicy.richTaxRate();
            }
            float tax = grossIncome * taxRate * regionInstance.taxCollectionShare();
            float netIncome = grossIncome - tax;
            float donation = 0f;
            if (donor && regionDemographics.totalByPopType(religiousIndex) > 0) {
                donation = Math.max(0f, netIncome) * countryCulturePolicy.religiousDonationRate();
                regionInstanceIncome.donations(regionInstanceIncome.donations() + donation);
                regionInstanceIncome.donationsByPopType(typeIdx, regionInstanceIncome.donationsByPopType(typeIdx) + donation);
            }

            float purchase = population.amount() / NEEDS_SCALING_FACTOR * (population.lifeNeedsDemandRatio() * countryMarket.paidLifeCostsByPopType(typeIdx)
                + population.everydayNeedsDemandRatio() * countryMarket.paidEverydayCostsByPopType(typeIdx)
                + population.luxuryNeedsDemandRatio() * countryMarket.paidLuxuryCostsByPopType(typeIdx));
            countryMarket.treasury(countryMarket.treasury() + tax);
            countryMarket.taxRevenue(countryMarket.taxRevenue() + tax);

            float scaledAmount = popAmount / NEEDS_SCALING_FACTOR;
            float scaledUnemployed = Math.max(0f, population.amount() * EmploymentUtils.activeShare(baseWorkforceShare, countryPopulationPolicy.workforceShareModifier()) - population.employment()) / NEEDS_SCALING_FACTOR;
            float scaledWageBase = paidOnPosts ? population.employment() / NEEDS_SCALING_FACTOR : scaledAmount;
            float scaledSeniors = province.adultsAmount() > 0 ? (float) ((double) province.seniorsAmount() * popAmount / province.adultsAmount() / NEEDS_SCALING_FACTOR) : 0f;
            float stateIncome = scaledWageBase * countryBudget.wageRatesByPopType(typeIdx) + scaledUnemployed * countryBudget.socialRatesByPopType(typeIdx)
                + scaledSeniors * countryBudget.pensionRatesByPopType(typeIdx);
            stateIncome = (float) Math.min(stateIncome, Math.max(0.0, countryMarket.treasury()));
            countryMarket.treasury(countryMarket.treasury() - stateIncome);

            population.deposit(population.deposit() * (1f - countryBank.depositLossRatio()));
            float interestIncome = (float) Math.min(population.deposit() * countryBank.depositRate(), Math.max(0.0, countryBank.interestPool()));
            countryBank.interestPool(countryBank.interestPool() - interestIncome);

            float income = netIncome - donation + stateIncome + interestIncome;
            population.income(income);
            if (population.amount() > 0) {
                // decision 230: income per 100 000 people, followed over a year (habits)
                float perUnit = Math.max(0f, income) / (population.amount() / NEEDS_SCALING_FACTOR);
                population.incomeTrend(population.incomeTrend() > 0f ? population.incomeTrend() + (perUnit - population.incomeTrend()) / INCOME_TREND_DAYS : perUnit);
            }
            population.savings(population.savings() + income - purchase);

            float deposit = Math.min(population.unspentBudget(), Math.max(0f, population.savings()));
            if (deposit > 0f) {
                population.savings(population.savings() - deposit);
                if (capitalist) {
                    countryInvestment.funds(CAPITALIST_OWNER, countryInvestment.funds(CAPITALIST_OWNER) + deposit);
                } else {
                    population.deposit(population.deposit() + deposit);
                    countryBank.reserves(countryBank.reserves() + deposit);
                }
            }
            countryBank.deposits(countryBank.deposits() + population.deposit());

            float coveredLifeShare = SubsistenceUtils.coveredLifeShare(rural, population.amount(), population.employment());
            population.lifeNeedsSatisfaction(coveredLifeShare + population.lifeNeedsDemandRatio() * countryMarket.servedLifeRatiosByPopType(typeIdx));
            population.everydayNeedsSatisfaction(population.everydayNeedsDemandRatio() * countryMarket.servedEverydayRatiosByPopType(typeIdx));
            population.luxuryNeedsSatisfaction(population.luxuryNeedsDemandRatio() * countryMarket.servedLuxuryRatiosByPopType(typeIdx));
        }
    }
}
