package com.populaire.projetguerrefroide.system;

import com.github.elebras1.flecs.EntityView;
import com.github.elebras1.flecs.Field;
import com.github.elebras1.flecs.Iter;
import com.github.elebras1.flecs.World;
import com.populaire.projetguerrefroide.component.*;

public class DemographicsPopulationSpreadSystem {
    private final World ecsWorld;

    public DemographicsPopulationSpreadSystem(World ecsWorld, long phaseId) {
        this.ecsWorld = ecsWorld;
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
        for(int i = 0; i < iter.count(); i++) {
            PopulationView population = populationField.getMutView(i);
            if (population.provinceId() != provinceId) {
                provinceId = population.provinceId();
                EntityView provinceEntity = this.ecsWorld.obtainEntityView(provinceId);
                province = provinceEntity.getMutView(Province.class);
                provinceDemographics = provinceEntity.getMutView(Demographics.class);
            }
            provinceDemographics.totalPopulation(provinceDemographics.totalPopulation() + population.amount());
            provinceDemographics.totalEmployment(provinceDemographics.totalEmployment() + population.employment());
            provinceDemographics.averageConsciousness(0f);
            provinceDemographics.averageMilitancy(0f);
            provinceDemographics.averageLiteracy(0f);
            provinceDemographics.totalSavings(provinceDemographics.totalSavings() + population.savings());
            provinceDemographics.totalByPopType(population.index(), provinceDemographics.totalByPopType(population.index()) + population.amount());
            provinceDemographics.employmentByPopType(population.index(), provinceDemographics.employmentByPopType(population.index()) + population.employment());
            provinceDemographics.consciousnessByPopType(population.index(), 0f);
            provinceDemographics.militancyByPopType(population.index(), 0f);
            provinceDemographics.literacyByPopType(population.index(), 0f);
            provinceDemographics.savingsByPopType(population.index(), provinceDemographics.savingsByPopType(population.index()) + population.savings());
            provinceDemographics.totalChildren(province.childrenAmount());
            provinceDemographics.totalAdults(province.adultsAmount());
            provinceDemographics.totalSeniors(province.seniorsAmount());
        }
    }
}
