package com.populaire.projetguerrefroide.system;

import com.github.elebras1.flecs.EntityView;
import com.github.elebras1.flecs.Field;
import com.github.elebras1.flecs.Iter;
import com.github.elebras1.flecs.World;
import com.populaire.projetguerrefroide.component.*;

public class DemographicsSpreadSystem {
    private final World ecsWorld;

    public DemographicsSpreadSystem(World ecsWorld, long phaseId) {
        this.ecsWorld = ecsWorld;
        ecsWorld.system("DemographicsSpreadSystem")
            .kind(phaseId)
            .with(Population.class)
            .iter(this::spread);
    }

    public void spread(Iter iter) {
        long provinceId = 0;
        ProvinceView province = null;
        GeoHierarchyView geoHierarchy = null;
        DemographicsView provinceDemographics = null;

        long localMarketId = 0;
        DemographicsView localMarketDemographics = null;

        long countryId = 0;
        CountryDemographicsView countryDemographics = null;

        Field<Population> populationField = iter.field(Population.class, 0);
        for(int i = 0; i < iter.count(); i++) {
            PopulationView population = populationField.getMutView(i);
            if(population.provinceId() != provinceId) {
                provinceId = population.provinceId();
                EntityView provinceEntity = this.ecsWorld.obtainEntityView(provinceId);
                province = provinceEntity.getMutView(Province.class);
                geoHierarchy = provinceEntity.getMutView(GeoHierarchy.class);
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
            provinceDemographics.savingsByPopType(population.index(), provinceDemographics.savingsByPopType(population.index()) + population.savings() * population.amount());
            provinceDemographics.totalChildren(province.childrenAmount());
            provinceDemographics.totalAdults(province.childrenAmount());
            provinceDemographics.totalSeniors(province.childrenAmount());

            if(geoHierarchy.localMarketId() != localMarketId) {
                localMarketId = geoHierarchy.localMarketId();
                EntityView localMarketEntity = this.ecsWorld.obtainEntityView(localMarketId);
                localMarketDemographics = localMarketEntity.getMutView(Demographics.class);
            }
            localMarketDemographics.totalPopulation(localMarketDemographics.totalPopulation() + population.amount());
            localMarketDemographics.totalEmployment(localMarketDemographics.totalEmployment() + population.employment());
            localMarketDemographics.averageConsciousness(0f);
            localMarketDemographics.averageMilitancy(0f);
            localMarketDemographics.averageLiteracy(0f);
            localMarketDemographics.totalSavings(localMarketDemographics.totalSavings() + population.savings());
            localMarketDemographics.totalByPopType(population.index(), localMarketDemographics.totalByPopType(population.index()) + population.amount());
            localMarketDemographics.employmentByPopType(population.index(), localMarketDemographics.employmentByPopType(population.index()) + population.employment());
            localMarketDemographics.consciousnessByPopType(population.index(), 0f);
            localMarketDemographics.militancyByPopType(population.index(), 0f);
            localMarketDemographics.literacyByPopType(population.index(), 0f);
            localMarketDemographics.savingsByPopType(population.index(), localMarketDemographics.savingsByPopType(population.index()) + population.savings() * population.amount());
            localMarketDemographics.totalChildren(province.childrenAmount());
            localMarketDemographics.totalAdults(province.childrenAmount());
            localMarketDemographics.totalSeniors(province.childrenAmount());


            if(province.ownerId() != countryId) {
                countryId = province.ownerId();
                EntityView countryEntity = this.ecsWorld.obtainEntityView(countryId);
                countryDemographics = countryEntity.getMutView(CountryDemographics.class);
            }
            countryDemographics.totalPopulation(countryDemographics.totalPopulation() + population.amount());
            countryDemographics.totalEmployment(countryDemographics.totalEmployment() + population.employment());
            countryDemographics.averageConsciousness(0f);
            countryDemographics.averageMilitancy(0f);
            countryDemographics.averageLiteracy(0f);
            countryDemographics.totalSavings(countryDemographics.totalSavings() + population.savings());
            countryDemographics.totalByPopType(population.index(), countryDemographics.totalByPopType(population.index()) + population.amount());
            countryDemographics.employmentByPopType(population.index(), countryDemographics.employmentByPopType(population.index()) + population.employment());
            countryDemographics.consciousnessByPopType(population.index(), 0f);
            countryDemographics.militancyByPopType(population.index(), 0f);
            countryDemographics.literacyByPopType(population.index(), 0f);
            countryDemographics.savingsByPopType(population.index(), countryDemographics.savingsByPopType(population.index()) + population.savings() * population.amount());
            countryDemographics.totalChildren(province.childrenAmount());
            countryDemographics.totalAdults(province.childrenAmount());
            countryDemographics.totalSeniors(province.childrenAmount());
        }

    }
}
