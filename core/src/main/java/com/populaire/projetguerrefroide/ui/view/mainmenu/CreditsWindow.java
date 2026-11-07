package com.populaire.projetguerrefroide.ui.view.mainmenu;

import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;
import com.monstrous.gdx.webgpu.scene2d.WgScrollPane;
import com.populaire.projetguerrefroide.ui.skin.SkinNames.FontsSkin;
import com.populaire.projetguerrefroide.ui.skin.SkinNames.Skins;
import com.populaire.projetguerrefroide.ui.skin.SkinNames.UiMainmenuSkin;
import com.populaire.projetguerrefroide.ui.skin.SkinNames.UiSkin;
import com.populaire.projetguerrefroide.ui.widget.UiAnchor;
import com.populaire.projetguerrefroide.ui.widget.UiBuilder;
import com.populaire.projetguerrefroide.ui.widget.UiWindow;
import java.util.List;

public class CreditsWindow extends UiWindow {
    private static final String HEADING = "§";
    private static final float SCROLL_SPEED = 30f;
    private final Button backButton;
    private final ScrollPane scroll;
    private final Table lines;

    public CreditsWindow(UiBuilder ui, List<String> credits, Runnable onBack) {
        super(ui, 420f, 500f);
        Skin mainMenuSkin = ui.skin(Skins.UI_MAINMENU);
        Skin uiSkin = ui.skin(Skins.UI);
        ui.image(this, uiSkin, UiSkin.Drawables.TILES_DIALOG, 0f, 0f, UiAnchor.UPPER_LEFT).setSize(420f, 420f);
        this.lines = new Table();
        this.lines.top();
        Label.LabelStyle text = ui.font(FontsSkin.Styles.Label.ARIAL12);
        Label.LabelStyle heading = ui.font(FontsSkin.Styles.Label.ARIAL12_BOLD_YELLOW);
        for (String line : credits) {
            boolean isHeading = line.startsWith(HEADING);
            Label label = new Label(isHeading ? line.substring(HEADING.length()) : line, isHeading ? heading : text);
            label.setAlignment(Align.center);
            label.setWrap(true);
            this.lines.add(label).width(380f).row();
        }
        this.scroll = new WgScrollPane(this.lines);
        this.scroll.setSize(380f, 380f);
        this.scroll.setScrollingDisabled(true, false);
        this.scroll.setOverscroll(false, false);
        this.scroll.setSmoothScrolling(false);
        this.scroll.setTouchable(Touchable.disabled);
        ui.add(this, this.scroll, 20f, 20f, UiAnchor.UPPER_LEFT);
        this.backButton = ui.button(this, mainMenuSkin, UiMainmenuSkin.Styles.Button.FRONTEND_BUTTON2_BTN, 123f, 431f, UiAnchor.UPPER_LEFT);
        ui.buttonLabel(this.backButton, "BACK", FontsSkin.Styles.Label.ARIAL_20);
        ui.onClick(this.backButton, onBack);
    }

    public void restart() {
        this.scroll.layout();
        this.scroll.setScrollY(0f);
        this.scroll.updateVisualScroll();
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        if (!this.isVisible()) {
            return;
        }
        float next = this.scroll.getScrollY() + SCROLL_SPEED * delta;
        this.scroll.setScrollY(next >= this.scroll.getMaxY() ? 0f : next);
        this.scroll.updateVisualScroll();
    }
}
