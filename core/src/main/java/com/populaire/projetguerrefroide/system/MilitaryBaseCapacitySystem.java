package com.populaire.projetguerrefroide.system;

import io.github.elebras1.flecs.EntityView;
import io.github.elebras1.flecs.Field;
import io.github.elebras1.flecs.Iter;
import io.github.elebras1.flecs.Query;
import io.github.elebras1.flecs.World;
import com.populaire.projetguerrefroide.component.*;
import com.populaire.projetguerrefroide.util.EcsConstants;
import com.populaire.projetguerrefroide.util.MilitaryUtils;

public class MilitaryBaseCapacitySystem {

    private final EcsConstants ecsConstants;
    private final Query developmentQuery;
    private final Query unitQuery;

    public MilitaryBaseCapacitySystem(World ecsWorld, EcsConstants ecsConstants, long phaseId) {
        this.ecsConstants = ecsConstants;
        this.developmentQuery = ecsWorld.query()
            .with(Building.class)
            .with(DevelopmentBuildingType.class)
            .build();
        this.unitQuery = ecsWorld.query()
            .with(UnitInstance.class)
            .with(AirWing.class).optional()
            .build();
        ecsWorld.system("MilitaryBaseCapacitySystem")
            .kind(phaseId)
            .with(CountryMilitary.class)
            .run(this::measure);
    }

    private void measure(Iter iter) {
        World world = iter.world();
        while (iter.next()) {
            Field<CountryMilitary> countryMilitaryField = iter.field(CountryMilitary.class, 0);
            for (int i = 0; i < iter.count(); i++) {
                CountryMilitaryMutView countryMilitary = countryMilitaryField.getMutView(i);
                countryMilitary.navalCapacity(0f);
                countryMilitary.navalLoad(0f);
                countryMilitary.airCapacity(0f);
                countryMilitary.airLoad(0f);
            }
        }

        EntityView globalBases = world.obtainEntityView(world.lookup("global_military_facilities"));
        if (globalBases.has(GlobalMilitaryFacilities.class)) {
            GlobalMilitaryFacilitiesView bases = globalBases.getView(GlobalMilitaryFacilities.class);
            long navalBaseTypeId = bases.navalBaseTypeId();
            float navalCapacityPerLevel = bases.navalCapacityPerLevel();
            long airBaseTypeId = bases.airBaseTypeId();
            float airCapacityPerLevel = bases.airCapacityPerLevel();
            this.developmentQuery.iter(developmentIter -> {
                long buildingTypeId = developmentIter.src(1);
                boolean naval = buildingTypeId == navalBaseTypeId;
                if (!naval && buildingTypeId != airBaseTypeId) {
                    return;
                }
                float capacityPerLevel = naval ? navalCapacityPerLevel : airCapacityPerLevel;
                Field<Building> buildingField = developmentIter.field(Building.class, 0);
                for (int j = 0; j < developmentIter.count(); j++) {
                    BuildingView building = buildingField.getMutView(j);
                    if (building.size() <= 0 || building.countryId() == 0) {
                        continue;
                    }
                    CountryMilitaryMutView countryMilitary = world.obtainEntityView(building.countryId()).getMutView(CountryMilitary.class);
                    if (naval) {
                        countryMilitary.navalCapacity(countryMilitary.navalCapacity() + building.size() * capacityPerLevel);
                    } else {
                        countryMilitary.airCapacity(countryMilitary.airCapacity() + building.size() * capacityPerLevel);
                    }
                }
            });
        }

        long seaForceTag = this.ecsConstants.seaForceTag();
        this.unitQuery.iter(unitIter -> {
            boolean airWings = unitIter.isSet(1);
            Field<UnitInstance> unitInstanceField = unitIter.field(UnitInstance.class, 0);
            Field<AirWing> airWingField = airWings ? unitIter.field(AirWing.class, 1) : null;
            for (int j = 0; j < unitIter.count(); j++) {
                UnitInstanceView unitInstance = unitInstanceField.getMutView(j);
                if (airWings) {
                    AirWingView airWing = airWingField.getMutView(j);
                    CountryMilitaryMutView countryMilitary = world.obtainEntityView(airWing.countryId()).getMutView(CountryMilitary.class);
                    countryMilitary.airLoad(countryMilitary.airLoad() + 1f);
                    continue;
                }
                UnitView unit = world.obtainEntityView(unitInstance.typeId()).getView(Unit.class);
                if (unit.forceTypeTagId() != seaForceTag) {
                    continue;
                }
                long countryId = MilitaryUtils.countryId(world, unitInstance, null);
                CountryMilitaryMutView countryMilitary = world.obtainEntityView(countryId).getMutView(CountryMilitary.class);
                countryMilitary.navalLoad(countryMilitary.navalLoad() + 1f);
            }
        });
    }
}
