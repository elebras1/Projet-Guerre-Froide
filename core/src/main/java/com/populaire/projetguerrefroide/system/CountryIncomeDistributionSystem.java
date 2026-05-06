package com.populaire.projetguerrefroide.system;

import com.github.elebras1.flecs.EntityView;
import com.github.elebras1.flecs.Field;
import com.github.elebras1.flecs.Iter;
import com.github.elebras1.flecs.World;
import com.populaire.projetguerrefroide.component.*;

public class CountryIncomeDistributionSystem {

    public CountryIncomeDistributionSystem(World ecsWorld, long phaseId) {
        ecsWorld.system("CountryIncomeDistributionSystem")
            .kind(phaseId)
            .with(RegionInstance.class)
            .with(RegionInstanceIncome.class)
            .iter(this::distribute);
    }

    private void distribute(Iter iter) {
        long countryId = 0;
        CountryMarketView countryMarket = null;

        Field<RegionInstance> regionInstanceField = iter.field(RegionInstance.class, 0);
        Field<RegionInstanceIncome> regionInstanceIncomeField = iter.field(RegionInstanceIncome.class, 1);
        for (int i = 0; i < iter.count(); i++) {
            RegionInstanceView regionInstance = regionInstanceField.getMutView(i);
            RegionInstanceIncomeView regionInstanceIncome = regionInstanceIncomeField.getMutView(i);

            if(regionInstance.ownerId() != countryId) {
                countryId = regionInstance.ownerId();
                EntityView country = iter.world().obtainEntityView(countryId);
                countryMarket = country.getMutView(CountryMarket.class);
            }

            countryMarket.treasury(countryMarket.treasury() + regionInstanceIncome.countryProfitShare());
        }
    }
}
