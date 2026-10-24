package com.populaire.projetguerrefroide.util;

import io.github.elebras1.flecs.EntityView;
import io.github.elebras1.flecs.World;
import com.populaire.projetguerrefroide.component.*;

import static com.populaire.projetguerrefroide.util.Constants.PRODUCTION_MODIFIER;

public class InfrastructureUtils {

    public static float productionModifier(World world, long buildingTypeId) {
        return ModifierUtils.value(world.obtainEntityView(buildingTypeId), world.lookup(PRODUCTION_MODIFIER));
    }

    public static void levelUp(World world, EcsConstants ecsConstants, long buildingId, long buildingTypeId) {
        EntityView building = world.obtainEntityView(buildingId);
        BuildingMutView buildingData = building.getMutView(Building.class);
        buildingData.size(buildingData.size() + 1);
        building.add(ecsConstants.leveledUp());
        float productionModifier = productionModifier(world, buildingTypeId);
        EntityView province = world.obtainEntityView(buildingData.parentId());
        if (productionModifier <= 0f || !province.has(ProvinceInfrastructure.class)) {
            return;
        }
        ProvinceInfrastructureMutView provinceInfrastructure = province.getMutView(ProvinceInfrastructure.class);
        provinceInfrastructure.productionBonus(provinceInfrastructure.productionBonus() + productionModifier);
        ProvinceView provinceData = province.getView(Province.class);
        if (provinceData.regionInstanceId() != 0) {
            RegionInstanceMutView regionInstance = world.obtainEntityView(provinceData.regionInstanceId()).getMutView(RegionInstance.class);
            regionInstance.infrastructureBonusSum(regionInstance.infrastructureBonusSum() + productionModifier);
        }
    }

    public static void levelDown(World world, long buildingId, long buildingTypeId) {
        EntityView building = world.obtainEntityView(buildingId);
        BuildingMutView buildingData = building.getMutView(Building.class);
        if (buildingData.size() <= 0) {
            return;
        }
        buildingData.size(buildingData.size() - 1);
        float productionModifier = productionModifier(world, buildingTypeId);
        EntityView province = world.obtainEntityView(buildingData.parentId());
        if (productionModifier <= 0f || !province.has(ProvinceInfrastructure.class)) {
            return;
        }
        ProvinceInfrastructureMutView provinceInfrastructure = province.getMutView(ProvinceInfrastructure.class);
        provinceInfrastructure.productionBonus(provinceInfrastructure.productionBonus() - productionModifier);
        ProvinceView provinceData = province.getView(Province.class);
        if (provinceData.regionInstanceId() != 0) {
            RegionInstanceMutView regionInstance = world.obtainEntityView(provinceData.regionInstanceId()).getMutView(RegionInstance.class);
            regionInstance.infrastructureBonusSum(regionInstance.infrastructureBonusSum() - productionModifier);
        }
    }
}
