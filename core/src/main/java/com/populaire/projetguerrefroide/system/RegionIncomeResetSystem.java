package com.populaire.projetguerrefroide.system;

import com.github.elebras1.flecs.Iter;
import com.github.elebras1.flecs.World;
import com.populaire.projetguerrefroide.component.RegionInstanceIncome;

public class RegionIncomeResetSystem {

    public RegionIncomeResetSystem(World ecsWorld, long phaseId) {
        ecsWorld.system("RegionIncomeResetSystem")
            .kind(phaseId)
            .with(RegionInstanceIncome.class)
            .iter(this::reset);
    }

    private void reset(Iter iter) {
        iter.table().resetColumn(RegionInstanceIncome.class);
    }
}
