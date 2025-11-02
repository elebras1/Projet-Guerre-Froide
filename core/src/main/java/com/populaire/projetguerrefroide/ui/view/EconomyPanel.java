package com.populaire.projetguerrefroide.ui.view;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.populaire.projetguerrefroide.screen.EconomyPanelListener;
import com.populaire.projetguerrefroide.service.LabelStylePool;
import com.populaire.projetguerrefroide.ui.widget.WidgetFactory;

import java.util.Map;

public class EconomyPanel extends Table {

    public EconomyPanel(WidgetFactory widgetFactory, Skin skin, Skin skinUi, LabelStylePool labelStylePool, Map<String, String> localisation, EconomyPanelListener listener) {
        Drawable background = skin.getDrawable("economy_bg_shadow");
        this.setBackground(background);
        this.setSize(background.getMinWidth(), background.getMinHeight());
        this.setMainContent(widgetFactory, skin, skinUi, listener);
        this.setRightContent(skin);
    }

    private void setMainContent(WidgetFactory widgetFactory, Skin skin, Skin skinUi, EconomyPanelListener listener) {
        Table mainTable = new Table();
        Drawable background = skin.getDrawable("bg_economy_trade");
        mainTable.setBackground(background);
        mainTable.setFillParent(true);
        this.add(mainTable);

        Button aiButton = widgetFactory.createButton(skin, "eco_btn_ai", 83, 685.5f, mainTable);
        Button regionalButton = widgetFactory.createButton(skin, "eco_btn_regional", 773, 685.5f, mainTable);
        Button closeButton = widgetFactory.createButton(skinUi, "close_btn", 1057, 734, mainTable);
        closeButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                listener.onCloseEconomyPanelClicked();
            }
        });

    }

    private void setRightContent(Skin skin) {
        Table rightTable = new Table();
        Drawable background = skin.getDrawable("bg_eco_budget");
        rightTable.setBackground(background);
        rightTable.setSize(background.getMinWidth(), background.getMinHeight());
        rightTable.setPosition(821, 55);
        this.addActor(rightTable);
    }
}
