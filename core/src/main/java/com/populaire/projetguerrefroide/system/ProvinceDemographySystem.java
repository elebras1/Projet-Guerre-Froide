package com.populaire.projetguerrefroide.system;

import io.github.elebras1.flecs.Field;
import io.github.elebras1.flecs.Iter;
import io.github.elebras1.flecs.World;
import com.populaire.projetguerrefroide.component.*;
import com.populaire.projetguerrefroide.util.PopulationMobilityUtils;
import com.populaire.projetguerrefroide.util.StateBudgetUtils;
import com.populaire.projetguerrefroide.util.SubsistenceUtils;
import com.populaire.projetguerrefroide.simulation.EcsConstants;

import static com.populaire.projetguerrefroide.simulation.Constants.*;

public class ProvinceDemographySystem {
    private final EcsConstants ecsConstants;

    public ProvinceDemographySystem(World ecsWorld, EcsConstants ecsConstants, long phaseId) {
        this.ecsConstants = ecsConstants;
        ecsWorld.system("ProvinceDemographySystem")
            .kind(phaseId)
            .with(Province.class)
            .with(Demographics.class)
            .with(ProvinceDemography.class)
            .iter(this::age);
    }

    private void age(Iter iter) {
        if (!PopulationMobilityUtils.isMobilityDay(iter.world(), this.ecsConstants)) {
            return;
        }

        double monthShare = (double) DAYS_PER_MONTH / DAYS_PER_YEAR;
        Field<Province> provinceField = iter.field(Province.class, 0);
        Field<Demographics> demographicsField = iter.field(Demographics.class, 1);
        Field<ProvinceDemography> demographyField = iter.field(ProvinceDemography.class, 2);
        long ownerId = 0;
        float pensionMortality = StateBudgetUtils.pensionMortality(0f);
        for (int i = 0; i < iter.count(); i++) {
            ProvinceMutView province = provinceField.getMutView(i);
            DemographicsView demographics = demographicsField.getMutView(i);
            ProvinceDemographyMutView demography = demographyField.getMutView(i);

            if (province.ownerId() != ownerId) {
                ownerId = province.ownerId();
                float pensionCoverage = 0f;
                if (ownerId != 0) {
                    CountryBudgetView countryBudget = iter.world().obtainEntityView(ownerId).getView(CountryBudget.class);
                    pensionCoverage = countryBudget.pensionCoverage();
                }
                pensionMortality = StateBudgetUtils.pensionMortality(pensionCoverage);
            }

            int adults = demographics.totalPopulation();
            int children = province.childrenAmount();
            int seniors = province.seniorsAmount();
            float famineRate = SubsistenceUtils.famineDeathRate(this.lifeNeedsSatisfaction(demographics));
            double births = BIRTH_RATE * demography.childrenPerAdult() * adults * monthShare;
            double childAging = adults > 0 ? CHILD_AGING_RATE * children * monthShare : 0.0;
            double childDeaths = (CHILD_DEATH_RATE + famineRate) * children * monthShare;
            double adultAging = ADULT_AGING_RATE * adults * monthShare;
            double seniorDeaths = (SENIOR_DEATH_RATE * pensionMortality + famineRate) * seniors * monthShare;

            double nextChildren = children + births - childAging - childDeaths + demography.childrenRemainder();
            double nextSeniors = seniors + adultAging - seniorDeaths + demography.seniorsRemainder();
            int childrenAmount = (int) Math.floor(nextChildren);
            int seniorsAmount = (int) Math.floor(nextSeniors);

            province.childrenAmount(childrenAmount);
            province.seniorsAmount(seniorsAmount);
            demography.childrenRemainder((float) (nextChildren - childrenAmount));
            demography.seniorsRemainder((float) (nextSeniors - seniorsAmount));
            demography.adultEntryShare(adults > 0 ? (float) (childAging / adults) : 0f);
            demography.famineDeathRate(famineRate);
        }
    }

    private float lifeNeedsSatisfaction(DemographicsView demographics) {
        long members = 0;
        double satisfaction = 0.0;
        for (int b = 0; b < POP_TYPE_COUNT; b++) {
            members += demographics.totalByPopType(b);
            satisfaction += (double) demographics.totalByPopType(b) * demographics.lifeNeedsSatisfactionByPopType(b);
        }
        return members > 0 ? (float) (satisfaction / members) : 1f;
    }
}
