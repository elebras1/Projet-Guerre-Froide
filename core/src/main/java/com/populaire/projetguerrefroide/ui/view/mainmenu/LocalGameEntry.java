package com.populaire.projetguerrefroide.ui.view.mainmenu;

import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.populaire.projetguerrefroide.ui.skin.SkinNames.FontsSkin;
import com.populaire.projetguerrefroide.ui.skin.SkinNames.Skins;
import com.populaire.projetguerrefroide.ui.skin.SkinNames.UiMainmenuSkin;
import com.populaire.projetguerrefroide.ui.widget.UiAnchor;
import com.populaire.projetguerrefroide.ui.widget.UiBuilder;
import com.populaire.projetguerrefroide.ui.widget.UiWindow;

public class LocalGameEntry extends UiWindow {
    private final Button game;
    private final Label name;

    public LocalGameEntry(UiBuilder ui) {
        super(ui, 128f, 21f);
        Skin mainMenuSkin = ui.skin(Skins.UI_MAINMENU);
        this.game = ui.button(this, mainMenuSkin, UiMainmenuSkin.Styles.Button.FRONTEND_LOCALGAME_LISTBUTTON_TGL, 3f, -17f, UiAnchor.UPPER_LEFT);
        this.name = ui.buttonLabel(this.game, "...", FontsSkin.Styles.Label.ARIAL12_BOLD);
    }

    public void setName(String name) {
        this.name.setText(name);
    }

    public Button button() {
        return this.game;
    }
}
