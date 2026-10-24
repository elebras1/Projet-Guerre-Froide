package com.populaire.projetguerrefroide.system;

import io.github.elebras1.flecs.EntityView;
import io.github.elebras1.flecs.Field;
import io.github.elebras1.flecs.Iter;
import io.github.elebras1.flecs.World;
import com.populaire.projetguerrefroide.component.*;
import com.populaire.projetguerrefroide.util.ModifierUtils;

import static com.populaire.projetguerrefroide.util.Constants.PRODUCTION_MODIFIER;

public class InfrastructureInitializationSystem {

    public InfrastructureInitializationSystem(World ecsWorld, long phaseId) {
        ecsWorld.system("InfrastructureInitializationSystem")
            .kind(phaseId)
            .with(Building.class)
            .with(DevelopmentBuildingType.class)
            .iter(this::initialize);
    }

    private void initialize(Iter iter) {
        World world = iter.world();
        float productionModifier = ModifierUtils.value(world.obtainEntityView(iter.src(1)), world.lookup(PRODUCTION_MODIFIER));
        if (productionModifier <= 0f) {
            return;
        }
        Field<Building> buildingField = iter.field(Building.class, 0);
        for (int i = 0; i < iter.count(); i++) {
            BuildingView building = buildingField.getMutView(i);
            EntityView province = world.obtainEntityView(building.parentId());
            if (!province.has(ProvinceInfrastructure.class)) {
                continue;
            }
            float bonus = building.size() * productionModifier;
            ProvinceInfrastructureMutView provinceInfrastructure = province.getMutView(ProvinceInfrastructure.class);
            provinceInfrastructure.productionBonus(provinceInfrastructure.productionBonus() + bonus);
            ProvinceView provinceData = province.getView(Province.class);
            if (provinceData.regionInstanceId() != 0) {
                RegionInstanceMutView regionInstance = world.obtainEntityView(provinceData.regionInstanceId()).getMutView(RegionInstance.class);
                regionInstance.infrastructureBonusSum(regionInstance.infrastructureBonusSum() + bonus);
            }
        }
    }
}
