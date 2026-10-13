package com.populaire.projetguerrefroide.loader;

import com.github.tommyettinger.ds.*;
import com.populaire.projetguerrefroide.component.*;
import com.populaire.projetguerrefroide.dao.json.*;
import com.populaire.projetguerrefroide.util.EcsConstants;
import io.github.elebras1.flecs.EntityView;
import io.github.elebras1.flecs.World;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static com.populaire.projetguerrefroide.util.Constants.*;

public final class MilitaryLoader {
    private final WorldLoadingContext context;
    private final World ecsWorld;
    private final EcsConstants ecsConstants;

    public MilitaryLoader(WorldLoadingContext context) {
        this.context = context;
        this.ecsWorld = context.ecsWorld();
        this.ecsConstants = context.ecsConstants();
    }

    public void loadTraits(Map<String, Map<String, Double>> traits) {
        try {
            for(var traitEntry : traits.entrySet()) {
                var modifierEntry = traitEntry.getValue().entrySet().iterator().next();
                long modifierTagId = this.ecsWorld.entity(modifierEntry.getKey());
                long traitEntityId = this.ecsWorld.entity(traitEntry.getKey());
                EntityView traitEntity = this.ecsWorld.obtainEntityView(traitEntityId);
                float[] modifierValues = new float[MAX_MODIFIERS];
                long[] modifierTagIds = new long[MAX_MODIFIERS];
                Arrays.fill(modifierTagIds, -1);
                modifierValues[0] = modifierEntry.getValue().floatValue();
                modifierTagIds[0] = modifierTagId;
                traitEntity.set(new Modifiers(modifierValues, modifierTagIds));
            }
        } catch (Exception exception) {
            throw new RuntimeException(exception);
        }
    }

    public void loadUnits(Map<String, UnitJson> units) {
        Map<String, UnitJson> unitsByName = new ObjectObjectMap<>(POP_TYPE_COUNT, 1f);
        try {
            for(var unitEntry : units.entrySet()) {
                unitsByName.put(unitEntry.getKey(), unitEntry.getValue());
            }

            for (Map.Entry<String, UnitJson> unit : unitsByName.entrySet()) {
                this.loadUnit(unit.getKey(), unit.getValue());
            }
        } catch (Exception exception) {
            throw new RuntimeException(exception);
        }
    }

    private void loadUnit(String unitName, UnitJson unitJson) {
        try {
            long unitId = this.ecsWorld.entity(unitName);
            EntityView unit = this.ecsWorld.obtainEntityView(unitId);

            long forceTypeId = this.ecsConstants.getForceTypeTag(unitJson.type());
            long unitTypeId = this.ecsWorld.entity(unitJson.unitType());

            long[] buildCostIds = new long[MAX_GOODS];
            Arrays.fill(buildCostIds, -1);
            float[] buildCostAmounts = new float[MAX_GOODS];
            this.context.fillGoodCosts(unitJson.buildCost(), buildCostIds, buildCostAmounts);
            int[] supplyCostGoodIndexes = new int[MAX_GOODS];
            Arrays.fill(supplyCostGoodIndexes, -1);
            long[] supplyCostsIds = new long[MAX_GOODS];
            Arrays.fill(supplyCostsIds, -1);
            float[] supplyCostsAmounts = new float[MAX_GOODS];
            this.context.fillGoods(unitJson.supplyCost(), supplyCostGoodIndexes, supplyCostsIds, supplyCostsAmounts);
            for(int i = 0; i < unitJson.supplyCost().size(); i++) {
                if(supplyCostGoodIndexes[i] == -1) {
                    throw new IllegalArgumentException("Unknown supply good for unit " + unitName);
                }
            }

            unit.set(new Unit(forceTypeId, unitTypeId, (int) unitJson.attack(), (int) unitJson.defence(), (int) unitJson.priority(), (int) unitJson.defaultOrganisation(), (int) unitJson.supplyConsumption(), (int) unitJson.weightedValue(), (int) unitJson.buildTime(), (int) unitJson.maximumSpeed(), (int) unitJson.maxStrength(), buildCostIds, buildCostAmounts, supplyCostGoodIndexes, supplyCostsIds, supplyCostsAmounts));
        } catch (Exception exception) {
            throw new RuntimeException(exception);
        }
    }

    public void loadLeaders(List<LeadersJson> leaders) {
        try {
            for (LeadersJson leadersJson : leaders) {
                this.loadLeader(leadersJson);
            }
        } catch (Exception exception) {
            throw new RuntimeException(exception);
        }
    }

