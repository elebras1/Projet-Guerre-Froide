package com.populaire.projetguerrefroide.ui.view;

import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.populaire.projetguerrefroide.service.LabelStylePool;

import java.util.Map;

public class TitleBar extends Table {

    public TitleBar(Skin skinUi, LabelStylePool labelStylePool, Map<String, String> localisation) {
        Drawable drawable = skinUi.getDrawable("selected_scenario_bg");

        Label.LabelStyle labelStyleJocker18Black = labelStylePool.getLabelStyle("jockey_18_black");
        Label titleScenario = new Label(localisation.get("TITLE_SELECT_NATION"), labelStyleJocker18Black);

        Label.LabelStyle labelStyleF25Executive17 = labelStylePool.getLabelStyle("f25_executive_17");
        Label order = new Label(localisation.get("NATION_TO_PLAY"), labelStyleF25Executive17);

        this.setBackground(drawable);
        this.add(titleScenario);
        this.row();
        this.add(order);
        this.setSize(drawable.getMinWidth(), drawable.getMinHeight());
    }
}
