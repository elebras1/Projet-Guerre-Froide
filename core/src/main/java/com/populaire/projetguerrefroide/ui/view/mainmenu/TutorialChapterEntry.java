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

public class TutorialChapterEntry extends UiWindow {
    private final Button chapterButton;
    private final Label text;

    public TutorialChapterEntry(UiBuilder ui) {
        super(ui, 139f, 37f);
        Skin mainMenuSkin = ui.skin(Skins.UI_MAINMENU);
        this.chapterButton = ui.button(this, mainMenuSkin, UiMainmenuSkin.Styles.Button.TUTORIAL_SELECT_CHAPTER_TGL, 0f, -3f, UiAnchor.UPPER_LEFT);
        this.text = ui.buttonLabel(this.chapterButton, "", FontsSkin.Styles.Label.JOCKEY_14_GLOW_BLUE);
    }

    public void setText(String text) {
        this.text.setText(text);
    }

    public Button button() {
        return this.chapterButton;
    }
}
