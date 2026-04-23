package com.populaire.projetguerrefroide.system;

import com.github.elebras1.flecs.EntityView;
import com.github.elebras1.flecs.Field;
import com.github.elebras1.flecs.Iter;
import com.github.elebras1.flecs.World;
import com.populaire.projetguerrefroide.component.*;

public class DemographicsPopulationSpreadSystem {

    public DemographicsPopulationSpreadSystem(World ecsWorld, long phaseId) {
        ecsWorld.system("DemographicsPopulationSpreadSystem")
            .kind(phaseId)
            .with(Population.class)
            .iter(this::spread);
    }

    private void spread(Iter iter) {
        long provinceId = 0;
        ProvinceView province = null;
        DemographicsView provinceDemographics = null;

        Field<Population> populationField = iter.field(Population.class, 0);
        for (int i = 0; i < iter.count(); i++) {
            PopulationView population = populationField.getMutView(i);
            if (population.provinceId() != provinceId) {
                provinceId = population.provinceId();
                EntityView provinceEntity = iter.world().obtainEntityView(provinceId);
                province = provinceEntity.getMutView(Province.class);
                provinceDemographics = provinceEntity.getMutView(Demographics.class);
            }

            provinceDemographics.totalPopulation(provinceDemographics.totalPopulation() + population.amount());
            provinceDemographics.totalEmployment(provinceDemographics.totalEmployment() + population.employment());
            provinceDemographics.consciousness(provinceDemographics.consciousness() + population.consciousness());
            provinceDemographics.militancy(provinceDemographics.militancy() + population.militancy());
            provinceDemographics.literacy(provinceDemographics.literacy() + population.literacy());
            provinceDemographics.lifeNeedsSatisfaction(provinceDemographics.lifeNeedsSatisfaction() + population.lifeNeedsSatisfaction());
            provinceDemographics.everydayNeedsSatisfaction(provinceDemographics.everydayNeedsSatisfaction() + population.everydayNeedsSatisfaction());
            provinceDemographics.luxuryNeedsSatisfaction(provinceDemographics.luxuryNeedsSatisfaction() + population.luxuryNeedsSatisfaction());
            provinceDemographics.savings(provinceDemographics.savings() + population.savings());

            provinceDemographics.totalByPopType(population.index(), provinceDemographics.totalByPopType(population.index()) + population.amount());
            provinceDemographics.employmentByPopType(population.index(), provinceDemographics.employmentByPopType(population.index()) + population.employment());
            provinceDemographics.consciousnessByPopType(population.index(), provinceDemographics.consciousnessByPopType(population.index()) + population.consciousness());
            provinceDemographics.militancyByPopType(population.index(), provinceDemographics.militancyByPopType(population.index()) + population.militancy());
            provinceDemographics.literacyByPopType(population.index(), provinceDemographics.literacyByPopType(population.index()) + population.literacy());
            provinceDemographics.savingsByPopType(population.index(), provinceDemographics.savingsByPopType(population.index()) + population.savings());
            provinceDemographics.lifeNeedsSatisfactionByPopType(population.index(), provinceDemographics.lifeNeedsSatisfactionByPopType(population.index()) + population.lifeNeedsSatisfaction());
            provinceDemographics.everydayNeedsSatisfactionByPopType(population.index(), provinceDemographics.everydayNeedsSatisfactionByPopType(population.index()) + population.everydayNeedsSatisfaction());
            provinceDemographics.luxuryNeedsSatisfactionByPopType(population.index(), provinceDemographics.luxuryNeedsSatisfactionByPopType(population.index()) + population.luxuryNeedsSatisfaction());

            provinceDemographics.totalChildren(province.childrenAmount());
            provinceDemographics.totalAdults(province.adultsAmount());
            provinceDemographics.totalSeniors(province.seniorsAmount());
        }
    }
}
