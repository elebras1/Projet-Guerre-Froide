package com.populaire.projetguerrefroide.system;

import com.github.elebras1.flecs.Field;
import com.github.elebras1.flecs.Iter;
import com.github.elebras1.flecs.World;
import com.populaire.projetguerrefroide.component.*;

public class DemographicsProvinceSpreadSystem {

    public DemographicsProvinceSpreadSystem(World ecsWorld, long phaseId) {
        ecsWorld.system("DemographicsProvinceSpreadSystem")
            .kind(phaseId)
            .with(Province.class)
            .with(Demographics.class)
            .iter(this::spread);
    }

    private void spread(Iter iter) {
        long ownerId = 0;
        DemographicsView regionInstanceDemographics = null;

        Field<Province> provinceField = iter.field(Province.class, 0);
        Field<Demographics> demographicsField = iter.field(Demographics.class, 1);
        for(int i = 0; i < iter.count(); i++) {
            ProvinceView province = provinceField.getMutView(i);
            DemographicsView demographics = demographicsField.getMutView(i);

            if(province.regionInstanceId() != ownerId) {
                ownerId = province.regionInstanceId();
                regionInstanceDemographics = iter.world().obtainEntityView(ownerId).getMutView(Demographics.class);
            }

            regionInstanceDemographics.totalPopulation(regionInstanceDemographics.totalPopulation() + demographics.totalPopulation());
            regionInstanceDemographics.totalEmployment(regionInstanceDemographics.totalEmployment() + demographics.totalEmployment());
            regionInstanceDemographics.consciousness(regionInstanceDemographics.consciousness() + demographics.consciousness());
            regionInstanceDemographics.militancy(regionInstanceDemographics.militancy() + demographics.militancy());
            regionInstanceDemographics.literacy(regionInstanceDemographics.literacy() + demographics.literacy());
            regionInstanceDemographics.savings(regionInstanceDemographics.savings() + demographics.savings());
            regionInstanceDemographics.lifeNeedsSatisfaction(regionInstanceDemographics.lifeNeedsSatisfaction() + demographics.lifeNeedsSatisfaction());
            regionInstanceDemographics.everydayNeedsSatisfaction(regionInstanceDemographics.everydayNeedsSatisfaction() + demographics.everydayNeedsSatisfaction());
            regionInstanceDemographics.luxuryNeedsSatisfaction(regionInstanceDemographics.luxuryNeedsSatisfaction() + demographics.luxuryNeedsSatisfaction());

            for(int popTypeIndex = 0; popTypeIndex < demographics.totalByPopTypeLength(); popTypeIndex++) {
                regionInstanceDemographics.totalByPopType(popTypeIndex, regionInstanceDemographics.totalByPopType(popTypeIndex) + demographics.totalByPopType(popTypeIndex));
                regionInstanceDemographics.employmentByPopType(popTypeIndex, regionInstanceDemographics.employmentByPopType(popTypeIndex) + demographics.employmentByPopType(popTypeIndex));
                regionInstanceDemographics.consciousnessByPopType(popTypeIndex, regionInstanceDemographics.consciousnessByPopType(popTypeIndex) + demographics.consciousnessByPopType(popTypeIndex));
                regionInstanceDemographics.militancyByPopType(popTypeIndex, regionInstanceDemographics.militancyByPopType(popTypeIndex) + demographics.militancyByPopType(popTypeIndex));
                regionInstanceDemographics.literacyByPopType(popTypeIndex, regionInstanceDemographics.literacyByPopType(popTypeIndex) + demographics.literacyByPopType(popTypeIndex));
                regionInstanceDemographics.savingsByPopType(popTypeIndex, regionInstanceDemographics.savingsByPopType(popTypeIndex) + demographics.savingsByPopType(popTypeIndex));
                regionInstanceDemographics.lifeNeedsSatisfactionByPopType(popTypeIndex, regionInstanceDemographics.lifeNeedsSatisfactionByPopType(popTypeIndex) + demographics.lifeNeedsSatisfactionByPopType(popTypeIndex));
                regionInstanceDemographics.everydayNeedsSatisfactionByPopType(popTypeIndex, regionInstanceDemographics.everydayNeedsSatisfactionByPopType(popTypeIndex) + demographics.everydayNeedsSatisfactionByPopType(popTypeIndex));
                regionInstanceDemographics.luxuryNeedsSatisfactionByPopType(popTypeIndex, regionInstanceDemographics.luxuryNeedsSatisfactionByPopType(popTypeIndex) + demographics.luxuryNeedsSatisfactionByPopType(popTypeIndex));
            }

            regionInstanceDemographics.totalChildren(regionInstanceDemographics.totalChildren() + demographics.totalChildren());
            regionInstanceDemographics.totalAdults(regionInstanceDemographics.totalAdults() + demographics.totalAdults());
            regionInstanceDemographics.totalSeniors(regionInstanceDemographics.totalSeniors() + demographics.totalSeniors());
        }
    }
}
