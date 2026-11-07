package com.populaire.projetguerrefroide.ui.view.dialog;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.utils.DragListener;
import com.populaire.projetguerrefroide.ui.skin.SkinNames.Skins;
import com.populaire.projetguerrefroide.ui.skin.SkinNames.UiSkin;
import com.populaire.projetguerrefroide.ui.widget.FlagImage;
import com.populaire.projetguerrefroide.ui.widget.UiAnchor;
import com.populaire.projetguerrefroide.ui.widget.UiBuilder;
import com.populaire.projetguerrefroide.ui.widget.UiWindow;

final class PopupShields {

    private PopupShields() {
    }

    static void add(UiBuilder ui, UiWindow popup, Image background, TextureRegion left, TextureRegion right) {
        Skin uiSkin = ui.skin(Skins.UI);
        FlagImage leftShield = new FlagImage(uiSkin.getRegion(UiSkin.Drawables.SHIELD_BIG_OVERLAY), uiSkin.getRegion(UiSkin.Drawables.SHIELD_BIG));
        FlagImage rightShield = new FlagImage(uiSkin.getRegion(UiSkin.Drawables.SHIELD_BIG_OVERLAY), uiSkin.getRegion(UiSkin.Drawables.SHIELD_BIG));
        leftShield.setFlag(left);
        rightShield.setFlag(right);
        float top = 75f - leftShield.getHeight();
        ui.add(popup, leftShield, 9f, top, UiAnchor.UPPER_LEFT);
        ui.add(popup, rightShield, background.getWidth() - 72f, top, UiAnchor.UPPER_LEFT);
    }

    static void draggable(UiWindow popup) {
        popup.setTouchable(Touchable.enabled);
        popup.addListener(new DragListener() {
            private float startX;
            private float startY;

            @Override
            public void dragStart(InputEvent event, float x, float y, int pointer) {
                this.startX = x;
                this.startY = y;
            }

            @Override
            public void drag(InputEvent event, float x, float y, int pointer) {
                float newX = MathUtils.clamp(popup.getX() + x - this.startX, 0f, popup.getStage().getWidth() - popup.getWidth());
                float newY = MathUtils.clamp(popup.getY() + y - this.startY, 0f, popup.getStage().getHeight() - popup.getHeight());
                popup.setPosition(newX, newY);
            }
        });
    }
}
