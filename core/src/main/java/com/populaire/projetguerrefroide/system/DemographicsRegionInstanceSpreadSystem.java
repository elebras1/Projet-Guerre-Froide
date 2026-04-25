package com.populaire.projetguerrefroide.system;

import com.github.elebras1.flecs.Field;
import com.github.elebras1.flecs.Iter;
import com.github.elebras1.flecs.World;
import com.populaire.projetguerrefroide.component.*;

public class DemographicsRegionInstanceSpreadSystem {

    public DemographicsRegionInstanceSpreadSystem(World ecsWorld, long phaseId) {
        ecsWorld.system("DemographicsRegionInstanceSpreadSystem")
            .kind(phaseId)
            .with(RegionInstance.class)
            .with(Demographics.class)
            .iter(this::spread);
    }

    private void spread(Iter iter) {
        long ownerId = 0;
        CountryDemographicsView countryDemographics = null;

        Field<RegionInstance> regionInstanceField = iter.field(RegionInstance.class, 0);
        Field<Demographics> demographicsField = iter.field(Demographics.class, 1);
        for(int i = 0; i < iter.count(); i++) {
            RegionInstanceView regionInstance = regionInstanceField.getMutView(i);
            DemographicsView demographics = demographicsField.getMutView(i);

            if(regionInstance.ownerId() != ownerId) {
                ownerId = regionInstance.ownerId();
                countryDemographics = iter.world().obtainEntityView(ownerId).getMutView(CountryDemographics.class);
            }

            countryDemographics.totalPopulation(countryDemographics.totalPopulation() + demographics.totalPopulation());
            countryDemographics.totalEmployment(countryDemographics.totalEmployment() + demographics.totalEmployment());
            countryDemographics.consciousness(countryDemographics.consciousness() + demographics.consciousness());
            countryDemographics.militancy(countryDemographics.militancy() + demographics.militancy());
            countryDemographics.literacy(countryDemographics.literacy() + demographics.literacy());
            countryDemographics.savings(countryDemographics.savings() + demographics.savings());
            countryDemographics.lifeNeedsSatisfaction(countryDemographics.lifeNeedsSatisfaction() + demographics.lifeNeedsSatisfaction());
            countryDemographics.everydayNeedsSatisfaction(countryDemographics.everydayNeedsSatisfaction() + demographics.everydayNeedsSatisfaction());
            countryDemographics.luxuryNeedsSatisfaction(countryDemographics.luxuryNeedsSatisfaction() + demographics.luxuryNeedsSatisfaction());

            for(int popTypeIndex = 0; popTypeIndex < demographics.totalByPopTypeLength(); popTypeIndex++) {
                countryDemographics.totalByPopType(popTypeIndex, countryDemographics.totalByPopType(popTypeIndex) + demographics.totalByPopType(popTypeIndex));
                countryDemographics.employmentByPopType(popTypeIndex, countryDemographics.employmentByPopType(popTypeIndex) + demographics.employmentByPopType(popTypeIndex));
                countryDemographics.consciousnessByPopType(popTypeIndex, countryDemographics.consciousnessByPopType(popTypeIndex) + demographics.consciousnessByPopType(popTypeIndex));
                countryDemographics.militancyByPopType(popTypeIndex, countryDemographics.militancyByPopType(popTypeIndex) + demographics.militancyByPopType(popTypeIndex));
                countryDemographics.literacyByPopType(popTypeIndex, countryDemographics.literacyByPopType(popTypeIndex) + demographics.literacyByPopType(popTypeIndex));
                countryDemographics.savingsByPopType(popTypeIndex, countryDemographics.savingsByPopType(popTypeIndex) + demographics.savingsByPopType(popTypeIndex));
                countryDemographics.lifeNeedsSatisfactionByPopType(popTypeIndex, countryDemographics.lifeNeedsSatisfactionByPopType(popTypeIndex) + demographics.lifeNeedsSatisfactionByPopType(popTypeIndex));
                countryDemographics.everydayNeedsSatisfactionByPopType(popTypeIndex, countryDemographics.everydayNeedsSatisfactionByPopType(popTypeIndex) + demographics.everydayNeedsSatisfactionByPopType(popTypeIndex));
                countryDemographics.luxuryNeedsSatisfactionByPopType(popTypeIndex, countryDemographics.luxuryNeedsSatisfactionByPopType(popTypeIndex) + demographics.luxuryNeedsSatisfactionByPopType(popTypeIndex));
            }
            countryDemographics.totalChildren(countryDemographics.totalChildren() + demographics.totalChildren());
            countryDemographics.totalAdults(countryDemographics.totalAdults() + demographics.totalAdults());
            countryDemographics.totalSeniors(countryDemographics.totalSeniors() + demographics.totalSeniors());
        }
    }
}
