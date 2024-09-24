package com.populaire.projetguerrefroide.ui;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.populaire.projetguerrefroide.utils.Bookmark;

import java.util.Map;

public class ScenarioSavegameSelector extends Table {
    private Bookmark bookmark;
    private Table scenarioTable;
    private Table savegameTable;

    public ScenarioSavegameSelector(Skin skinUi, Skin skinFonts, Bookmark bookmark, Map<String, String> localisation) {
        this.bookmark = bookmark;
        Table buttonsTable = this.createButtonsTable(skinUi, skinFonts, localisation);

        this.scenarioTable = this.createScenarioTable(skinUi, skinFonts, localisation);
        this.savegameTable = this.createSavegameTable(skinUi, skinFonts);
        Stack scenarioSavegameStack = new Stack();
        scenarioSavegameStack.add(this.scenarioTable);
        scenarioSavegameStack.add(this.savegameTable);

        this.add(buttonsTable);
        this.row();
        this.add(scenarioSavegameStack);
    }

    private Table createButtonsTable(Skin skinUi, Skin skinFonts, Map<String, String> localisation) {
        Image tabBg = new Image(skinUi.getDrawable("tab_bg"));
        Label.LabelStyle labelStyleArial172 = new Label.LabelStyle();
        labelStyleArial172.font = skinFonts.getFont("Arial_17_2");

        Label scenarioLabel = new Label(localisation.get("HISTORICAL_START"), labelStyleArial172);
        Label savegameLabel = new Label(localisation.get("SAVED_GAMES"), labelStyleArial172);

        Button bookmarkButton = new Button(skinUi, "tab");
        bookmarkButton.add(scenarioLabel).center();
        bookmarkButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                showScenarioTable();
            }
        });

        Button savegameButton = new Button(skinUi, "tab");
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

    private Table createScenarioTable(Skin skinUi, Skin skinFonts, Map<String, String> localisation) {
        Table scenarioTable = new Table();
        Drawable bookmarkImage = skinUi.getDrawable(bookmark.getIconNameFile());
        scenarioTable.setBackground(bookmarkImage);
        Label.LabelStyle labelStyleImpactSmall = new Label.LabelStyle();
        labelStyleImpactSmall.font = skinFonts.getFont("impact_small");
        Label.LabelStyle labelStyleJockey14 = new Label.LabelStyle();
        labelStyleJockey14.font = skinFonts.getFont("Jockey_14");
        Label nameLabel = new Label(localisation.get(bookmark.getNameId()), labelStyleImpactSmall);
        Label descriptionLabel = new Label(localisation.get(bookmark.getDescriptionId()), labelStyleJockey14);
        nameLabel.setPosition(15, 35);
        descriptionLabel.setPosition(10, 15);
        scenarioTable.addActor(nameLabel);
        scenarioTable.addActor(descriptionLabel);

        return scenarioTable;
    }

    private Table createSavegameTable(Skin skinUi, Skin skinFonts) {
        return new Table();
    }

    private void showScenarioTable() {
        this.scenarioTable.setVisible(true);
        this.savegameTable.setVisible(false);
    }

    private void showSavegameTable() {
        this.scenarioTable.setVisible(false);
        savegameTable.setVisible(true);
    }
}
