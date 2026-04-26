package com.populaire.projetguerrefroide.system;

import com.github.elebras1.flecs.EntityView;
import com.github.elebras1.flecs.Field;
import com.github.elebras1.flecs.Iter;
import com.github.elebras1.flecs.World;
import com.populaire.projetguerrefroide.component.*;

public class RGOSpreadProductionSystem {

    public RGOSpreadProductionSystem(World ecsWorld, long phaseId) {
        ecsWorld.system("RGOSpreadProductionSystem")
            .kind(phaseId)
            .with(Province.class)
            .with(ResourceGathering.class)
            .iter(this::spread);
    }

    private void spread(Iter iter) {
        long countryId = 0;
        CountryMarketView countryMarket = null;

        Field<Province> provinceField = iter.field(Province.class, 0);
        Field<ResourceGathering> resourceGatheringField = iter.field(ResourceGathering.class, 1);
        for(int i = 0; i < iter.count(); i++) {
            ProvinceView province = provinceField.getMutView(i);
            ResourceGatheringView resourceGathering = resourceGatheringField.getMutView(i);

            if(countryId != province.ownerId()) {
                countryId = province.ownerId();
                EntityView country = iter.world().obtainEntityView(countryId);
                countryMarket = country.getMutView(CountryMarket.class);
            }

            countryMarket.domesticMarketPool(resourceGathering.goodIndex(), countryMarket.domesticMarketPool(resourceGathering.goodIndex()) + resourceGathering.production());
        }
    }
}
