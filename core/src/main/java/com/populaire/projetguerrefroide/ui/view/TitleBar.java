package com.populaire.projetguerrefroide.ui.view;

import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.populaire.projetguerrefroide.ui.skin.SkinNames.FontsSkin;
import com.populaire.projetguerrefroide.ui.skin.SkinNames.UiNewgameSkin;
import com.populaire.projetguerrefroide.ui.widget.WidgetFactory;

import java.util.Map;

public class TitleBar extends Table {

    public TitleBar(WidgetFactory widgetFactory, Skin skin, Skin skinFonts, Map<String, String> localisation) {
        widgetFactory.applyBackgroundToTable(skin, UiNewgameSkin.Drawables.SELECTED_SCENARIO_BG, this);

        Label.LabelStyle labelStyleJocker18Black = skinFonts.get(FontsSkin.Styles.Label.JOCKEY_18_BLACK, Label.LabelStyle.class);
        Label titleScenario = new Label(localisation.get("TITLE_SELECT_NATION"), labelStyleJocker18Black);
        this.add(titleScenario);
        this.row();
        Label.LabelStyle labelStyleF25Executive17 = skinFonts.get(FontsSkin.Styles.Label.F25_EXECUTIVE_17, Label.LabelStyle.class);
        Label order = new Label(localisation.get("NATION_TO_PLAY"), labelStyleF25Executive17);
        this.add(order);
    }
}
