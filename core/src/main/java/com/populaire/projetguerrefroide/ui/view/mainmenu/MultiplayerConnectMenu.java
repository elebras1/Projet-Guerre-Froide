package com.populaire.projetguerrefroide.ui.view.mainmenu;

import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.populaire.projetguerrefroide.ui.skin.SkinNames.FontsSkin;
import com.populaire.projetguerrefroide.ui.skin.SkinNames.Skins;
import com.populaire.projetguerrefroide.ui.skin.SkinNames.UiMainmenuSkin;
import com.populaire.projetguerrefroide.ui.widget.UiAnchor;
import com.populaire.projetguerrefroide.ui.widget.UiBuilder;
import com.populaire.projetguerrefroide.ui.widget.UiList;
import com.populaire.projetguerrefroide.ui.widget.UiWindow;

public class MultiplayerConnectMenu extends UiWindow {
    private final Image frontendConnectlogBg;
    private final UiList<ConnectionLogText> infoList;
    private final Button back;

    public MultiplayerConnectMenu(UiBuilder ui, Runnable onBack) {
        super(ui, 420f, 500f);
        Skin mainMenuSkin = ui.skin(Skins.UI_MAINMENU);
        this.frontendConnectlogBg = ui.image(this, mainMenuSkin, UiMainmenuSkin.Drawables.FRONTEND_CONNECTLOG_BG, 102f, 85f, UiAnchor.UPPER_LEFT);
        this.infoList = ui.list(this, 116f, 96f, 220f, 260f, UiAnchor.UPPER_LEFT, 2f, 4f, 4f, ConnectionLogText::new);
        this.back = ui.button(this, mainMenuSkin, UiMainmenuSkin.Styles.Button.FRONTEND_BUTTON4_BTN, 146f, 386f, UiAnchor.UPPER_LEFT);
        ui.buttonLabel(this.back, "BACK", FontsSkin.Styles.Label.FRANGOTH_BOLD);
        ui.onClick(this.back, onBack);
    }

    public void setLog(String... keys) {
        this.infoList.setRowCount(keys.length);
        for (int i = 0; i < keys.length; i++) {
            this.infoList.row(i).setText(this.ui.localise(keys[i]));
        }
    }
}
