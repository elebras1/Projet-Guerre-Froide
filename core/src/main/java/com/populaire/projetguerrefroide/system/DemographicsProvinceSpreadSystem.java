package com.populaire.projetguerrefroide.system;

import com.github.elebras1.flecs.Field;
import com.github.elebras1.flecs.Iter;
import com.github.elebras1.flecs.World;
import com.populaire.projetguerrefroide.component.*;

public class DemographicsProvinceSpreadSystem {
    private final World ecsWorld;

    public DemographicsProvinceSpreadSystem(World ecsWorld, long phaseId) {
        this.ecsWorld = ecsWorld;
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
                localMarketDemographics = this.ecsWorld.obtainEntityView(ownerId).getMutView(Demographics.class);
            }

            localMarketDemographics.totalPopulation(localMarketDemographics.totalPopulation() + demographics.totalPopulation());
            localMarketDemographics.totalEmployment(localMarketDemographics.totalEmployment() + demographics.totalEmployment());
            localMarketDemographics.averageConsciousness(0f);
            localMarketDemographics.averageMilitancy(0f);
            localMarketDemographics.averageLiteracy(0f);
            localMarketDemographics.totalSavings(localMarketDemographics.totalSavings() + demographics.totalSavings());
            for(int popTypeIndex = 0; popTypeIndex < demographics.totalByPopTypeLength(); popTypeIndex++) {
                localMarketDemographics.totalByPopType(popTypeIndex, localMarketDemographics.totalByPopType(popTypeIndex) + demographics.totalByPopType(popTypeIndex));
                localMarketDemographics.employmentByPopType(popTypeIndex, localMarketDemographics.employmentByPopType(popTypeIndex) + demographics.employmentByPopType(popTypeIndex));
                localMarketDemographics.consciousnessByPopType(popTypeIndex, 0f);
                localMarketDemographics.militancyByPopType(popTypeIndex, 0f);
                localMarketDemographics.literacyByPopType(popTypeIndex, 0f);
                localMarketDemographics.savingsByPopType(popTypeIndex, localMarketDemographics.savingsByPopType(popTypeIndex) + demographics.savingsByPopType(popTypeIndex));
            }
            localMarketDemographics.totalChildren(localMarketDemographics.totalChildren() + demographics.totalChildren());
            localMarketDemographics.totalAdults(localMarketDemographics.totalAdults() + demographics.totalAdults());
            localMarketDemographics.totalSeniors(localMarketDemographics.totalSeniors() + demographics.totalSeniors());
        }
    }
}
