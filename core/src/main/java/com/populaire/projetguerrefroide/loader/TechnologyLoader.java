package com.populaire.projetguerrefroide.loader;

import com.populaire.projetguerrefroide.component.Modifiers;
import com.populaire.projetguerrefroide.component.Technology;
import com.populaire.projetguerrefroide.dao.json.TechnologyEffectsJson;
import com.populaire.projetguerrefroide.dao.json.TechnologyJson;
import io.github.elebras1.flecs.EntityView;
import io.github.elebras1.flecs.World;

import java.util.Arrays;
import java.util.Map;

import static com.populaire.projetguerrefroide.simulation.Constants.*;

public final class TechnologyLoader {
    private final World ecsWorld;
    private final long[] technologyIds;
    private int technologyCount;

    public TechnologyLoader(WorldLoadingContext context) {
        this.ecsWorld = context.ecsWorld();
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
                        modifierValues[modifierIndex] = modifierEntry.getValue().floatValue();
                        modifierTagIds[modifierIndex] = this.ecsWorld.entity(modifierEntry.getKey());
                        modifierIndex++;
                    }
                    technologyEntity.set(new Modifiers(modifierValues, modifierTagIds));
                }
                index++;
            }
        } catch (Exception exception) {
            throw new RuntimeException(exception);
        }
    }

    public long[] technologyIds() {
        return this.technologyIds;
    }

    public int technologyCount() {
        return this.technologyCount;
    }
}
