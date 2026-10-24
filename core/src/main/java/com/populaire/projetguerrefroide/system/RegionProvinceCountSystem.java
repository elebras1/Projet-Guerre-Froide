package com.populaire.projetguerrefroide.system;

import io.github.elebras1.flecs.Field;
import io.github.elebras1.flecs.Iter;
import io.github.elebras1.flecs.World;
import com.populaire.projetguerrefroide.component.*;

public class RegionProvinceCountSystem {

    public RegionProvinceCountSystem(World ecsWorld, long phaseId) {
        ecsWorld.system("RegionProvinceCountSystem")
            .kind(phaseId)
            .with(Province.class)
            .with(ProvinceInfrastructure.class)
            .iter(this::count);
    }

    private void count(Iter iter) {
        Field<Province> provinceField = iter.field(Province.class, 0);
        for (int i = 0; i < iter.count(); i++) {
            ProvinceView province = provinceField.getMutView(i);
            if (province.regionInstanceId() == 0) {
                continue;
            }
            RegionInstanceMutView regionInstance = iter.world().obtainEntityView(province.regionInstanceId()).getMutView(RegionInstance.class);
            regionInstance.provinceCount(regionInstance.provinceCount() + 1);
        }
    }
}
