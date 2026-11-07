package com.populaire.projetguerrefroide.ui.view.menubar;

import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Align;
import com.populaire.projetguerrefroide.ui.skin.SkinNames.FontsSkin;
import com.populaire.projetguerrefroide.ui.skin.SkinNames.Skins;
import com.populaire.projetguerrefroide.ui.skin.SkinNames.UiMainmenuIgSkin;
import com.populaire.projetguerrefroide.ui.widget.UiAnchor;
import com.populaire.projetguerrefroide.ui.widget.UiBuilder;
import com.populaire.projetguerrefroide.ui.widget.UiWindow;

public class MessageEntry extends UiWindow {
    private final Button message;
    private final Label text;
    private final Button messageiconPause;
    private final Button messageiconLog;
    private final Button messageiconDialog;
    private final Button messageiconMap;

    public MessageEntry(UiBuilder ui) {
        super(ui, 468f, 20f);
        Skin ingameSkin = ui.skin(Skins.UI_MAINMENU_IG);
        this.message = ui.flatButton(this, ingameSkin, UiMainmenuIgSkin.Drawables.MESSAGESETTINGS_LISTBUTTON, 0f, 2f, UiAnchor.UPPER_LEFT);
        this.text = ui.label(this, "", FontsSkin.Styles.Label.ARIAL12, 8f, 4f, 390f, 16f, Align.topLeft, UiAnchor.UPPER_LEFT);
        this.messageiconPause = ui.button(this, ingameSkin, UiMainmenuIgSkin.Styles.Button.MESSAGEICON_PAUSE_TGL, 390f, 2f, UiAnchor.UPPER_LEFT);
        this.messageiconLog = ui.button(this, ingameSkin, UiMainmenuIgSkin.Styles.Button.MESSAGEICON_LOG_TGL, 410f, 2f, UiAnchor.UPPER_LEFT);
        this.messageiconDialog = ui.button(this, ingameSkin, UiMainmenuIgSkin.Styles.Button.MESSAGEICON_DIALOG_TGL, 430f, 2f, UiAnchor.UPPER_LEFT);
        this.messageiconMap = ui.button(this, ingameSkin, UiMainmenuIgSkin.Styles.Button.MESSAGEICON_MAP_TGL, 448f, 2f, UiAnchor.UPPER_LEFT);
    }
}
