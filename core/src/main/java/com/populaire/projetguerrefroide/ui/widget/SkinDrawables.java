package com.populaire.projetguerrefroide.ui.widget;

import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.GdxRuntimeException;

public final class SkinDrawables {
    private SkinDrawables() {
    }

    public static Drawable optional(Skin skin, String name, Drawable fallback) {
        try {
            return skin.getDrawable(name);
        } catch (GdxRuntimeException exception) {
            return fallback;
        }
    }
}
