package com.populaire.projetguerrefroide.ui.view;

import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.populaire.projetguerrefroide.service.LabelStylePool;
import com.populaire.projetguerrefroide.ui.widget.WidgetFactory;

import java.util.Map;

public class EconomyPanel extends Table {

    public EconomyPanel(WidgetFactory widgetFactory, Skin skin, Skin skinUi, LabelStylePool labelStylePool, Map<String, String> localisation) {
        Drawable background = skin.getDrawable("economy_bg_shadow");
        this.setBackground(background);
        this.setSize(background.getMinWidth(), background.getMinHeight());
        this.setMainContent(skin);
        this.setRightContent(skin);
    }

    public void setMainContent(Skin skin) {
        Table mainTable = new Table();
        Drawable background = skin.getDrawable("bg_economy_trade");
        mainTable.setBackground(background);
        mainTable.setFillParent(true);
        this.add(mainTable);
    }

    public void setRightContent(Skin skin) {
        Table rightTable = new Table();
        Drawable background = skin.getDrawable("bg_eco_budget");
        rightTable.setBackground(background);
        rightTable.setSize(background.getMinWidth(), background.getMinHeight());
        rightTable.setPosition(790, 26);
        this.addActor(rightTable);
    }
}
