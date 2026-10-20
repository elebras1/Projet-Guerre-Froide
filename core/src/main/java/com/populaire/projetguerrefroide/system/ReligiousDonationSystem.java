package com.populaire.projetguerrefroide.system;

import io.github.elebras1.flecs.EntityView;
import io.github.elebras1.flecs.Field;
import io.github.elebras1.flecs.Iter;
import io.github.elebras1.flecs.World;
import com.populaire.projetguerrefroide.component.*;
import com.populaire.projetguerrefroide.util.EcsConstants;

public class ReligiousDonationSystem {

    public ReligiousDonationSystem(World ecsWorld, EcsConstants ecsConstants, long phaseId) {
        ecsWorld.system("ReligiousDonationSystem")
            .kind(phaseId)
            .with(Population.class)
            .with(PopulationType.class)
            .with(ecsConstants.religiousTag())
            .iter(this::receive);
    }

    private void receive(Iter iter) {
        int popTypeIndex = iter.field(PopulationType.class, 1).<PopulationTypeView>getView(0).index();

        long regionInstanceId = 0;
        RegionInstanceIncomeMutView regionInstanceIncome = null;
        RegionDemographicsView regionDemographics = null;

        Field<Population> populationField = iter.field(Population.class, 0);
        for (int i = 0; i < iter.count(); i++) {
            PopulationMutView population = populationField.getMutView(i);
            if (population.regionInstanceId() != regionInstanceId) {
                regionInstanceId = population.regionInstanceId();
                EntityView regionEntity = iter.world().obtainEntityView(regionInstanceId);
                regionInstanceIncome = regionEntity.getMutView(RegionInstanceIncome.class);
                regionDemographics = regionEntity.getView(RegionDemographics.class);
            }

            int holders = regionDemographics.totalByPopType(popTypeIndex);
            if (holders <= 0) {
                continue;
            }
            float share = regionInstanceIncome.donations() * population.amount() / holders;
            regionInstanceIncome.claimedDonations(regionInstanceIncome.claimedDonations() + share);
            population.income(population.income() + share);
            population.savings(population.savings() + share);
        }
    }
}
