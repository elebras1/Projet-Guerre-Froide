package com.populaire.projetguerrefroide.util;

import io.github.elebras1.flecs.EntityView;
import com.populaire.projetguerrefroide.component.Modifiers;
import com.populaire.projetguerrefroide.component.ModifiersView;

public class ModifierUtils {

    public static float value(EntityView entity, long modifierId) {
        if (!entity.has(Modifiers.class)) {
            return 0f;
        }
        ModifiersView modifiers = entity.getView(Modifiers.class);
        for (int m = 0; m < modifiers.tagIdsLength(); m++) {
            long tagId = modifiers.tagIds(m);
            if (tagId <= 0) {
                break;
            }
            if (tagId == modifierId) {
                return modifiers.values(m);
            }
        }
        return 0f;
    }
}
