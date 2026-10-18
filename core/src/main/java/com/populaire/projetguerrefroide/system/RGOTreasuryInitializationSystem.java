package com.populaire.projetguerrefroide.system;

import io.github.elebras1.flecs.EntityView;
import io.github.elebras1.flecs.Field;
import io.github.elebras1.flecs.Iter;
import io.github.elebras1.flecs.World;
import com.populaire.projetguerrefroide.component.*;
import com.populaire.projetguerrefroide.util.EcsConstants;

import static com.populaire.projetguerrefroide.util.Constants.RGO_TREASURY_BUFFER_DAYS;
import static com.populaire.projetguerrefroide.util.RgoUtils.wageBill;

public class RGOTreasuryInitializationSystem {

    public RGOTreasuryInitializationSystem(World ecsWorld, EcsConstants ecsConstants, long phaseId) {
        ecsWorld.system("RGOTreasuryInitializationSystem")
            .kind(phaseId)
            .with(Province.class)
            .with(ResourceGathering.class)
            .with(ResourceGatheringType.class).up(ecsConstants.rgoType())
            .multiThreaded()
            .iter(this::initialize);
    }

    private void initialize(Iter iter) {
        long countryId = 0;
        CountryMarketView countryMarket = null;
        CountryLaborPolicyView countryLaborPolicy = null;

        Field<Province> provinceField = iter.field(Province.class, 0);
        Field<ResourceGathering> resourceGatheringField = iter.field(ResourceGathering.class, 1);
        int workerPopTypeIndex = iter.field(ResourceGatheringType.class, 2).<ResourceGatheringTypeView>getView(0).workerPopTypeIndex();
        for (int i = 0; i < iter.count(); i++) {
            ProvinceView province = provinceField.getMutView(i);
            ResourceGatheringMutView resourceGathering = resourceGatheringField.getMutView(i);
            if (province.ownerId() != countryId) {
                countryId = province.ownerId();
                EntityView country = iter.world().obtainEntityView(countryId);
                countryMarket = country.getView(CountryMarket.class);
                countryLaborPolicy = country.getView(CountryLaborPolicy.class);
            }
            resourceGathering.treasury(RGO_TREASURY_BUFFER_DAYS * wageBill(countryMarket, workerPopTypeIndex, countryLaborPolicy.minWageFactor(), resourceGathering.workerAmount()));
        }
    }
}
