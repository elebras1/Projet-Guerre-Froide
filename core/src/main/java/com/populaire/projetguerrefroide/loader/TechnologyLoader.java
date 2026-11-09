package com.populaire.projetguerrefroide.loader;

import com.populaire.projetguerrefroide.component.EconomyBuildingType;
import com.populaire.projetguerrefroide.component.EconomyBuildingTypeView;
import com.populaire.projetguerrefroide.component.Modifiers;
import com.populaire.projetguerrefroide.component.Technology;
import com.populaire.projetguerrefroide.component.TechnologyProduction;
import com.populaire.projetguerrefroide.dao.json.TechnologyEffectsJson;
import com.populaire.projetguerrefroide.dao.json.TechnologyJson;
import com.populaire.projetguerrefroide.system.technology.PolicyModifier;
import io.github.elebras1.flecs.EntityView;
import io.github.elebras1.flecs.World;

import java.util.Arrays;
import java.util.Map;

import static com.populaire.projetguerrefroide.simulation.Constants.*;

public final class TechnologyLoader {
    private final World ecsWorld;
    private final WorldLoadingContext context;
    private final long[] technologyIds;
    private int technologyCount;

    public TechnologyLoader(WorldLoadingContext context) {
        this.ecsWorld = context.ecsWorld();
        this.context = context;
        this.technologyIds = new long[MAX_TECHNOLOGIES];
    }

    public void loadTechnologies(Map<String, TechnologyJson> technologies) {
        try {
            int index = 0;
            for (String technologyId : technologies.keySet()) {
                if (index >= MAX_TECHNOLOGIES) {
                    throw new IllegalStateException("Too many technologies: " + technologies.size());
                }
                this.technologyIds[index] = this.ecsWorld.entity(technologyId);
                index++;
            }
            this.technologyCount = index;

            index = 0;
            for (Map.Entry<String, TechnologyJson> technologyEntry : technologies.entrySet()) {
                TechnologyJson technology = technologyEntry.getValue();
                long[] prerequisiteIds = new long[MAX_TECHNOLOGY_PREREQUISITES];
                int prerequisiteIndex = 0;
                for (String prerequisite : technology.requires()) {
                    long prerequisiteId = this.ecsWorld.lookup(prerequisite);
                    if (prerequisiteId == 0) {
                        throw new IllegalArgumentException("Unknown technology prerequisite: " + prerequisite);
                    }
                    prerequisiteIds[prerequisiteIndex] = prerequisiteId;
                    prerequisiteIndex++;
                }
                int tree = switch (technology.tree()) {
                    case "production" -> TECHNOLOGY_TREE_PRODUCTION;
                    case "military" -> TECHNOLOGY_TREE_MILITARY;
                    case "society" -> TECHNOLOGY_TREE_SOCIETY;
                    default -> throw new IllegalArgumentException("Unknown technology tree: " + technology.tree());
                };
                EntityView technologyEntity = this.ecsWorld.obtainEntityView(this.technologyIds[index]);
                technologyEntity.set(new Technology(index, tree, technology.era(), technology.year(), technology.time(), prerequisiteIds));
                TechnologyEffectsJson effects = technology.effects();
                if (effects != null && effects.modifiers() != null) {
                    float[] modifierValues = new float[MAX_MODIFIERS];
                    long[] modifierTagIds = new long[MAX_MODIFIERS];
                    Arrays.fill(modifierTagIds, -1);
                    int modifierIndex = 0;
                    for (Map.Entry<String, Double> modifierEntry : effects.modifiers().entrySet()) {
                        PolicyModifier.forTechnology(modifierEntry.getKey());
                        modifierValues[modifierIndex] = modifierEntry.getValue().floatValue();
                        modifierTagIds[modifierIndex] = this.ecsWorld.entity(modifierEntry.getKey());
                        modifierIndex++;
                    }
                    technologyEntity.set(new Modifiers(modifierValues, modifierTagIds));
                }
                if (effects != null && (effects.rgoProductivity() != null || effects.factoryProductivity() != null || effects.productionMethods() != null)) {
                    technologyEntity.set(this.production(technologyEntry.getKey(), effects));
                }
                index++;
            }
        } catch (Exception exception) {
            throw new RuntimeException(exception);
        }
    }

