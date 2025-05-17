package com.populaire.projetguerrefroide.ui.view;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.populaire.projetguerrefroide.entity.Bookmark;
import com.populaire.projetguerrefroide.service.LabelStylePool;

import java.util.Map;

public class ScenarioSavegameSelector extends Table {
    private final Bookmark bookmark;
    private final Table scenarioTable;
    private final Table savegameTable;

    public ScenarioSavegameSelector(Skin skinUi, LabelStylePool labelStylePool, Bookmark bookmark, Map<String, String> localisation) {
        this.bookmark = bookmark;
        Table buttonsTable = this.createButtonsTable(skinUi, labelStylePool, localisation);

        this.scenarioTable = this.createScenarioTable(skinUi, labelStylePool, localisation);
        this.savegameTable = this.createSavegameTable(skinUi, labelStylePool);
        Stack scenarioSavegameStack = new Stack();
        scenarioSavegameStack.add(this.scenarioTable);
        scenarioSavegameStack.add(this.savegameTable);

        this.add(buttonsTable);
        this.row();
        this.add(scenarioSavegameStack);
    }

    private Table createButtonsTable(Skin skinUi, LabelStylePool labelStylePool, Map<String, String> localisation) {
        Image tabBg = new Image(skinUi.getDrawable("tab_bg"));
        LabelStyle labelStyleArial172 = labelStylePool.getLabelStyle("arial_17_2");

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

    private Table createScenarioTable(Skin skinUi, LabelStylePool labelStylePool, Map<String, String> localisation) {
        Table scenarioTable = new Table();
        Drawable bookmarkImage = skinUi.getDrawable(bookmark.getIconNameFile());
        scenarioTable.setBackground(bookmarkImage);
        LabelStyle labelStyleImpactSmall = labelStylePool.getLabelStyle("impact_small");
        LabelStyle labelStyleJockey14 = labelStylePool.getLabelStyle("jockey_14");
        Label nameLabel = new Label(localisation.get(bookmark.getNameId()), labelStyleImpactSmall);
        Label descriptionLabel = new Label(localisation.get(bookmark.getDescriptionId()), labelStyleJockey14);
        nameLabel.setPosition(15, 35);
        descriptionLabel.setPosition(10, 15);
        scenarioTable.addActor(nameLabel);
        scenarioTable.addActor(descriptionLabel);

        return scenarioTable;
    }

    private Table createSavegameTable(Skin skinUi, LabelStylePool labelStylePool) {
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
