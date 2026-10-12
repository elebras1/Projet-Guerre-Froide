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

            long forceTypeId = this.ecsWorld.lookup(unitJson.type());
            long unitTypeId = this.ecsWorld.entity(unitJson.unitType());

            long[] buildCostIds = new long[MAX_GOODS];
            Arrays.fill(buildCostIds, -1);
            float[] buildCostAmounts = new float[MAX_GOODS];
            this.context.fillGoodCosts(unitJson.buildCost(), buildCostIds, buildCostAmounts);
            long[] supplyCostsIds = new long[MAX_GOODS];
            Arrays.fill(supplyCostsIds, -1);
            float[] supplyCostsAmounts = new float[MAX_GOODS];
            this.context.fillGoodCosts(unitJson.supplyCost(), supplyCostsIds, supplyCostsAmounts);

            unit.set(new Unit(forceTypeId, unitTypeId, (int) unitJson.attack(), (int) unitJson.defence(), (int) unitJson.priority(), (int) unitJson.defaultOrganisation(), (int) unitJson.supplyConsumption(), (int) unitJson.weightedValue(), (int) unitJson.buildTime(), (int) unitJson.maximumSpeed(), (int) unitJson.maxStrength(), buildCostIds, buildCostAmounts, supplyCostsIds, supplyCostsAmounts));
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
                long forceTypeTagId = this.ecsWorld.lookup(leader.forceType());
                long traitId = this.ecsWorld.lookup(leader.trait());
                long leaderEntityId = this.ecsWorld.entity();
                EntityView leaderEntity = this.ecsWorld.obtainEntityView(leaderEntityId);
                leaderEntity.set(new Leader(leader.name(), skill, forceTypeTagId, traitId, countryEntityId));
            }
        } catch (Exception exception) {
            throw new RuntimeException(exception);
        }
    }
}
