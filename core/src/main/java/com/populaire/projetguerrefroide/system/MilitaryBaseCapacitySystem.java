package com.populaire.projetguerrefroide.system;

import io.github.elebras1.flecs.EntityView;
import io.github.elebras1.flecs.Field;
import io.github.elebras1.flecs.Iter;
import io.github.elebras1.flecs.Query;
import io.github.elebras1.flecs.World;
import io.github.elebras1.flecs.callback.IterCallback;
import com.populaire.projetguerrefroide.component.*;
import com.populaire.projetguerrefroide.simulation.EcsConstants;
import com.populaire.projetguerrefroide.util.MilitaryUtils;

public class MilitaryBaseCapacitySystem {

    private final EcsConstants ecsConstants;
    private final IterCallback readUnitReader = this::readUnit;
    private long seaForceTag;
    private final IterCallback readBaseReader = this::readBase;
    private World world;
    private long navalBaseTypeId;
    private float navalCapacityPerLevel;
    private long airBaseTypeId;
    private float airCapacityPerLevel;
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
        this.world = world;
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

        EntityView globalBases = world.obtainEntityView(this.ecsConstants.globalMilitaryFacilities());
        if (globalBases.has(GlobalMilitaryFacilities.class)) {
            GlobalMilitaryFacilitiesView bases = globalBases.getView(GlobalMilitaryFacilities.class);
            long navalBaseTypeId = bases.navalBaseTypeId();
            float navalCapacityPerLevel = bases.navalCapacityPerLevel();
            long airBaseTypeId = bases.airBaseTypeId();
            float airCapacityPerLevel = bases.airCapacityPerLevel();
            this.navalBaseTypeId = navalBaseTypeId;
            this.navalCapacityPerLevel = navalCapacityPerLevel;
            this.airBaseTypeId = airBaseTypeId;
            this.airCapacityPerLevel = airCapacityPerLevel;
            this.developmentQuery.iter(this.readBaseReader);
        }

        long seaForceTag = this.ecsConstants.seaForceTag();
        this.seaForceTag = seaForceTag;
        this.unitQuery.iter(this.readUnitReader);
    }

    private void readBase(Iter developmentIter) {
        World world = this.world;
        long navalBaseTypeId = this.navalBaseTypeId;
        float navalCapacityPerLevel = this.navalCapacityPerLevel;
        long airBaseTypeId = this.airBaseTypeId;
        float airCapacityPerLevel = this.airCapacityPerLevel;
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
    }

    private void readUnit(Iter unitIter) {
        World world = this.world;
        long seaForceTag = this.seaForceTag;
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
    }
}