    private TechnologyProduction production(String technologyName, TechnologyEffectsJson effects) {
        int[] rgoGoodIndexes = new int[MAX_TECHNOLOGY_EFFECTS];
        float[] rgoProductivities = new float[MAX_TECHNOLOGY_EFFECTS];
        int[] factoryTypeIndexes = new int[MAX_TECHNOLOGY_EFFECTS];
        float[] factoryProductivities = new float[MAX_TECHNOLOGY_EFFECTS];
        int[] recipeTypeIndexes = new int[MAX_TECHNOLOGY_EFFECTS];
        Arrays.fill(rgoGoodIndexes, -1);
        Arrays.fill(factoryTypeIndexes, -1);
        Arrays.fill(recipeTypeIndexes, -1);
        if (effects.rgoProductivity() != null) {
            int slot = 0;
            for (Map.Entry<String, Double> entry : effects.rgoProductivity().entrySet()) {
                this.checkSlot(technologyName, slot);
                long goodId = this.ecsWorld.lookup(entry.getKey());
                if (goodId == 0) {
                    throw new IllegalArgumentException("Unknown good in " + technologyName + ": " + entry.getKey());
                }
                rgoGoodIndexes[slot] = this.context.getGoodIndex(goodId);
                rgoProductivities[slot] = entry.getValue().floatValue();
                slot++;
            }
        }
        if (effects.factoryProductivity() != null) {
            int slot = 0;
            for (Map.Entry<String, Double> entry : effects.factoryProductivity().entrySet()) {
                this.checkSlot(technologyName, slot);
                factoryTypeIndexes[slot] = this.economyBuildingTypeIndex(technologyName, entry.getKey());
                factoryProductivities[slot] = entry.getValue().floatValue();
                slot++;
            }
        }
        if (effects.productionMethods() != null) {
            int slot = 0;
            for (String buildingType : effects.productionMethods()) {
                this.checkSlot(technologyName, slot);
                int typeIndex = this.economyBuildingTypeIndex(technologyName, buildingType);
                long buildingTypeId = this.ecsWorld.lookup(buildingType);
                if (this.ecsWorld.obtainEntityView(buildingTypeId).<EconomyBuildingTypeView>getView(EconomyBuildingType.class).advancedGoodInputIndexes(0) < 0) {
                    throw new IllegalArgumentException("Building without advanced recipe in " + technologyName + ": " + buildingType);
                }
                recipeTypeIndexes[slot] = typeIndex;
                slot++;
            }
        }
        return new TechnologyProduction(rgoGoodIndexes, rgoProductivities, factoryTypeIndexes, factoryProductivities, recipeTypeIndexes);
    }

    private int economyBuildingTypeIndex(String technologyName, String buildingType) {
        long buildingTypeId = this.ecsWorld.lookup(buildingType);
        if (buildingTypeId == 0 || !this.ecsWorld.obtainEntityView(buildingTypeId).has(EconomyBuildingType.class)) {
            throw new IllegalArgumentException("Unknown economy building type in " + technologyName + ": " + buildingType);
        }
        return this.ecsWorld.obtainEntityView(buildingTypeId).<EconomyBuildingTypeView>getView(EconomyBuildingType.class).typeIndex();
    }

    private void checkSlot(String technologyName, int slot) {
        if (slot >= MAX_TECHNOLOGY_EFFECTS) {
            throw new IllegalArgumentException("Too many production effects in " + technologyName);
        }
    }

    public long[] technologyIds() {
        return this.technologyIds;
    }

    public int technologyCount() {
        return this.technologyCount;
    }
}
