package com.populaire.projetguerrefroide.system;

import io.github.elebras1.flecs.Field;
import io.github.elebras1.flecs.Iter;
import io.github.elebras1.flecs.World;
import com.populaire.projetguerrefroide.component.*;

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
        Field<RegionInstance> regionInstanceField = iter.field(RegionInstance.class, 0);
        Field<RegionInstanceIncome> regionInstanceIncomeField = iter.field(RegionInstanceIncome.class, 1);
        Field<RegionDemographics> regionDemographicsField = iter.field(RegionDemographics.class, 2);
        for (int i = 0; i < iter.count(); i++) {
            RegionInstanceMutView regionInstance = regionInstanceField.getMutView(i);
            RegionInstanceIncomeView regionInstanceIncome = regionInstanceIncomeField.getMutView(i);
            RegionDemographicsView regionDemographics = regionDemographicsField.getMutView(i);
            for (int p = 0; p < regionInstance.wagePremiumByPopTypeLength(); p++) {
                float demand = regionInstanceIncome.solventWorkerDemandByPopType(p);
                float available = regionDemographics.totalByPopType(p);
                float excessDemand = demand > 0f ? Math.clamp((demand - available) / Math.max(demand, available), -1f, 1f) : -1f;
                float wageMultiplier = (1f + regionInstance.wagePremiumByPopType(p)) * (1f + WAGE_ADJUSTMENT_RATE * excessDemand);
                regionInstance.wagePremiumByPopType(p, Math.max(0f, wageMultiplier - 1f));
            }
        }
    }
}