    private void loadLeader(LeadersJson leaders) {
        try {
            long countryEntityId = this.ecsWorld.lookup(leaders.country());
            for(LeaderJson leader : leaders.leaders()) {
                int skill = (byte) leader.skill();
                long forceTypeTagId = this.ecsConstants.getForceTypeTag(leader.forceType());
                long traitId = this.ecsWorld.lookup(leader.trait());
                long leaderEntityId = this.ecsWorld.entity();
                EntityView leaderEntity = this.ecsWorld.obtainEntityView(leaderEntityId);
                leaderEntity.set(new Leader(leader.name(), skill, forceTypeTagId, traitId, countryEntityId));
            }
        } catch (Exception exception) {
            throw new RuntimeException(exception);
        }
    }

    public void loadForces(Map<String, CountryHistoryJson> countriesHistory) {
        try {
            for(var countryHistoryEntry : countriesHistory.entrySet()) {
                this.loadCountryForces(countryHistoryEntry.getKey(), countryHistoryEntry.getValue());
            }
        } catch (Exception exception) {
            throw new RuntimeException(exception);
        }
    }

    private void loadCountryForces(String countryNameId, CountryHistoryJson countryHistory) {
        long countryId = this.ecsWorld.lookup(countryNameId);
        if(countryId == 0) {
            throw new IllegalArgumentException("Unknown country " + countryNameId);
        }
        long landForceTag = this.ecsConstants.landForceTag();
        long seaForceTag = this.ecsConstants.seaForceTag();
        long airForceTag = this.ecsConstants.airForceTag();

        if(countryHistory.armies() != null) {
            for(ArmyJson army : countryHistory.armies()) {
                long provinceId = this.lookupProvince(army.province(), army.nameId());
                long armyId = this.createFormation(countryId, landForceTag, provinceId, 0, army.nameId());
                for(UnitInstanceJson regiment : army.regiments()) {
                    this.createUnitInstance(armyId, landForceTag, regiment.type(), regiment.strength(), regiment.experience(), regiment.nameId());
                }
            }
        }

        if(countryHistory.navies() != null) {
            for(NavyJson navy : countryHistory.navies()) {
                long provinceId = this.lookupProvince(navy.province(), navy.nameId());
                long baseId = this.lookupProvince(navy.base(), navy.nameId());
                long navyId = this.createFormation(countryId, seaForceTag, provinceId, baseId, navy.nameId());
                for(UnitInstanceJson ship : navy.ships()) {
                    this.createUnitInstance(navyId, seaForceTag, ship.type(), ship.strength(), ship.experience(), ship.nameId());
                }
            }
        }

        if(countryHistory.airWings() != null) {
            for(AirWingJson airWing : countryHistory.airWings()) {
                long baseId = this.lookupProvince(airWing.base(), airWing.nameId());
                long airWingId = this.createUnitInstance(0, airForceTag, airWing.type(), airWing.strength(), airWing.experience(), airWing.nameId());
                this.ecsWorld.obtainEntityView(airWingId).set(new AirWing(countryId, baseId));
            }
        }
    }

    private long createFormation(long countryId, long forceTypeTagId, long provinceId, long baseId, String nameId) {
        long formationId = this.ecsWorld.entity();
        this.ecsWorld.obtainEntityView(formationId).set(new Formation(countryId, forceTypeTagId, provinceId, baseId, nameId));
        return formationId;
    }

    private long createUnitInstance(long formationId, long forceTypeTagId, String typeName, double strength, double experience, String nameId) {
        long typeId = this.ecsWorld.lookup(typeName);
        if(typeId == 0) {
            throw new IllegalArgumentException("Unknown unit type " + typeName + " for " + nameId);
        }
        Unit unit = this.ecsWorld.obtainEntityView(typeId).get(Unit.class);
        if(unit.forceTypeTagId() != forceTypeTagId) {
            throw new IllegalArgumentException("Unit type " + typeName + " does not belong to the force of " + nameId);
        }
        long unitInstanceId = this.ecsWorld.entity();
        this.ecsWorld.obtainEntityView(unitInstanceId).set(new UnitInstance(formationId, typeId, (float) strength, unit.defaultOrganisation(), (float) experience, nameId));
        return unitInstanceId;
    }

    private long lookupProvince(long provinceNameId, String nameId) {
        long provinceId = this.ecsWorld.lookup(String.valueOf(provinceNameId));
        if(provinceId == 0) {
            throw new IllegalArgumentException("Unknown province " + provinceNameId + " for " + nameId);
        }
        return provinceId;
    }
}
