package com.populaire.projetguerrefroide.ui.view.ledger;

import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Align;
import com.populaire.projetguerrefroide.ui.skin.SkinNames.FontsSkin;
import com.populaire.projetguerrefroide.ui.skin.SkinNames.Skins;
import com.populaire.projetguerrefroide.ui.skin.SkinNames.UiNewgameSkin;
import com.populaire.projetguerrefroide.ui.widget.UiAnchor;
import com.populaire.projetguerrefroide.ui.widget.UiBuilder;
import com.populaire.projetguerrefroide.ui.widget.UiWindow;

public class GraphBackground extends UiWindow {
    private final Label graphLabel;
    private final Button prevGraphButton;
    private final Button nextGraphButton;
    private final Label vertical1;
    private final Label vertical2;
    private final Label vertical3;
    private final Label vertical4;
    private final Label horizontal1;
    private final Label horizontal2;
    private final Label horizontal3;
    private final Label horizontal4;
    private final Label horizontal5;

    public GraphBackground(UiBuilder ui) {
        super(ui, 640f, 400f);
        Skin newGameSkin = ui.skin(Skins.UI_NEWGAME);
        this.graphLabel = ui.label(this, "", FontsSkin.Styles.Label.JOCKEY_14, 240f, 0f, 172f, 18f, Align.top, UiAnchor.UPPER_LEFT);
        this.prevGraphButton = ui.button(this, newGameSkin, UiNewgameSkin.Styles.Button.ARROW_LEFT_BTN, 239f, -10f, UiAnchor.UPPER_LEFT);
        this.nextGraphButton = ui.button(this, newGameSkin, UiNewgameSkin.Styles.Button.ARROW_RIGHT_BTN, 375f, -10f, UiAnchor.UPPER_LEFT);
        // graph_vertical_bar : sprite GFX_graph_vertical_bar sans asset PGF
        // graph_horizontal_bar : sprite GFX_graph_horizontal_bar sans asset PGF
        this.vertical1 = ui.label(this, "", FontsSkin.Styles.Label.ARIAL12, -105f, -10f, 100f, 30f, Align.topRight, UiAnchor.UPPER_LEFT);
        this.vertical2 = ui.label(this, "", FontsSkin.Styles.Label.ARIAL12, -105f, 90f, 100f, 30f, Align.topRight, UiAnchor.UPPER_LEFT);
        this.vertical3 = ui.label(this, "", FontsSkin.Styles.Label.ARIAL12, -105f, 190f, 100f, 30f, Align.topRight, UiAnchor.UPPER_LEFT);
        this.vertical4 = ui.label(this, "", FontsSkin.Styles.Label.ARIAL12, -105f, 290f, 100f, 30f, Align.topRight, UiAnchor.UPPER_LEFT);
        this.horizontal1 = ui.label(this, "", FontsSkin.Styles.Label.ARIAL12, -100f, 375f, 200f, 30f, Align.top, UiAnchor.UPPER_LEFT);
        this.horizontal2 = ui.label(this, "", FontsSkin.Styles.Label.ARIAL12, 60f, 375f, 200f, 30f, Align.top, UiAnchor.UPPER_LEFT);
        this.horizontal3 = ui.label(this, "", FontsSkin.Styles.Label.ARIAL12, 220f, 375f, 200f, 30f, Align.top, UiAnchor.UPPER_LEFT);
        this.horizontal4 = ui.label(this, "", FontsSkin.Styles.Label.ARIAL12, 380f, 375f, 200f, 30f, Align.top, UiAnchor.UPPER_LEFT);
        this.horizontal5 = ui.label(this, "", FontsSkin.Styles.Label.ARIAL12, 540f, 375f, 200f, 30f, Align.top, UiAnchor.UPPER_LEFT);
    }
}
