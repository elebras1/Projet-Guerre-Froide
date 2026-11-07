package com.populaire.projetguerrefroide.ui.view.menubar;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.populaire.projetguerrefroide.ui.skin.SkinNames.FontsSkin;
import com.populaire.projetguerrefroide.ui.skin.SkinNames.Skins;
import com.populaire.projetguerrefroide.ui.skin.SkinNames.UiMinimapSkin;
import com.populaire.projetguerrefroide.ui.widget.UiAnchor;
import com.populaire.projetguerrefroide.ui.widget.UiBuilder;
import com.populaire.projetguerrefroide.ui.widget.UiWindow;

public class ChatWindow extends UiWindow {
    private final Image chatBg;
    private final Button none;
    private final Button allies;
    private final Button enemies;
    private final Button all;
    private final Button closebutton;
    private final TextField chatMessage;

    public ChatWindow(UiBuilder ui) {
        super(ui, 720f, 150f);
        Skin minimapSkin = ui.skin(Skins.UI_MINIMAP);
        this.chatBg = ui.image(this, minimapSkin, UiMinimapSkin.Drawables.BG_INGAME_CHAT, 0f, 0f, UiAnchor.UPPER_LEFT);
        this.none = ui.button(this, minimapSkin, UiMinimapSkin.Styles.Button.SMALL_BUTTON_BTN, 5f, 16f, UiAnchor.UPPER_LEFT);
        ui.buttonLabel(this.none, "CHAT_NONE", FontsSkin.Styles.Label.ARIAL_11_BOLD_BLACK);
        this.allies = ui.button(this, minimapSkin, UiMinimapSkin.Styles.Button.SMALL_BUTTON_BTN, 5f, 41f, UiAnchor.UPPER_LEFT);
        ui.buttonLabel(this.allies, "CHAT_ALLIES", FontsSkin.Styles.Label.ARIAL_11_BOLD_BLACK);
        this.enemies = ui.button(this, minimapSkin, UiMinimapSkin.Styles.Button.SMALL_BUTTON_BTN, 5f, 66f, UiAnchor.UPPER_LEFT);
        ui.buttonLabel(this.enemies, "CHAT_FOES", FontsSkin.Styles.Label.ARIAL_11_BOLD_BLACK);
        this.all = ui.button(this, minimapSkin, UiMinimapSkin.Styles.Button.SMALL_BUTTON_BTN, 5f, 91f, UiAnchor.UPPER_LEFT);
        ui.buttonLabel(this.all, "CHAT_ALL", FontsSkin.Styles.Label.ARIAL_11_BOLD_BLACK);
        this.closebutton = ui.button(this, minimapSkin, UiMinimapSkin.Styles.Button.CLOSE_INGAME_CHAT_BTN, 666f, 8f, UiAnchor.UPPER_LEFT);
        ui.shortcut(this, this.closebutton, Input.Keys.ESCAPE);
        this.chatMessage = ui.textField(this, null, null, "", FontsSkin.Styles.Label.ARIAL12_BOLD, 90f, 82f, 560f, 32f, UiAnchor.UPPER_LEFT, 8f, 8f);
        ui.onClick(this.closebutton, () -> this.setVisible(false));
    }
}
