package com.populaire.projetguerrefroide.ui;

import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;

import java.util.Map;

public class TitleBar extends Table {
    private Label titleScenario;
    private Label order;

    public TitleBar(Skin skinUi, Skin skinFonts, Map<String, String> localisation) {
        Drawable drawable = skinUi.getDrawable("selected_scenario_bg");

        Label.LabelStyle labelStyleTitle = new Label.LabelStyle();
        labelStyleTitle.font = skinFonts.getFont("jockey_18_black");
        this.titleScenario = new Label(localisation.get("TITLE_SELECT_NATION"), labelStyleTitle);

        Label.LabelStyle labelStyleOrder = new Label.LabelStyle();
        labelStyleOrder.font = skinFonts.getFont("f25_executive_17");
        this.order = new Label(localisation.get("NATION_TO_PLAY"), labelStyleOrder);


        this.setBackground(drawable);
        this.add(this.titleScenario);
        this.row();
        this.add(this.order);
        this.setSize(drawable.getMinWidth(), drawable.getMinHeight());
    }

    public void updateTitle(String title) {
        this.titleScenario.setText(title);
    }
}
