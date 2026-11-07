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
import com.populaire.projetguerrefroide.ui.widget.UiList;
import com.populaire.projetguerrefroide.ui.widget.UiWindow;

public class DefaultPopupBig extends UiWindow {
    private final Image background;
    private final Label header;
    private final Button agreeButton;
    private final Button declineButton;
    private final Button centerOK;
    private final Label line1;
    private final UiList<TextListItem> textlist;

    public DefaultPopupBig(UiBuilder ui, boolean twoButtons, String titleKey, String headerKey, TextureRegion leftFlag, TextureRegion rightFlag, PopupListener listener) {
        super(ui, 500f, 280f);
        Skin popupSkin = ui.skin(Skins.UI_POPUP);
        this.background = ui.image(this, popupSkin, UiPopupSkin.Drawables.POPUP_BG_BIG_DOUBLE, 0f, 0f, UiAnchor.UPPER_LEFT);
        this.header = ui.label(this, headerKey, FontsSkin.Styles.Label.JOCKEY_14_DARK, 55f, 36f, 350f, -1f, Align.top, UiAnchor.UPPER_LEFT, 4f, 4f);
        this.agreeButton = ui.button(this, popupSkin, UiPopupSkin.Styles.Button.POPUP_BTN, 247f, 436f, UiAnchor.UPPER_LEFT);
        ui.buttonLabel(this.agreeButton, "OK", FontsSkin.Styles.Label.JOCKEY_16_GLOW_BLUE);
        ui.shortcut(this, this.agreeButton, Input.Keys.ENTER);
        this.declineButton = ui.button(this, popupSkin, UiPopupSkin.Styles.Button.POPUP_BTN, 83f, 436f, UiAnchor.UPPER_LEFT);
        ui.buttonLabel(this.declineButton, "CANCEL", FontsSkin.Styles.Label.JOCKEY_16_GLOW_BLUE);
        ui.shortcut(this, this.declineButton, Input.Keys.ESCAPE);
        this.centerOK = ui.button(this, popupSkin, UiPopupSkin.Styles.Button.POPUP_BTN, 164f, 436f, UiAnchor.UPPER_LEFT);
        ui.buttonLabel(this.centerOK, "OK", FontsSkin.Styles.Label.JOCKEY_16_GLOW_BLUE);
        ui.shortcut(this, this.centerOK, Input.Keys.ENTER);
        this.line1 = ui.label(this, titleKey, FontsSkin.Styles.Label.JOCKEY_18_BLUE, -90f, 13f, 650f, -1f, Align.top, UiAnchor.UPPER_LEFT, 4f, 4f);
        this.textlist = ui.list(this, 30f, 113f, 434f, 164f, UiAnchor.UPPER_LEFT, 0f, 0f, 0f, TextListItem::new);
        this.agreeButton.setVisible(twoButtons);
        this.declineButton.setVisible(twoButtons);
        this.centerOK.setVisible(!twoButtons);
        ui.onClick(this.agreeButton, listener::onOkClicked);
        ui.onClick(this.declineButton, listener::onCancelClicked);
        ui.onClick(this.centerOK, listener::onOkClicked);
        PopupShields.add(ui, this, this.background, leftFlag, rightFlag);
        PopupShields.draggable(this);
    }

    public void setLines(String... keys) {
        this.textlist.setRowCount(keys.length);
        for (int i = 0; i < keys.length; i++) {
            this.textlist.row(i).setText(this.ui.localise(keys[i]));
        }
    }
}
