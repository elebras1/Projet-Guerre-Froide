package com.populaire.projetguerrefroide.system;

import io.github.elebras1.flecs.EntityView;
import io.github.elebras1.flecs.Field;
import io.github.elebras1.flecs.Iter;
import io.github.elebras1.flecs.World;
import com.populaire.projetguerrefroide.component.*;
import com.populaire.projetguerrefroide.util.EcsConstants;
import com.populaire.projetguerrefroide.util.StateBudgetUtils;
import com.populaire.projetguerrefroide.util.SubsistenceUtils;

import static com.populaire.projetguerrefroide.util.Constants.*;
import static com.populaire.projetguerrefroide.util.StrataUtils.*;

public class PopulationIncomeDistributionSystem {

    public PopulationIncomeDistributionSystem(World ecsWorld, EcsConstants ecsConstants, long phaseId) {
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
        boolean statePost = typeIdx == StateBudgetUtils.statePostIndex(iter.world());

        long regionInstanceId = 0;
        RegionInstanceView regionInstance = null;
        RegionInstanceIncomeMutView regionInstanceIncome = null;
        RegionDemographicsView regionDemographics = null;

        long countryId = 0;
        CountryMarketMutView countryMarket = null;
        CountryTaxPolicyView countryTaxPolicy = null;
        CountryBudgetView countryBudget = null;
        CountryBankMutView countryBank = null;
        CountryInvestmentMutView countryInvestment = null;

        Field<Population> populationField = iter.field(Population.class, 0);
        for (int i = 0; i < iter.count(); i++) {
            PopulationMutView population = populationField.getMutView(i);

            if (population.regionInstanceId() != regionInstanceId) {
                regionInstanceId = population.regionInstanceId();
                EntityView regionEntity = iter.world().obtainEntityView(regionInstanceId);
                regionInstance = regionEntity.getView(RegionInstance.class);
                regionInstanceIncome = regionEntity.getMutView(RegionInstanceIncome.class);
                regionDemographics = regionEntity.getMutView(RegionDemographics.class);
            }

            if (population.countryId() != countryId) {
                countryId = population.countryId();
                EntityView country = iter.world().obtainEntityView(countryId);
                countryMarket = country.getMutView(CountryMarket.class);
                countryTaxPolicy = country.getMutView(CountryTaxPolicy.class);
                countryBudget = country.getMutView(CountryBudget.class);
                countryBank = country.getMutView(CountryBank.class);
                countryInvestment = country.getMutView(CountryInvestment.class);
            }

            float grossIncome = 0f;

            float popAmount = population.amount();

            float totalMinWages = regionInstanceIncome.minWagesByPopType(typeIdx);
            float totalBonus = regionInstanceIncome.profitShareByPopType(typeIdx);
            int totalPopType = regionDemographics.totalByPopType(typeIdx);
            if (totalPopType > 0) {
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

            float purchase = population.amount() / NEEDS_SCALING_FACTOR * (population.lifeNeedsDemandRatio() * countryMarket.paidLifeCostsByPopType(typeIdx)
                + population.everydayNeedsDemandRatio() * countryMarket.paidEverydayCostsByPopType(typeIdx)
                + population.luxuryNeedsDemandRatio() * countryMarket.paidLuxuryCostsByPopType(typeIdx));
            countryMarket.treasury(countryMarket.treasury() + tax);
            countryMarket.taxRevenue(countryMarket.taxRevenue() + tax);

            float scaledAmount = popAmount / NEEDS_SCALING_FACTOR;
            float scaledUnemployed = Math.max(0, population.amount() - population.employment()) / NEEDS_SCALING_FACTOR;
            float scaledWageBase = statePost ? population.employment() / NEEDS_SCALING_FACTOR : scaledAmount;
            float stateIncome = scaledWageBase * countryBudget.wageRatesByPopType(typeIdx) + scaledAmount * countryBudget.transferRatesByPopType(typeIdx)
                + scaledUnemployed * countryBudget.socialRatesByPopType(typeIdx);
            stateIncome = (float) Math.min(stateIncome, Math.max(0.0, countryMarket.treasury()));
            countryMarket.treasury(countryMarket.treasury() - stateIncome);

            float bankIncome = (float) Math.min(scaledAmount * countryBank.transferRatesByPopType(typeIdx), Math.max(0.0, countryBank.deposits()));
            countryBank.deposits(countryBank.deposits() - bankIncome);

            float income = netIncome + stateIncome + bankIncome;
            population.income(income);
            population.savings(population.savings() + income - purchase);

            float deposit = Math.min(population.unspentBudget(), Math.max(0f, population.savings()));
            if (deposit > 0f) {
                population.savings(population.savings() - deposit);
                if (capitalist) {
                    countryInvestment.funds(CAPITALIST_OWNER, countryInvestment.funds(CAPITALIST_OWNER) + deposit);
                } else {
                    countryBank.deposits(countryBank.deposits() + deposit);
                }
            }

            float coveredLifeShare = SubsistenceUtils.coveredLifeShare(rural, population.amount(), population.employment());
            population.lifeNeedsSatisfaction(coveredLifeShare + population.lifeNeedsDemandRatio() * countryMarket.servedLifeRatiosByPopType(typeIdx));
            population.everydayNeedsSatisfaction(population.everydayNeedsDemandRatio() * countryMarket.servedEverydayRatiosByPopType(typeIdx));
            population.luxuryNeedsSatisfaction(population.luxuryNeedsDemandRatio() * countryMarket.servedLuxuryRatiosByPopType(typeIdx));
        }
    }
}
