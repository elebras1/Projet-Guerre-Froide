package com.populaire.projetguerrefroide.system;

import com.github.elebras1.flecs.Field;
import com.github.elebras1.flecs.Iter;
import com.github.elebras1.flecs.World;
import com.populaire.projetguerrefroide.component.*;

public class DemographicsProvinceSpreadSystem {

    public DemographicsProvinceSpreadSystem(World ecsWorld, long phaseId) {
        ecsWorld.system("DemographicsProvinceSpreadSystem")
            .kind(phaseId)
            .with(Demographics.class)
            .with(GeoHierarchy.class)
            .iter(this::spread);
    }

    private void spread(Iter iter) {
        long ownerId = 0;
        DemographicsView localMarketDemographics = null;

        Field<Demographics> demographicsViewField = iter.field(Demographics.class, 0);
        Field<GeoHierarchy> geoHierarchyViewField = iter.field(GeoHierarchy.class, 1);
        for(int i = 0; i < iter.count(); i++) {
            DemographicsView demographics = demographicsViewField.getMutView(i);
            GeoHierarchyView geoHierarchy = geoHierarchyViewField.getMutView(i);

            if(geoHierarchy.localMarketId() != ownerId) {
                ownerId = geoHierarchy.localMarketId();
                localMarketDemographics = iter.world().obtainEntityView(ownerId).getMutView(Demographics.class);
            }

            localMarketDemographics.totalPopulation(localMarketDemographics.totalPopulation() + demographics.totalPopulation());
            localMarketDemographics.totalEmployment(localMarketDemographics.totalEmployment() + demographics.totalEmployment());
            localMarketDemographics.consciousness(localMarketDemographics.consciousness() + demographics.consciousness());
            localMarketDemographics.militancy(localMarketDemographics.militancy() + demographics.militancy());
            localMarketDemographics.literacy(localMarketDemographics.literacy() + demographics.literacy());
            localMarketDemographics.savings(localMarketDemographics.savings() + demographics.savings());
            localMarketDemographics.lifeNeedsSatisfaction(localMarketDemographics.lifeNeedsSatisfaction() + demographics.lifeNeedsSatisfaction());
            localMarketDemographics.everydayNeedsSatisfaction(localMarketDemographics.everydayNeedsSatisfaction() + demographics.everydayNeedsSatisfaction());
            localMarketDemographics.luxuryNeedsSatisfaction(localMarketDemographics.luxuryNeedsSatisfaction() + demographics.luxuryNeedsSatisfaction());

            for(int popTypeIndex = 0; popTypeIndex < demographics.totalByPopTypeLength(); popTypeIndex++) {
                localMarketDemographics.totalByPopType(popTypeIndex, localMarketDemographics.totalByPopType(popTypeIndex) + demographics.totalByPopType(popTypeIndex));
                localMarketDemographics.employmentByPopType(popTypeIndex, localMarketDemographics.employmentByPopType(popTypeIndex) + demographics.employmentByPopType(popTypeIndex));
                localMarketDemographics.consciousnessByPopType(popTypeIndex, localMarketDemographics.consciousnessByPopType(popTypeIndex) + demographics.consciousnessByPopType(popTypeIndex));
                localMarketDemographics.militancyByPopType(popTypeIndex, localMarketDemographics.militancyByPopType(popTypeIndex) + demographics.militancyByPopType(popTypeIndex));
                localMarketDemographics.literacyByPopType(popTypeIndex, localMarketDemographics.literacyByPopType(popTypeIndex) + demographics.literacyByPopType(popTypeIndex));
                localMarketDemographics.savingsByPopType(popTypeIndex, localMarketDemographics.savingsByPopType(popTypeIndex) + demographics.savingsByPopType(popTypeIndex));
                localMarketDemographics.lifeNeedsSatisfactionByPopType(popTypeIndex, localMarketDemographics.lifeNeedsSatisfactionByPopType(popTypeIndex) + demographics.lifeNeedsSatisfactionByPopType(popTypeIndex));
                localMarketDemographics.everydayNeedsSatisfactionByPopType(popTypeIndex, localMarketDemographics.everydayNeedsSatisfactionByPopType(popTypeIndex) + demographics.everydayNeedsSatisfactionByPopType(popTypeIndex));
                localMarketDemographics.luxuryNeedsSatisfactionByPopType(popTypeIndex, localMarketDemographics.luxuryNeedsSatisfactionByPopType(popTypeIndex) + demographics.luxuryNeedsSatisfactionByPopType(popTypeIndex));
            }

            localMarketDemographics.totalChildren(localMarketDemographics.totalChildren() + demographics.totalChildren());
            localMarketDemographics.totalAdults(localMarketDemographics.totalAdults() + demographics.totalAdults());
            localMarketDemographics.totalSeniors(localMarketDemographics.totalSeniors() + demographics.totalSeniors());
        }
    }
}
