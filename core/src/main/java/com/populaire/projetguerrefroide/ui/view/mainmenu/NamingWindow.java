package com.populaire.projetguerrefroide.ui.view.mainmenu;

import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.utils.Align;
import com.populaire.projetguerrefroide.ui.skin.SkinNames.FontsSkin;
import com.populaire.projetguerrefroide.ui.skin.SkinNames.Skins;
import com.populaire.projetguerrefroide.ui.skin.SkinNames.UiMainmenuSkin;
import com.populaire.projetguerrefroide.ui.skin.SkinNames.UiSkin;
import com.populaire.projetguerrefroide.ui.widget.UiAnchor;
import com.populaire.projetguerrefroide.ui.widget.UiBuilder;
import com.populaire.projetguerrefroide.ui.widget.UiWindow;
import java.util.function.Consumer;

public class NamingWindow extends UiWindow {
    private final Image background;
    private final Label title;
    private final TextField edit;
    private final Button okHostButton;
    private final Button backButton;
    private final Label okLabel;
    private Consumer<String> onOk;
    private Runnable onCancel;

    public NamingWindow(UiBuilder ui) {
        super(ui, 500f, 280f);
        Skin uiSkin = ui.skin(Skins.UI);
        Skin mainMenuSkin = ui.skin(Skins.UI_MAINMENU);
        this.background = ui.ninePatch(this, uiSkin, UiSkin.Drawables.TILES_DIALOG_TYPE3, 32, 0f, 0f, 500f, 280f, UiAnchor.UPPER_LEFT);
        this.title = ui.label(this, "", FontsSkin.Styles.Label.ARIAL_17, 0f, 36f, 500f, 320f, Align.top, UiAnchor.UPPER_LEFT, 4f, 4f);
        this.edit = ui.textField(this, uiSkin, UiSkin.Drawables.SMALL_TILES_DIALOG, "FE_MY_GAME", FontsSkin.Styles.Label.ARIAL12_BOLD, 100f, 100f, 300f, 32f, UiAnchor.UPPER_LEFT, 8f, 4f);
        this.okHostButton = ui.button(this, mainMenuSkin, UiMainmenuSkin.Styles.Button.FRONTEND_BUTTON4_BTN, 264f, 200f, UiAnchor.UPPER_LEFT);
        this.okLabel = ui.buttonLabel(this.okHostButton, "FE_HOST", FontsSkin.Styles.Label.FRANGOTH_BOLD);
        this.backButton = ui.button(this, mainMenuSkin, UiMainmenuSkin.Styles.Button.FRONTEND_BUTTON4_BTN, 60f, 200f, UiAnchor.UPPER_LEFT);
        ui.buttonLabel(this.backButton, "FE_CANCEL", FontsSkin.Styles.Label.FRANGOTH_BOLD);
        ui.onClick(this.okHostButton, () -> this.onOk.accept(this.edit.getText()));
        ui.onClick(this.backButton, () -> this.onCancel.run());
    }

    public void open(String title, String text, String okKey, Consumer<String> onOk, Runnable onCancel) {
        this.title.setText(title);
        this.edit.setText(text);
        this.okLabel.setText(this.ui.localise(okKey));
        this.onOk = onOk;
        this.onCancel = onCancel;
    }
}
