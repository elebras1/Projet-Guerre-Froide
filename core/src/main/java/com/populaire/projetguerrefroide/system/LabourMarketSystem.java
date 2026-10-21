package com.populaire.projetguerrefroide.system;

import io.github.elebras1.flecs.Field;
import io.github.elebras1.flecs.Iter;
import io.github.elebras1.flecs.World;
import com.populaire.projetguerrefroide.component.*;
import com.populaire.projetguerrefroide.util.EmploymentUtils;

import static com.populaire.projetguerrefroide.util.Constants.WAGE_ADJUSTMENT_RATE;

public class LabourMarketSystem {

    public LabourMarketSystem(World ecsWorld, long phaseId) {
        ecsWorld.system("LabourMarketSystem")
            .kind(phaseId)
            .with(RegionInstance.class)
            .with(RegionInstanceIncome.class)
            .with(RegionDemographics.class)
            .multiThreaded()
            .iter(this::adjust);
    }

    private void adjust(Iter iter) {
        World world = iter.world();
        GlobalPopulationTypeView globalPopulationType = world.obtainEntityView(world.lookup("global_population_type")).getView(GlobalPopulationType.class);
        int statePostIndex = globalPopulationType.statePostPopTypeIndex();
        Field<RegionInstance> regionInstanceField = iter.field(RegionInstance.class, 0);
        Field<RegionInstanceIncome> regionInstanceIncomeField = iter.field(RegionInstanceIncome.class, 1);
        Field<RegionDemographics> regionDemographicsField = iter.field(RegionDemographics.class, 2);
        float[] baseWorkforceShares = EmploymentUtils.baseWorkforceShares(world);
        long countryId = 0;
        float workforceShareModifier = 0f;
        for (int i = 0; i < iter.count(); i++) {
            RegionInstanceMutView regionInstance = regionInstanceField.getMutView(i);
            if (regionInstance.ownerId() != countryId) {
                countryId = regionInstance.ownerId();
                CountryPopulationPolicyView populationPolicy = world.obtainEntityView(countryId).getView(CountryPopulationPolicy.class);
                workforceShareModifier = populationPolicy.workforceShareModifier();
            }
            RegionInstanceIncomeView regionInstanceIncome = regionInstanceIncomeField.getMutView(i);
            RegionDemographicsView regionDemographics = regionDemographicsField.getMutView(i);
            for (int p = 0; p < regionInstance.wagePremiumByPopTypeLength(); p++) {
                if (p == statePostIndex) {
                    continue;
                }
                float demand = regionInstanceIncome.solventWorkerDemandByPopType(p);
                float available = regionDemographics.totalByPopType(p) * EmploymentUtils.activeShare(baseWorkforceShares[p], workforceShareModifier);
                regionInstance.vacanciesByPopType(p, Math.max(0f, demand - available));
                regionInstance.applicantsByPopType(p, 0f);
                float excessDemand = demand > 0f ? Math.clamp((demand - available) / Math.max(demand, available), -1f, 1f) : -1f;
                float wageMultiplier = (1f + regionInstance.wagePremiumByPopType(p)) * (1f + WAGE_ADJUSTMENT_RATE * excessDemand);
                regionInstance.wagePremiumByPopType(p, Math.max(0f, wageMultiplier - 1f));
            }
        }
    }
}
