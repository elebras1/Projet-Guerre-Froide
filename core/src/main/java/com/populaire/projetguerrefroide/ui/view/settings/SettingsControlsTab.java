package com.populaire.projetguerrefroide.ui.view.settings;

import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.utils.Align;
import com.populaire.projetguerrefroide.ui.skin.SkinNames.FontsSkin;
import com.populaire.projetguerrefroide.ui.skin.SkinNames.Skins;
import com.populaire.projetguerrefroide.ui.skin.SkinNames.UiMainmenuSkin;
import com.populaire.projetguerrefroide.ui.widget.UiAnchor;
import com.populaire.projetguerrefroide.ui.widget.UiBuilder;
import com.populaire.projetguerrefroide.ui.widget.UiWindow;

public class SettingsControlsTab extends UiWindow {
    private final Image controlsBackground;
    private final Label scrollspeedLabel;
    private final Slider scrollspeedSlider;
    private final Label zoomspeedLabel;
    private final Slider zoomspeedSlider;

    public SettingsControlsTab(UiBuilder ui) {
        super(ui, 370f, 598f);
        Skin mainMenuSkin = ui.skin(Skins.UI_MAINMENU);
        this.controlsBackground = ui.image(this, mainMenuSkin, UiMainmenuSkin.Drawables.FRONTEND_SETTINGS_CONTROLS_BG, 0f, 0f, UiAnchor.UPPER_LEFT);
        this.scrollspeedLabel = ui.label(this, "SCROLL_SPEED", FontsSkin.Styles.Label.JOCKEY_14_GLOW_BLUE, 36f, 103f, 306f, 18f, Align.top, UiAnchor.UPPER_LEFT);
        this.scrollspeedSlider = ui.slider(this, 33f, 131f, 305f, 12f, UiAnchor.UPPER_LEFT, 0f, 100f, 1f, 20f);
        this.zoomspeedLabel = ui.label(this, "ZOOM_SPEED", FontsSkin.Styles.Label.JOCKEY_14_GLOW_BLUE, 34f, 164f, 306f, 18f, Align.top, UiAnchor.UPPER_LEFT);
        this.zoomspeedSlider = ui.slider(this, 33f, 192f, 305f, 12f, UiAnchor.UPPER_LEFT, 0f, 100f, 1f, 20f);
        this.scrollspeedLabel.setVisible(false);
        this.scrollspeedSlider.setVisible(false);
        this.zoomspeedLabel.setVisible(false);
        this.zoomspeedSlider.setVisible(false);
    }
}
