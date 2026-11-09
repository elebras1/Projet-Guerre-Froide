package com.populaire.projetguerrefroide.system.technology;

import com.populaire.projetguerrefroide.component.*;
import com.populaire.projetguerrefroide.simulation.EcsConstants;
import com.populaire.projetguerrefroide.system.population.PopulationMobilityUtils;
import io.github.elebras1.flecs.EntityView;
import io.github.elebras1.flecs.Field;
import io.github.elebras1.flecs.Iter;
import io.github.elebras1.flecs.World;

import static com.populaire.projetguerrefroide.simulation.Constants.*;

public class ResearchSystem {
    private final EcsConstants ecsConstants;

    public ResearchSystem(World ecsWorld, EcsConstants ecsConstants, long phaseId) {
        this.ecsConstants = ecsConstants;
        ecsWorld.system("ResearchSystem")
            .kind(phaseId)
            .with(CountryTechnology.class)
            .with(CountryDevelopment.class)
            .with(CountryEducationPolicy.class)
            .with(CountryDemographics.class)
            .iter(this::research);
    }

    private void research(Iter iter) {
        World world = iter.stage();
        if (!PopulationMobilityUtils.isMobilityDay(world, this.ecsConstants)) {
            return;
        }
        EconomyCalendarView calendar = world.obtainEntityView(this.ecsConstants.economyCalendar()).getView(EconomyCalendar.class);
        float year = calendar.startYear() + calendar.day() / DAYS_PER_YEAR;
        int[] technologyYears = this.ecsConstants.technologyYears();
        int[] technologyTimes = this.ecsConstants.technologyTimes();

        Field<CountryTechnology> countryTechnologyField = iter.field(CountryTechnology.class, 0);
        Field<CountryDevelopment> countryDevelopmentField = iter.field(CountryDevelopment.class, 1);
        Field<CountryEducationPolicy> educationPolicyField = iter.field(CountryEducationPolicy.class, 2);
        Field<CountryDemographics> countryDemographicsField = iter.field(CountryDemographics.class, 3);
        for (int i = 0; i < iter.count(); i++) {
            CountryDemographicsView countryDemographics = countryDemographicsField.getMutView(i);
            if (countryDemographics.totalAdults() <= 0L) {
                continue;
            }
            CountryTechnologyMutView countryTechnology = countryTechnologyField.getMutView(i);
            CountryEducationPolicyMutView educationPolicy = educationPolicyField.getMutView(i);
            CountryDevelopmentView countryDevelopment = countryDevelopmentField.getMutView(i);
            float researchFactor = countryDevelopment.researchFactor();
            for (int tree = 0; tree < TECHNOLOGY_TREE_COUNT; tree++) {
                float researchEfficiency = educationPolicy.researchEfficiency();
                int current = countryTechnology.currentTechnologyIndexes(tree);
                if (current < 0) {
                    current = this.chooseShortest(countryTechnology, tree, year, researchFactor, researchEfficiency);
                    countryTechnology.currentTechnologyIndexes(tree, current);
                    if (current < 0) {
                        continue;
                    }
                }
                float days = ResearchUtils.effectiveDays(technologyTimes[current], technologyYears[current], year, researchFactor, researchEfficiency);
                float progress = countryTechnology.progress(current) + DAYS_PER_MONTH / days;
                if (progress >= 1f) {
                    countryTechnology.progress(current, 1f);
                    countryTechnology.acquired(current, true);
                    countryTechnology.currentTechnologyIndexes(tree, -1);
                    EntityView country = world.obtainEntityView(iter.entity(i));
                    TechnologyEffectUtils.applyTechnology(this.ecsConstants, country, current);
                    TechnologyEffectUtils.clampPolicies(country);
                } else {
                    countryTechnology.progress(current, progress);
                }
            }
        }
    }

    private int chooseShortest(CountryTechnologyView countryTechnology, int tree, float year, float researchFactor, float researchEfficiency) {
        int[] technologyYears = this.ecsConstants.technologyYears();
        int[] technologyTimes = this.ecsConstants.technologyTimes();
        int[] technologyTrees = this.ecsConstants.technologyTrees();
        int technologyCount = this.ecsConstants.technologyCount();
        int best = -1;
        float bestDays = Float.MAX_VALUE;
        for (int t = 0; t < technologyCount; t++) {
            if (technologyTrees[t] != tree || countryTechnology.acquired(t) || !this.prerequisitesAcquired(countryTechnology, t)) {
                continue;
            }
            float days = ResearchUtils.effectiveDays(technologyTimes[t], technologyYears[t], year, researchFactor, researchEfficiency);
            if (days < bestDays) {
                best = t;
                bestDays = days;
            }
        }
        return best;
    }

    private boolean prerequisitesAcquired(CountryTechnologyView countryTechnology, int technologyIndex) {
        int[] prerequisites = this.ecsConstants.technologyPrerequisites()[technologyIndex];
        for (int prerequisite : prerequisites) {
            if (!countryTechnology.acquired(prerequisite)) {
                return false;
            }
        }
        return true;
    }
}
