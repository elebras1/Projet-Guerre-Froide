package com.populaire.projetguerrefroide.ui.view;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.populaire.projetguerrefroide.pojo.Bookmark;

import com.populaire.projetguerrefroide.ui.skin.SkinNames.FontsSkin;
import com.populaire.projetguerrefroide.ui.skin.SkinNames.UiNewgameSkin;
import java.util.Map;

public class ScenarioSavegameSelector extends Table {
    private final Bookmark bookmark;
    private final Table scenarioTable;
    private final Table savegameTable;

    public ScenarioSavegameSelector(Skin skin, Skin skinFonts, Bookmark bookmark, Map<String, String> localisation) {
        this.bookmark = bookmark;
        Table buttonsTable = this.createButtonsTable(skin, skinFonts, localisation);

        this.scenarioTable = this.createScenarioTable(skin, skinFonts, localisation);
        this.savegameTable = this.createSavegameTable(skin, skinFonts);
        Stack scenarioSavegameStack = new Stack();
        scenarioSavegameStack.add(this.scenarioTable);
        scenarioSavegameStack.add(this.savegameTable);

        this.add(buttonsTable);
        this.row();
        this.add(scenarioSavegameStack);
    }

    private Table createButtonsTable(Skin skin, Skin skinFonts, Map<String, String> localisation) {
        Image tabBg = new Image(skin.getDrawable(UiNewgameSkin.Drawables.TAB_BG));
        LabelStyle labelStyleArial172 = skinFonts.get(FontsSkin.Styles.Label.ARIAL_17_2, LabelStyle.class);

        Label scenarioLabel = new Label(localisation.get("HISTORICAL_START"), labelStyleArial172);
        Label savegameLabel = new Label(localisation.get("SAVED_GAMES"), labelStyleArial172);

        Button bookmarkButton = new Button(skin, UiNewgameSkin.Styles.Button.TAB_BTN);
        bookmarkButton.add(scenarioLabel).center();
        bookmarkButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                showScenarioTable();
            }
        });

        Button savegameButton = new Button(skin, UiNewgameSkin.Styles.Button.TAB_BTN);
        savegameButton.add(savegameLabel).center();
        savegameButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                showSavegameTable();
            }
        });

        Table buttonsTable = new Table();
        buttonsTable.add(bookmarkButton);
        buttonsTable.add(savegameButton);
        buttonsTable.setBackground(tabBg.getDrawable());

        return buttonsTable;
    }

    private Table createScenarioTable(Skin skin, Skin skinFonts, Map<String, String> localisation) {
        Table scenarioTable = new Table();
        Drawable bookmarkImage = skin.getDrawable(bookmark.iconNameFile());
        scenarioTable.setBackground(bookmarkImage);
        LabelStyle labelStyleImpactSmall = skinFonts.get(FontsSkin.Styles.Label.IMPACT_SMALL, LabelStyle.class);
        LabelStyle labelStyleJockey14 = skinFonts.get(FontsSkin.Styles.Label.JOCKEY_14, LabelStyle.class);
        Label nameLabel = new Label(localisation.get(bookmark.nameId()), labelStyleImpactSmall);
        Label descriptionLabel = new Label(localisation.get(bookmark.descriptionId()), labelStyleJockey14);
        nameLabel.setPosition(15, 35);
        descriptionLabel.setPosition(10, 15);
        scenarioTable.addActor(nameLabel);
        scenarioTable.addActor(descriptionLabel);

        return scenarioTable;
    }

    private Table createSavegameTable(Skin skin, Skin skinFonts) {
        return new Table();
    }

    private void showScenarioTable() {
        this.scenarioTable.setVisible(true);
        this.savegameTable.setVisible(false);
    }

    private void showSavegameTable() {
        this.scenarioTable.setVisible(false);
        this.savegameTable.setVisible(true);
    }
}
