package com.populaire.projetguerrefroide.ui.view;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.populaire.projetguerrefroide.ui.skin.SkinNames.FontsSkin;
import com.populaire.projetguerrefroide.ui.skin.SkinNames.UiNewgameSkin;
import com.populaire.projetguerrefroide.ui.skin.SkinNames.UiScrollbarsSkin;
import com.populaire.projetguerrefroide.ui.widget.HoverScrollPane;
import com.populaire.projetguerrefroide.ui.widget.WidgetFactory;

import java.util.Map;

public class LobbyBox extends Table {

    public LobbyBox(WidgetFactory widgetFactory, Skin skin, Skin skinScrollbars, Skin skinFonts, Map<String, String> localisation, LobbyBoxListener listener) {
        widgetFactory.applyBackgroundToTable(skin, UiNewgameSkin.Drawables.BOTTOM_BG, this);
        this.addIntroductionSection(skinScrollbars, skinFonts, localisation);
        this.addButtonsSection(skin, skinFonts, localisation, listener);
    }

    private void addIntroductionSection(Skin skinScrollbars, Skin skinFonts, Map<String, String> localisation) {
        Label.LabelStyle labelStyleJockey16GlowBlue = skinFonts.get(FontsSkin.Styles.Label.JOCKEY_16_GLOW_BLUE, Label.LabelStyle.class);

        Label introLabel = new Label(localisation.get("INTRODUCTION"), labelStyleJockey16GlowBlue);
        introLabel.setWrap(true);
        introLabel.setSize(735, 120);
        HoverScrollPane scrollPane = new HoverScrollPane(introLabel, skinScrollbars, UiScrollbarsSkin.Styles.Scrollpane.DEFAULT);
        Table scrollTable = new Table();
        scrollTable.add(scrollPane).width(735).height(120);
        this.add(scrollTable).width(793);
    }

    private void addButtonsSection(Skin skin, Skin skinFonts, Map<String, String> localisation, LobbyBoxListener listener) {
        Label.LabelStyle labelStyleJockey24GlowBlue = skinFonts.get(FontsSkin.Styles.Label.JOCKEY_24_GLOW_BLUE, Label.LabelStyle.class);
        Label.LabelStyle labelStyleArial172 = skinFonts.get(FontsSkin.Styles.Label.ARIAL_17_2, Label.LabelStyle.class);

        Button playButton = new Button(skin, UiNewgameSkin.Styles.Button.PLAY_BTN);
        playButton.add(new Label(localisation.get("PLAY"), labelStyleJockey24GlowBlue));
        playButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                listener.onPlayClicked();
            }
        });

        Button backButton = new Button(skin, UiNewgameSkin.Styles.Button.GEN_BTN);
        backButton.add(new Label(localisation.get("BACK"), labelStyleArial172));
        backButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                listener.onBackClicked();
            }
        });

        Table buttonsTable = new Table();
        buttonsTable.add(playButton).expand().padRight(37).padBottom(10);
        buttonsTable.row();
        buttonsTable.add(backButton).padRight(32).padBottom(20);
        this.add(buttonsTable).expand().fill();
    }
}
