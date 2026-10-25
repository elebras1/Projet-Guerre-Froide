package com.populaire.projetguerrefroide.system;

import io.github.elebras1.flecs.EntityView;
import io.github.elebras1.flecs.Field;
import io.github.elebras1.flecs.Iter;
import io.github.elebras1.flecs.World;
import com.populaire.projetguerrefroide.component.*;
import com.populaire.projetguerrefroide.util.PopulationMobilityUtils;

import static com.populaire.projetguerrefroide.util.Constants.*;

public class PopulationGrowthSystem {

    public PopulationGrowthSystem(World ecsWorld, long phaseId) {
        ecsWorld.system("PopulationGrowthSystem")
            .kind(phaseId)
            .with(Population.class)
            .with(PopulationType.class)
            .iter(this::grow);
    }

    private void grow(Iter iter) {
        World world = iter.world();
        if (!PopulationMobilityUtils.isMobilityDay(world)) {
            return;
        }

        int popTypeIndex = iter.field(PopulationType.class, 1).<PopulationTypeView>getView(0).index();
        double monthShare = (double) DAYS_PER_MONTH / DAYS_PER_YEAR;
        Field<Population> populationField = iter.field(Population.class, 0);
        for (int i = 0; i < iter.count(); i++) {
            PopulationMutView population = populationField.getMutView(i);
            int amount = population.amount();
            if (amount <= 0) {
                continue;
            }

            EntityView province = world.obtainEntityView(population.provinceId());
            ProvinceDemographyView demography = province.getView(ProvinceDemography.class);
            double exitShare = (ADULT_DEATH_RATE + ADULT_AGING_RATE + demography.famineDeathRate()) * monthShare;
            double change = amount * (demography.adultEntryShare() - exitShare) + population.growthRemainder();
            int delta = (int) Math.floor(change);
            int nextAmount = amount + delta;
            population.amount(nextAmount);
            population.growthRemainder((float) (change - delta));
            population.employment(Math.min(population.employment(), nextAmount));
            ProvinceMutView provinceData = province.getMutView(Province.class);
            provinceData.adultsAmount(provinceData.adultsAmount() + delta);

            if (nextAmount == 0) {
                this.bequeath(population, popTypeIndex, province);
            }
        }
    }

    private void bequeath(PopulationMutView population, int popTypeIndex, EntityView province) {
        float savings = population.savings();
        float deposit = population.deposit();
        if (savings <= 0f && deposit <= 0f) {
            return;
        }

        DemographicsView demographics = province.getView(Demographics.class);
        long heirs = 0;
        int lastHeir = -1;
        for (int b = 0; b < POP_TYPE_COUNT; b++) {
            if (b != popTypeIndex && demographics.totalByPopType(b) > 0) {
                heirs += demographics.totalByPopType(b);
                lastHeir = b;
            }
        }
        if (heirs == 0) {
            return;
        }

        PopulationTransferMutView transfer = province.getMutView(PopulationTransfer.class);
        float savingsLeft = savings;
        float depositLeft = deposit;
        for (int b = 0; b < POP_TYPE_COUNT; b++) {
            if (b == popTypeIndex || demographics.totalByPopType(b) <= 0) {
                continue;
            }
            float share = (float) demographics.totalByPopType(b) / heirs;
            float heirSavings = b == lastHeir ? savingsLeft : savings * share;
            float heirDeposit = b == lastHeir ? depositLeft : deposit * share;
            savingsLeft -= heirSavings;
            depositLeft -= heirDeposit;
            transfer.arrivingSavings(b, transfer.arrivingSavings(b) + heirSavings);
            transfer.arrivingDeposits(b, transfer.arrivingDeposits(b) + heirDeposit);
        }
        population.savings(0f);
        population.deposit(0f);
    }
}
