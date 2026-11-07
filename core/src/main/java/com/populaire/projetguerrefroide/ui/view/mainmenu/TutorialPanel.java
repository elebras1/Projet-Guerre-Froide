package com.populaire.projetguerrefroide.ui.view.mainmenu;

import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.ButtonGroup;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Align;
import com.populaire.projetguerrefroide.configuration.TutorialCategory;
import com.populaire.projetguerrefroide.configuration.TutorialChapter;
import com.populaire.projetguerrefroide.ui.skin.SkinNames.FontsSkin;
import com.populaire.projetguerrefroide.ui.skin.SkinNames.Skins;
import com.populaire.projetguerrefroide.ui.skin.SkinNames.UiMainmenuSkin;
import com.populaire.projetguerrefroide.ui.skin.SkinNames.UiNewgameSkin;
import com.populaire.projetguerrefroide.ui.widget.UiAnchor;
import com.populaire.projetguerrefroide.ui.widget.UiBuilder;
import com.populaire.projetguerrefroide.ui.widget.UiList;
import com.populaire.projetguerrefroide.ui.widget.UiWindow;
import java.util.List;

public class TutorialPanel extends UiWindow {
    public interface PlayListener {
        void onPlay(int category, int chapter);
    }

    private final Image tutorialListBg;
    private final Label tutorialLabel;
    private final UiList<TutorialCategoryEntry> categoryList;
    private final Label tutorialTitle;
    private final Label tutorialDesc;
    private final Label tutorialPartDesc;
    private final UiList<TutorialChapterEntry> tutorialList;
    private final Button backButton;
    private final Label backLabel;
    private final Button playButton;
    private final Label playLabel;
    private final List<TutorialCategory> categories;
    private final ButtonGroup<Button> categoryGroup;
    private final ButtonGroup<Button> chapterGroup;
    private int category;
    private int chapter;
    private int wiredChapterRows;

    public TutorialPanel(UiBuilder ui, List<TutorialCategory> categories, Runnable onBack, PlayListener onPlay) {
        super(ui, 788f, 540f);
        this.categories = categories;
        Skin mainMenuSkin = ui.skin(Skins.UI_MAINMENU);
        Skin newGameSkin = ui.skin(Skins.UI_NEWGAME);
        this.tutorialListBg = ui.image(this, mainMenuSkin, UiMainmenuSkin.Drawables.TUTORIAL_LIST_BG, 0f, 0f, UiAnchor.UPPER_LEFT);
        this.tutorialLabel = ui.label(this, "TUTORIAL", FontsSkin.Styles.Label.JOCKEY_24, 0f, 62f, 788f, 21f, Align.top, UiAnchor.UPPER_LEFT);
        this.categoryList = ui.list(this, 45f, 97f, 231f, 300f, UiAnchor.UPPER_LEFT, 0f, 0f, 0f, TutorialCategoryEntry::new);
        this.tutorialTitle = ui.label(this, "TUTORIAL", FontsSkin.Styles.Label.JOCKEY_18_BLUE, 385f, 111f, 303f, 21f, Align.top, UiAnchor.UPPER_LEFT);
        this.tutorialDesc = ui.label(this, "", FontsSkin.Styles.Label.JOCKEY_14, 290f, 145f, 450f, 80f, Align.top, UiAnchor.UPPER_LEFT);
        this.tutorialPartDesc = ui.label(this, "", FontsSkin.Styles.Label.JOCKEY_14, 490f, 250f, 250f, 140f, Align.topLeft, UiAnchor.UPPER_LEFT);
        this.tutorialList = ui.list(this, 315f, 250f, 137f, 150f, UiAnchor.UPPER_LEFT, 0f, 0f, 0f, TutorialChapterEntry::new);
        this.backButton = ui.button(this, newGameSkin, UiNewgameSkin.Styles.Button.GEN_BTN, 92f, 436f, UiAnchor.UPPER_LEFT);
        this.backLabel = ui.label(this, "BACK", FontsSkin.Styles.Label.JOCKEY_16_DARK, 40f, 445f, 250f, 140f, Align.top, UiAnchor.UPPER_LEFT);
        this.playButton = ui.button(this, mainMenuSkin, UiMainmenuSkin.Styles.Button.BUTTON_BUILD_DIVISION_BTN, 599f, 420f, UiAnchor.UPPER_LEFT);
        this.playLabel = ui.label(this, "PLAY", FontsSkin.Styles.Label.JOCKEY_24_GLOW_BLUE, 535f, 441f, 250f, 140f, Align.top, UiAnchor.UPPER_LEFT);
        this.tutorialDesc.setWrap(true);
        this.tutorialPartDesc.setWrap(true);
        ui.onClick(this.backButton, onBack);
        ui.onClick(this.playButton, () -> onPlay.onPlay(this.category, this.chapter));
        this.categoryGroup = new ButtonGroup<>();
        this.chapterGroup = new ButtonGroup<>();
        this.categoryList.setRowCount(categories.size());
        for (int i = 0; i < categories.size(); i++) {
            TutorialCategoryEntry entry = this.categoryList.row(i);
            entry.setTitle(ui.localise(categories.get(i).shortId()));
            this.categoryGroup.add(entry.button());
            int index = i;
            ui.onClick(entry.button(), () -> this.selectCategory(index));
        }
    }

    public void reset() {
        if (!this.categories.isEmpty()) {
            this.selectCategory(0);
        }
    }

    public void showMessage(String key) {
        this.tutorialPartDesc.setText(this.ui.localise(key));
    }

    private void selectCategory(int index) {
        this.category = index;
        this.categoryList.row(index).button().setChecked(true);
        TutorialCategory category = this.categories.get(index);
        this.tutorialTitle.setText(this.ui.localise(category.titleId()));
        this.tutorialDesc.setText(this.ui.localise(category.descriptionId()));
        List<TutorialChapter> chapters = category.chapters();
        this.chapterGroup.clear();
        this.tutorialList.setRowCount(chapters.size());
        for (int i = 0; i < chapters.size(); i++) {
            TutorialChapterEntry entry = this.tutorialList.row(i);
            entry.setText(this.ui.localise(chapters.get(i).buttonId()));
            entry.button().setChecked(false);
            this.chapterGroup.add(entry.button());
            if (i >= this.wiredChapterRows) {
                int chapter = i;
                this.ui.onClick(entry.button(), () -> this.selectChapter(chapter));
                this.wiredChapterRows = i + 1;
            }
        }
        if (chapters.isEmpty()) {
            this.chapter = -1;
            this.tutorialPartDesc.setText("");
        } else {
            this.selectChapter(0);
        }
    }

    private void selectChapter(int index) {
        this.chapter = index;
        this.tutorialList.row(index).button().setChecked(true);
        TutorialChapter chapter = this.categories.get(this.category).chapters().get(index);
        this.tutorialPartDesc.setText(this.ui.localise(chapter.titleId()) + "\n\n" + this.ui.localise(chapter.descriptionId()));
    }
}
