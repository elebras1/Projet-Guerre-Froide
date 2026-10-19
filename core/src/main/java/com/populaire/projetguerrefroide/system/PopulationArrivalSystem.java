package com.populaire.projetguerrefroide.system;

import io.github.elebras1.flecs.Field;
import io.github.elebras1.flecs.Iter;
import io.github.elebras1.flecs.World;
import com.populaire.projetguerrefroide.component.*;
import com.populaire.projetguerrefroide.util.PopulationMobilityUtils;

public class PopulationArrivalSystem {

    public PopulationArrivalSystem(World ecsWorld, long phaseId) {
        ecsWorld.system("PopulationArrivalSystem")
            .kind(phaseId)
            .with(Population.class)
            .with(PopulationType.class)
            .iter(this::arrive);
    }

    private void arrive(Iter iter) {
        World world = iter.world();
        if (!PopulationMobilityUtils.isMobilityDay(world)) {
            return;
        }
        int popTypeIndex = iter.field(PopulationType.class, 1).<PopulationTypeView>getView(0).index();

        long provinceId = 0;
        PopulationTransferMutView transfer = null;
        Field<Population> populationField = iter.field(Population.class, 0);
        for (int i = 0; i < iter.count(); i++) {
            PopulationMutView population = populationField.getMutView(i);
            if (population.provinceId() != provinceId) {
                provinceId = population.provinceId();
                transfer = world.obtainEntityView(provinceId).getMutView(PopulationTransfer.class);
            }
            int arriving = transfer.arrivingAmounts(popTypeIndex);
            if (arriving == 0) {
                continue;
            }
            population.amount(population.amount() + arriving);
            population.savings(population.savings() + transfer.arrivingSavings(popTypeIndex));
            transfer.arrivingAmounts(popTypeIndex, 0);
            transfer.arrivingSavings(popTypeIndex, 0f);
        }
    }
}
