package com.populaire.projetguerrefroide.system;

import io.github.elebras1.flecs.Field;
import io.github.elebras1.flecs.Iter;
import io.github.elebras1.flecs.World;
import com.populaire.projetguerrefroide.component.*;

public class EconomyBuildingScaleSystem {

    public EconomyBuildingScaleSystem(World ecsWorld, long phaseId) {
        ecsWorld.system("EconomyBuildingScaleSystem")
            .kind(phaseId)
            .with(EconomyBuilding.class)
            .iter(this::updateScale);
    }

    private void updateScale(Iter iter) {
        Field<EconomyBuilding> economyBuildingField = iter.field(EconomyBuilding.class, 0);
        for (int i = 0; i < iter.count(); i++) {
            EconomyBuildingView economyBuilding = economyBuildingField.getMutView(i);
            float profit = economyBuilding.profit();

            float newScale;
            if (profit > 0) {
                newScale = Math.min(1.0f, economyBuilding.scale() + 0.05f);
            } else {
                newScale = Math.max(0.0f, economyBuilding.scale() - 0.05f);
            }
            economyBuilding.scale(newScale);
        }
    }
}
