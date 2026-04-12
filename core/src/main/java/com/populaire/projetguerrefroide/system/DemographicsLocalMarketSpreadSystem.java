package com.populaire.projetguerrefroide.system;

import com.github.elebras1.flecs.Field;
import com.github.elebras1.flecs.Iter;
import com.github.elebras1.flecs.World;
import com.populaire.projetguerrefroide.component.*;

public class DemographicsLocalMarketSpreadSystem {
    private final World ecsWorld;

    public DemographicsLocalMarketSpreadSystem(World ecsWorld, long phaseId) {
        this.ecsWorld = ecsWorld;
        ecsWorld.system("DemographicsLocalMarketSpreadSystem")
            .kind(phaseId)
            .with(LocalMarket.class)
            .with(Demographics.class)
            .iter(this::spread);
    }

    private void spread(Iter iter) {
        long ownerId = 0;
        CountryDemographicsView countryDemographics = null;

        Field<LocalMarket> localMarketField = iter.field(LocalMarket.class, 0);
        Field<Demographics> demographicsField = iter.field(Demographics.class, 1);
        for(int i = 0; i < iter.count(); i++) {
            LocalMarketView localMarket = localMarketField.getMutView(i);
            DemographicsView demographics = demographicsField.getMutView(i);

            if(localMarket.ownerId() != ownerId) {
                ownerId = localMarket.ownerId();
                countryDemographics = this.ecsWorld.obtainEntityView(ownerId).getMutView(CountryDemographics.class);
            }

            countryDemographics.totalPopulation(countryDemographics.totalPopulation() + demographics.totalPopulation());
            countryDemographics.totalEmployment(countryDemographics.totalEmployment() + demographics.totalEmployment());
            countryDemographics.averageConsciousness(0f);
            countryDemographics.averageMilitancy(0f);
            countryDemographics.averageLiteracy(0f);
            countryDemographics.totalSavings(countryDemographics.totalSavings() + demographics.totalSavings());
            for(int popTypeIndex = 0; popTypeIndex < demographics.totalByPopTypeLength(); popTypeIndex++) {
                countryDemographics.totalByPopType(popTypeIndex, countryDemographics.totalByPopType(popTypeIndex) + demographics.totalByPopType(popTypeIndex));
                countryDemographics.employmentByPopType(popTypeIndex, countryDemographics.employmentByPopType(popTypeIndex) + demographics.employmentByPopType(popTypeIndex));
                countryDemographics.consciousnessByPopType(popTypeIndex, 0f);
                countryDemographics.militancyByPopType(popTypeIndex, 0f);
                countryDemographics.literacyByPopType(popTypeIndex, 0f);
                countryDemographics.savingsByPopType(popTypeIndex, countryDemographics.savingsByPopType(popTypeIndex) + demographics.savingsByPopType(popTypeIndex));
            }
            countryDemographics.totalChildren(countryDemographics.totalChildren() + demographics.totalChildren());
            countryDemographics.totalAdults(countryDemographics.totalAdults() + demographics.totalAdults());
            countryDemographics.totalSeniors(countryDemographics.totalSeniors() + demographics.totalSeniors());
        }
    }
}
