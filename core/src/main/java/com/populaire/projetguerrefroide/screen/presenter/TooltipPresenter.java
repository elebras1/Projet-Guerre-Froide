package com.populaire.projetguerrefroide.screen.presenter;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.populaire.projetguerrefroide.pojo.MapMode;
import com.populaire.projetguerrefroide.service.GameContext;
import com.populaire.projetguerrefroide.service.WorldService;
import com.populaire.projetguerrefroide.ui.view.Tooltip;
import com.populaire.projetguerrefroide.ui.widget.WidgetFactory;

public class TooltipPresenter implements Presenter {
    private final GameContext gameContext;
    private final WorldService worldService;
    private final WidgetFactory widgetFactory;
    private final Skin skinUi, skinFlags;
    private Tooltip tooltip;
    private boolean lockedByUi;

    public TooltipPresenter(GameContext context, WorldService worldService, WidgetFactory factory, Skin skinUi, Skin skinFlags) {
        this.gameContext = context;
        this.worldService = worldService;
        this.widgetFactory = factory;
        this.skinUi = skinUi;
        this.skinFlags = skinFlags;
        this.lockedByUi = false;
    }

    @Override
    public void initialize(Stage stage) {
        this.tooltip = new Tooltip(widgetFactory, skinUi, skinFlags, gameContext.getLabelStylePool(), gameContext.getLocalisation());
        this.tooltip.setVisible(false);
        stage.addActor(this.tooltip);
    }

    @Override
    public void refresh() {
    }

    @Override
    public void update(float delta) {

    }

    public void updateMapTooltip(int x, int y) {
        if (this.lockedByUi) {
            return;
        }

        int screenX = Gdx.input.getX();
        int screenY = Gdx.graphics.getHeight() - Gdx.input.getY();

        if(this.worldService.getMapMode().equals(MapMode.CULTURAL)) {
            this.tooltip.update(this.worldService.getProvinceNameId(x, y), this.worldService.getCountryNameIdOfHoveredProvince(x, y), this.worldService.getColonizerIdOfHoveredProvince(x, y), this.worldService.getCulturesOfHoveredProvince(x, y));
        } else if(this.worldService.getMapMode().equals(MapMode.RELIGIOUS)) {
            this.tooltip.update(this.worldService.getProvinceNameId(x, y), this.worldService.getCountryNameIdOfHoveredProvince(x, y), this.worldService.getColonizerIdOfHoveredProvince(x, y), this.worldService.getReligionsOfHoveredProvince(x, y));
        } else {
            this.tooltip.update(this.worldService.getProvinceNameId(x, y), this.worldService.getCountryNameIdOfHoveredProvince(x, y), this.worldService.getColonizerIdOfHoveredProvince(x, y));
        }

        this.updatePosition(screenX, screenY);
        this.tooltip.setVisible(true);
    }

    public void hideMapTooltip() {
        if (this.lockedByUi) {
            return;
        }
        this.tooltip.setVisible(false);
    }

    public void updateUiTooltip(String content) {
        this.lockedByUi = true;
        int screenX = Gdx.input.getX();
        int screenY = Gdx.graphics.getHeight() - Gdx.input.getY();

        this.tooltip.update(content);
        this.updatePosition(screenX, screenY);
        this.tooltip.setVisible(true);
        this.tooltip.toFront();
    }

    public void hideUiTooltip() {
        this.lockedByUi = false;
        this.tooltip.setVisible(false);
    }

    private void updatePosition(float screenX, float screenY) {
        this.tooltip.setPosition(screenX + (float) this.gameContext.getCursorManager().getWidth(), screenY - this.gameContext.getCursorManager().getHeight() * 1.5f);
    }

    @Override
    public void dispose() {

    }
}
