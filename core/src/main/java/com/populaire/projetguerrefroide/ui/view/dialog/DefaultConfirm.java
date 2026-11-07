package com.populaire.projetguerrefroide.ui.view.dialog;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Align;
import com.populaire.projetguerrefroide.ui.skin.SkinNames.FontsSkin;
import com.populaire.projetguerrefroide.ui.skin.SkinNames.Skins;
import com.populaire.projetguerrefroide.ui.skin.SkinNames.UiPopupSkin;
import com.populaire.projetguerrefroide.ui.widget.UiAnchor;
import com.populaire.projetguerrefroide.ui.widget.UiBuilder;
import com.populaire.projetguerrefroide.ui.widget.UiWindow;

public class DefaultConfirm extends UiWindow {
    private final Image background;
    private final Label title;
    private final Label description;
    private final Button agreeButton;
    private final Button declineButton;

    public DefaultConfirm(UiBuilder ui, String titleKey, String descriptionKey, TextureRegion leftFlag, TextureRegion rightFlag, PopupListener listener) {
        super(ui, 500f, 320f);
        Skin popupSkin = ui.skin(Skins.UI_POPUP);
        this.background = ui.image(this, popupSkin, UiPopupSkin.Drawables.POPUP_BG_STANDARD_FLAG_DOUBLE, 0f, 0f, UiAnchor.UPPER_LEFT);
        this.title = ui.label(this, titleKey, FontsSkin.Styles.Label.JOCKEY_18_BLUE, 0f, 9f, 500f, 32f, Align.top, UiAnchor.UPPER_LEFT, 4f, 4f);
        this.description = ui.label(this, descriptionKey, FontsSkin.Styles.Label.JOCKEY_14, 20f, 70f, 450f, 200f, Align.top, UiAnchor.UPPER_LEFT, 4f, 4f);
        this.agreeButton = ui.button(this, popupSkin, UiPopupSkin.Styles.Button.POPUP_BTN, 246f, 254f, UiAnchor.UPPER_LEFT);
        ui.buttonLabel(this.agreeButton, "OK", FontsSkin.Styles.Label.JOCKEY_16_GLOW_BLUE);
        ui.shortcut(this, this.agreeButton, Input.Keys.ENTER);
        this.declineButton = ui.button(this, popupSkin, UiPopupSkin.Styles.Button.POPUP_RED_BTN, 83f, 254f, UiAnchor.UPPER_LEFT);
        ui.buttonLabel(this.declineButton, "CANCEL", FontsSkin.Styles.Label.JOCKEY_16_GLOW_RED);
        ui.shortcut(this, this.declineButton, Input.Keys.ESCAPE);
        this.description.setWrap(true);
        PopupShields.add(ui, this, this.background, leftFlag, rightFlag);
        PopupShields.draggable(this);
        ui.onClick(this.agreeButton, listener::onOkClicked);
        ui.onClick(this.declineButton, listener::onCancelClicked);
    }
}
