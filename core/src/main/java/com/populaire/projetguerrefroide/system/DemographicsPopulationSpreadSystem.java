package com.populaire.projetguerrefroide.system;

import io.github.elebras1.flecs.Field;
import io.github.elebras1.flecs.Iter;
import io.github.elebras1.flecs.World;
import com.populaire.projetguerrefroide.component.*;

public class DemographicsPopulationSpreadSystem {

    public DemographicsPopulationSpreadSystem(World ecsWorld, long phaseId) {
        ecsWorld.system("DemographicsPopulationSpreadSystem")
            .kind(phaseId)
            .with(Population.class)
            .with(PopulationType.class)
            .iter(this::spread);
    }

    private void spread(Iter iter) {
        long provinceId = 0;
        DemographicsMutView provinceDemographics = null;

        Field<PopulationType> populationTypeField = iter.field(PopulationType.class, 1);
        PopulationTypeView populationTypeData = populationTypeField.getView(0);
        int popTypeIndex = populationTypeData.index();

        Field<Population> populationField = iter.field(Population.class, 0);
        for (int i = 0; i < iter.count(); i++) {
            PopulationView population = populationField.getMutView(i);
            if (population.provinceId() != provinceId) {
                provinceId = population.provinceId();
                provinceDemographics = iter.world().obtainEntityView(provinceId).getMutView(Demographics.class);
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

            provinceDemographics.totalByPopType(popTypeIndex, provinceDemographics.totalByPopType(popTypeIndex) + population.amount());
            provinceDemographics.employmentByPopType(popTypeIndex, provinceDemographics.employmentByPopType(popTypeIndex) + population.employment());
            provinceDemographics.consciousnessByPopType(popTypeIndex, provinceDemographics.consciousnessByPopType(popTypeIndex) + population.consciousness());
            provinceDemographics.militancyByPopType(popTypeIndex, provinceDemographics.militancyByPopType(popTypeIndex) + population.militancy());
            provinceDemographics.literacyByPopType(popTypeIndex, provinceDemographics.literacyByPopType(popTypeIndex) + population.literacy());
            provinceDemographics.savingsByPopType(popTypeIndex, provinceDemographics.savingsByPopType(popTypeIndex) + population.savings());
            provinceDemographics.lifeNeedsSatisfactionByPopType(popTypeIndex, provinceDemographics.lifeNeedsSatisfactionByPopType(popTypeIndex) + population.lifeNeedsSatisfaction());
            provinceDemographics.everydayNeedsSatisfactionByPopType(popTypeIndex, provinceDemographics.everydayNeedsSatisfactionByPopType(popTypeIndex) + population.everydayNeedsSatisfaction());
            provinceDemographics.luxuryNeedsSatisfactionByPopType(popTypeIndex, provinceDemographics.luxuryNeedsSatisfactionByPopType(popTypeIndex) + population.luxuryNeedsSatisfaction());
            provinceDemographics.incomeByPopType(popTypeIndex, provinceDemographics.incomeByPopType(popTypeIndex) + population.income());
        }
    }
}
