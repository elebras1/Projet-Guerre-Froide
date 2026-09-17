package com.populaire.projetguerrefroide.system;

import io.github.elebras1.flecs.EntityView;
import io.github.elebras1.flecs.Field;
import io.github.elebras1.flecs.Iter;
import io.github.elebras1.flecs.World;
import com.populaire.projetguerrefroide.component.*;

public class RegionIncomeResidualSystem {

    public RegionIncomeResidualSystem(World ecsWorld, long phaseId) {
        ecsWorld.system("RegionIncomeResidualSystem")
            .kind(phaseId)
            .with(RegionInstance.class)
            .with(RegionInstanceIncome.class)
            .iter(this::collect);
    }

    private void collect(Iter iter) {
        long countryId = 0;
        CountryMarketView countryMarket = null;

        Field<RegionInstance> regionInstanceField = iter.field(RegionInstance.class, 0);
        Field<RegionInstanceIncome> regionInstanceIncomeField = iter.field(RegionInstanceIncome.class, 1);
        for (int i = 0; i < iter.count(); i++) {
            RegionInstanceView regionInstance = regionInstanceField.getMutView(i);
            RegionInstanceIncomeView regionInstanceIncome = regionInstanceIncomeField.getMutView(i);

            float residual = regionInstanceIncome.capitalistProfitShare() - regionInstanceIncome.claimedCapitalistShare()
                + regionInstanceIncome.aristocratProfitShare() - regionInstanceIncome.claimedAristocratShare()
                + regionInstanceIncome.countryProfitShare() - regionInstanceIncome.claimedCountryShare();
            for (int p = 0; p < regionInstanceIncome.minWagesByPopTypeLength(); p++) {
                residual += regionInstanceIncome.minWagesByPopType(p) + regionInstanceIncome.profitShareByPopType(p) - regionInstanceIncome.claimedIncomeByPopType(p);
            }

            if (residual <= 0f) {
                continue;
            }

            if (regionInstance.ownerId() != countryId) {
                countryId = regionInstance.ownerId();
                EntityView country = iter.world().obtainEntityView(countryId);
                countryMarket = country.getMutView(CountryMarket.class);
            }
            countryMarket.treasury(countryMarket.treasury() + residual);
        }
    }
}
