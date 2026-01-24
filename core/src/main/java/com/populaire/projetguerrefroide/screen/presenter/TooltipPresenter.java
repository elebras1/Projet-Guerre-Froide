package com.populaire.projetguerrefroide.screen.presenter;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.populaire.projetguerrefroide.pojo.MapMode;
import com.populaire.projetguerrefroide.service.GameContext;
import com.populaire.projetguerrefroide.service.WorldService;
import com.populaire.projetguerrefroide.ui.view.HoverTooltip;
import com.populaire.projetguerrefroide.ui.widget.WidgetFactory;

public class TooltipPresenter implements Presenter {
    private final GameContext gameContext;
    private final WorldService worldService;
    private final WidgetFactory widgetFactory;
    private final Skin skinUi, skinFlags;
    private HoverTooltip hoverTooltip;

    public TooltipPresenter(GameContext context, WorldService worldService, WidgetFactory factory, Skin skinUi, Skin skinFlags) {
        this.gameContext = context;
        this.worldService = worldService;
        this.widgetFactory = factory;
        this.skinUi = skinUi;
        this.skinFlags = skinFlags;
    }

    @Override
    public void initialize(Stage stage) {
        this.hoverTooltip = new HoverTooltip(widgetFactory, skinUi, skinFlags, gameContext.getLabelStylePool(), gameContext.getLocalisation());
        this.hoverTooltip.setVisible(false);
        stage.addActor(this.hoverTooltip);
    }

    @Override
    public void refresh() {
    }

    public void update(int x, int y) {
        int screenX = Gdx.input.getX();
        int screenY = Gdx.graphics.getHeight() - Gdx.input.getY();

        if(this.worldService.getMapMode().equals(MapMode.CULTURAL)) {
            this.hoverTooltip.update(this.worldService.getProvinceNameId(x, y), this.worldService.getCountryNameIdOfHoveredProvince(x, y), this.worldService.getColonizerIdOfHoveredProvince(x, y), this.worldService.getCulturesOfHoveredProvince(x, y));
        } else if(this.worldService.getMapMode().equals(MapMode.RELIGIOUS)) {
            this.hoverTooltip.update(this.worldService.getProvinceNameId(x, y), this.worldService.getCountryNameIdOfHoveredProvince(x, y), this.worldService.getColonizerIdOfHoveredProvince(x, y), this.worldService.getReligionsOfHoveredProvince(x, y));
        } else {
            this.hoverTooltip.update(this.worldService.getProvinceNameId(x, y), this.worldService.getCountryNameIdOfHoveredProvince(x, y), this.worldService.getColonizerIdOfHoveredProvince(x, y));
        }
        this.hoverTooltip.setPosition(screenX + (float) this.gameContext.getCursorManager().getWidth(), screenY - this.gameContext.getCursorManager().getHeight() * 1.5f);
        this.hoverTooltip.setVisible(true);
    }

    public void update(String content) {
        int screenX = Gdx.input.getX();
        int screenY = Gdx.graphics.getHeight() - Gdx.input.getY();

        this.hoverTooltip.update(content);
        this.hoverTooltip.setPosition(screenX + (float) this.gameContext.getCursorManager().getWidth(), screenY - this.gameContext.getCursorManager().getHeight() * 1.5f);
        this.hoverTooltip.setVisible(true);
    }

    public void hide() {
        this.hoverTooltip.setVisible(false);
    }

    @Override
    public void dispose() {}
}
