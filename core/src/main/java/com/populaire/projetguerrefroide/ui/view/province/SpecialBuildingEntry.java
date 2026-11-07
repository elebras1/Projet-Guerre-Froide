package com.populaire.projetguerrefroide.ui.view.province;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.populaire.projetguerrefroide.ui.skin.SkinNames.Skins;
import com.populaire.projetguerrefroide.ui.skin.SkinNames.UiProvinceSkin;
import com.populaire.projetguerrefroide.ui.widget.UiAnchor;
import com.populaire.projetguerrefroide.ui.widget.UiBuilder;
import com.populaire.projetguerrefroide.ui.widget.UiWindow;

public class SpecialBuildingEntry extends UiWindow {
    private final Button specBuildingDemolishButton;
    private final Button specBuildingButton;
    private final Image constructionProgress;
    private String tooltip;

    public SpecialBuildingEntry(UiBuilder ui) {
        super(ui, 59f, 54f);
        Skin provinceSkin = ui.skin(Skins.UI_PROVINCE);
        this.specBuildingDemolishButton = ui.button(this, provinceSkin, UiProvinceSkin.Styles.Button.DEMOLISH_SPEC_BUILDING_BTN, 43f, 1f, UiAnchor.UPPER_LEFT);
        this.specBuildingButton = ui.flatButton(this, provinceSkin, UiProvinceSkin.Drawables.EMPTY_SPEC_BUILDING, 1f, -2f, UiAnchor.UPPER_LEFT);
        this.constructionProgress = ui.image(this, provinceSkin, UiProvinceSkin.Drawables.CONSTRUCTION_PROGRESS_CURRENT, 4f, 40f, UiAnchor.UPPER_LEFT);
        // slot1 : GFX_empty_slot (sans asset PGF)
        this.specBuildingDemolishButton.setVisible(false);
        this.constructionProgress.setVisible(false);
    }

    public void setBuilding(Drawable image, String tooltip) {
        this.specBuildingButton.getStyle().up = image;
        this.specBuildingButton.setSize(image.getMinWidth(), image.getMinHeight());
        this.specBuildingButton.invalidateHierarchy();
        this.tooltip = tooltip;
    }

    public void setTooltipListener(ProvincePanelListener listener) {
        this.specBuildingButton.addListener(new InputListener() {
            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                if (tooltip != null) {
                    listener.updateHoverTooltip(tooltip);
                }
            }

            @Override
            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                listener.hideUiTooltip();
            }
        });
    }
}
