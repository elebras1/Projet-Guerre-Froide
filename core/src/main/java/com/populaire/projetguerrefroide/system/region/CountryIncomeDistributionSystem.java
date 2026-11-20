package com.populaire.projetguerrefroide.system.region;

import io.github.elebras1.flecs.EntityView;
import io.github.elebras1.flecs.Field;
import io.github.elebras1.flecs.Iter;
import io.github.elebras1.flecs.World;
import com.populaire.projetguerrefroide.component.*;
import com.populaire.projetguerrefroide.simulation.EcsConstants;

import static com.populaire.projetguerrefroide.simulation.Constants.*;

public class CountryIncomeDistributionSystem {

    private final EcsConstants ecsConstants;

    public CountryIncomeDistributionSystem(World ecsWorld, EcsConstants ecsConstants, long phaseId) {
        this.ecsConstants = ecsConstants;
        ecsWorld.system("CountryIncomeDistributionSystem")
            .kind(phaseId)
            .with(RegionInstance.class)
            .with(RegionInstanceIncome.class)
            .with(RegionDemographics.class)
            .iter(this::distribute);
    }

    private void distribute(Iter iter) {
        int capitalistIndex = iter.stage().obtainEntityView(this.ecsConstants.globalPopulationType()).<GlobalPopulationTypeView>getView(GlobalPopulationType.class).capitalistPopTypeIndex();
        long countryId = 0;
        CountryMarketMutView countryMarket = null;
        CountryInvestmentMutView countryInvestment = null;
        double fundDividendPerHead = 0.0;

        Field<RegionInstance> regionInstanceField = iter.field(RegionInstance.class, 0);
        Field<RegionInstanceIncome> regionInstanceIncomeField = iter.field(RegionInstanceIncome.class, 1);
        Field<RegionDemographics> regionDemographicsField = iter.field(RegionDemographics.class, 2);
        for (int i = 0; i < iter.count(); i++) {
            RegionInstanceView regionInstance = regionInstanceField.getMutView(i);
            RegionInstanceIncomeMutView regionInstanceIncome = regionInstanceIncomeField.getMutView(i);

            if(regionInstance.ownerId() != countryId) {
                countryId = regionInstance.ownerId();
                EntityView country = iter.stage().obtainEntityView(countryId);
                countryMarket = country.getMutView(CountryMarket.class);
                countryInvestment = country.getMutView(CountryInvestment.class);
                fundDividendPerHead = 0.0;
                long capitalists = capitalistIndex >= 0 ? country.<CountryDemographicsView>getView(CountryDemographics.class).totalByPopType(capitalistIndex) : 0L;
                double uncommitted = countryInvestment.funds(CAPITALIST_OWNER) - countryInvestment.committed(CAPITALIST_OWNER)
                    - country.<CountryBankView>getView(CountryBank.class).loans(CAPITALIST_OWNER);
                if (capitalists > 0L && uncommitted > 0.0) {
                    fundDividendPerHead = uncommitted * INVESTOR_FUND_PAYOUT_RATE / DAYS_PER_YEAR / capitalists;
                }
            }

            if (fundDividendPerHead > 0.0) {
                RegionDemographicsView regionDemographics = regionDemographicsField.getView(i);
                float dividend = (float) (fundDividendPerHead * regionDemographics.totalByPopType(capitalistIndex));
                if (dividend > 0f) {
                    countryInvestment.funds(CAPITALIST_OWNER, countryInvestment.funds(CAPITALIST_OWNER) - dividend);
                    regionInstanceIncome.capitalistProfitShare(regionInstanceIncome.capitalistProfitShare() + dividend);
                }
            }

            countryMarket.treasury(countryMarket.treasury() + regionInstanceIncome.countryProfitShare());
            countryMarket.profitRevenue(countryMarket.profitRevenue() + regionInstanceIncome.countryProfitShare());
            regionInstanceIncome.claimedCountryShare(regionInstanceIncome.countryProfitShare());
        }
    }
}
