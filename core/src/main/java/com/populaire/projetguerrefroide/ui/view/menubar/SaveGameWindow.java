package com.populaire.projetguerrefroide.ui.view.menubar;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.utils.Align;
import com.populaire.projetguerrefroide.ui.skin.SkinNames.FontsSkin;
import com.populaire.projetguerrefroide.ui.skin.SkinNames.Skins;
import com.populaire.projetguerrefroide.ui.skin.SkinNames.UiMainmenuIgSkin;
import com.populaire.projetguerrefroide.ui.widget.UiAnchor;
import com.populaire.projetguerrefroide.ui.widget.UiBuilder;
import com.populaire.projetguerrefroide.ui.widget.UiList;
import com.populaire.projetguerrefroide.ui.widget.UiWindow;
import java.util.function.Consumer;

public class SaveGameWindow extends UiWindow {
    private final Image saveBackground;
    private final Label savegameLabel;
    private final UiList<SaveGameEntry> games;
    private final Button save;
    private final Label saveBtnLabel;
    private final Button cancel;
    private final Label cancelBtnLabel;
    private final TextField gameName;

    public SaveGameWindow(UiBuilder ui, Runnable onCancel, Consumer<String> onSave) {
        super(ui, 420f, 500f);
        Skin ingameSkin = ui.skin(Skins.UI_MAINMENU_IG);
        this.saveBackground = ui.image(this, ingameSkin, UiMainmenuIgSkin.Drawables.SAVEGAME_BACKGROUND, 27f, -85f, UiAnchor.UPPER_LEFT);
        this.savegameLabel = ui.label(this, "SAVE_GAME", FontsSkin.Styles.Label.JOCKEY_24, 56f, -50f, 310f, 18f, Align.top, UiAnchor.UPPER_LEFT);
        this.games = ui.list(this, 60f, 9f, 287f, 324f, UiAnchor.UPPER_LEFT, 0f, 0f, 0f, SaveGameEntry::new);
        this.save = ui.button(this, ingameSkin, UiMainmenuIgSkin.Styles.Button.SETTINGS_OK_BTN, 217f, 446f, UiAnchor.UPPER_LEFT);
        this.saveBtnLabel = ui.label(this, "SAVE", FontsSkin.Styles.Label.JOCKEY_16_GLOW_BLUE, 205f, 455f, 161f, 24f, Align.top, UiAnchor.UPPER_LEFT);
        this.cancel = ui.button(this, ingameSkin, UiMainmenuIgSkin.Styles.Button.SETTINGS_CANCEL_BTN, 64f, 446f, UiAnchor.UPPER_LEFT);
        ui.shortcut(this, this.cancel, Input.Keys.ESCAPE);
        this.cancelBtnLabel = ui.label(this, "CLOSE", FontsSkin.Styles.Label.JOCKEY_16_GLOW_RED, 55f, 455f, 161f, 24f, Align.top, UiAnchor.UPPER_LEFT);
        this.gameName = ui.textField(this, null, null, "", FontsSkin.Styles.Label.JOCKEY_14, 75f, 378f, 270f, 14f, UiAnchor.UPPER_LEFT, 0f, 0f);
        ui.onClick(this.save, () -> onSave.accept(this.gameName.getText()));
        ui.onClick(this.cancel, onCancel);
    }

    public UiList<SaveGameEntry> list() {
        return this.games;
    }
}
