package com.populaire.projetguerrefroide.ui.view;

import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.populaire.projetguerrefroide.service.LabelStylePool;
import com.populaire.projetguerrefroide.ui.widget.WidgetFactory;

import java.util.Map;

public class TitleBar extends Table {

    public TitleBar(WidgetFactory widgetFactory, Skin skinUi, LabelStylePool labelStylePool, Map<String, String> localisation) {
        widgetFactory.applyBackgroundToTable(skinUi, "selected_scenario_bg", this);

        Label.LabelStyle labelStyleJocker18Black = labelStylePool.get("jockey_18_black");
        Label titleScenario = new Label(localisation.get("TITLE_SELECT_NATION"), labelStyleJocker18Black);
        this.add(titleScenario);
        this.row();
        Label.LabelStyle labelStyleF25Executive17 = labelStylePool.get("f25_executive_17");
        Label order = new Label(localisation.get("NATION_TO_PLAY"), labelStyleF25Executive17);
        this.add(order);
    }
}
