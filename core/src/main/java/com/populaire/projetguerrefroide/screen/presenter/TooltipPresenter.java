package com.populaire.projetguerrefroide.screen.presenter;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.populaire.projetguerrefroide.service.GameContext;
import com.populaire.projetguerrefroide.ui.view.HoverTooltip;
import com.populaire.projetguerrefroide.ui.widget.WidgetFactory;

public class TooltipPresenter implements Presenter {
    private final GameContext gameContext;
    private final WidgetFactory widgetFactory;
    private final Skin skinUi, skinFlags;

    private HoverTooltip view;

    public TooltipPresenter(GameContext context, WidgetFactory factory, Skin skinUi, Skin skinFlags) {
        this.gameContext = context;
        this.widgetFactory = factory;
        this.skinUi = skinUi;
        this.skinFlags = skinFlags;
    }

    @Override
    public void initialize(Stage stage) {
        this.view = new HoverTooltip(widgetFactory, skinUi, skinFlags, gameContext.getLabelStylePool(), gameContext.getLocalisation());
        this.view.setVisible(false);
        stage.addActor(this.view);
    }

    public void onHover(String provinceName, String countryName, String colonizer) {
        int x = Gdx.input.getX();
        int y = Gdx.graphics.getHeight() - Gdx.input.getY();

        this.view.update(provinceName, countryName, colonizer);
        this.view.setPosition(x + (float) gameContext.getCursorManager().getWidth(),
            y - gameContext.getCursorManager().getHeight() * 1.5f);
        this.view.setVisible(true);
    }

    public void hide() {
        this.view.setVisible(false);
    }

    @Override
    public void dispose() {}
}
