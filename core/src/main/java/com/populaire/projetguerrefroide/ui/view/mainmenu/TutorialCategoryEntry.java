package com.populaire.projetguerrefroide.ui.view.mainmenu;

import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Align;
import com.populaire.projetguerrefroide.ui.skin.SkinNames.FontsSkin;
import com.populaire.projetguerrefroide.ui.skin.SkinNames.Skins;
import com.populaire.projetguerrefroide.ui.skin.SkinNames.UiMainmenuSkin;
import com.populaire.projetguerrefroide.ui.widget.UiAnchor;
import com.populaire.projetguerrefroide.ui.widget.UiBuilder;
import com.populaire.projetguerrefroide.ui.widget.UiWindow;

public class TutorialCategoryEntry extends UiWindow {
    private final Button categoryButton;
    private final Image categoryPic;
    private final Image overlay;
    private final Label title;

    public TutorialCategoryEntry(UiBuilder ui) {
        super(ui, 228f, 100f);
        Skin mainMenuSkin = ui.skin(Skins.UI_MAINMENU);
        this.categoryButton = ui.button(this, mainMenuSkin, UiMainmenuSkin.Styles.Button.TUTORIAL_SELECT_TAB_TGL, 0f, -7f, UiAnchor.UPPER_LEFT);
        this.categoryPic = ui.image(this, mainMenuSkin, UiMainmenuSkin.Drawables.TUTORIAL_PIC, 20f, 14f, UiAnchor.UPPER_LEFT);
        this.overlay = ui.image(this, mainMenuSkin, UiMainmenuSkin.Drawables.TUTORIAL_PIC_OVERLAY, 1f, 40f, UiAnchor.UPPER_LEFT);
        this.title = ui.label(this, "", FontsSkin.Styles.Label.JOCKEY_16_PAPER, 81f, 22f, 130f, 20f, Align.top, UiAnchor.UPPER_LEFT);
    }

    public void setTitle(String title) {
        this.title.setText(title);
    }

    public Button button() {
        return this.categoryButton;
    }
}
