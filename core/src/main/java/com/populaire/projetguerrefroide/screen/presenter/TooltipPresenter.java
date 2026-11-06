package com.populaire.projetguerrefroide.screen.presenter;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.populaire.projetguerrefroide.pojo.MapMode;
import com.populaire.projetguerrefroide.protocol.request.RequestKind;
import com.populaire.projetguerrefroide.protocol.response.ProvinceHover;
import com.populaire.projetguerrefroide.screen.GameSession;
import com.populaire.projetguerrefroide.ui.UiContext;
import com.populaire.projetguerrefroide.ui.request.RequestWatch;
import com.populaire.projetguerrefroide.ui.view.Tooltip;
import com.populaire.projetguerrefroide.ui.widget.WidgetFactory;

public class TooltipPresenter extends RequestingPresenter {
    private final UiContext uiContext;
    private final WidgetFactory widgetFactory;
    private final Skin skinUi;
    private final Skin skinFlags;
    private Tooltip tooltip;
    private RequestWatch<ProvinceHover> hoverWatch;
    private boolean lockedByUi;
    private boolean mapTooltipShown;

    public TooltipPresenter(GameSession session, UiContext context, WidgetFactory factory, Skin skinUi, Skin skinFlags) {
        super(session);
        this.uiContext = context;
        this.widgetFactory = factory;
        this.skinUi = skinUi;
        this.skinFlags = skinFlags;
        this.lockedByUi = false;
    }

    @Override
    public void initialize(Stage stage) {
        this.tooltip = new Tooltip(this.widgetFactory, this.skinUi, this.skinFlags, this.uiContext.getSkinFonts(), this.uiContext.getLocalisation());
        this.tooltip.setVisible(false);
        stage.addActor(this.tooltip);
        this.hoverWatch = this.watch(RequestKind.PROVINCE_HOVER, new ProvinceHover(), new ProvinceHover(),
            () -> this.mapTooltipShown && this.showsElements(), params -> params.index(this.session.uiState().hoveredProvince()), RequestWatch.Staleness.EVERY_REVISION, hover -> this.refreshMapTooltip());
    }

    private boolean showsElements() {
        MapMode mapMode = this.session.uiState().mapMode();
        return mapMode == MapMode.CULTURAL || mapMode == MapMode.RELIGIOUS;
    }

    public void updateMapTooltip(int provinceIndex) {
        if (this.lockedByUi) {
            return;
        }
        this.session.uiState().hoverProvince(provinceIndex);
        this.mapTooltipShown = true;
        this.refreshMapTooltip();
        this.updatePosition(Gdx.input.getX(), Gdx.graphics.getHeight() - Gdx.input.getY());
        this.tooltip.setVisible(true);
    }

    private void refreshMapTooltip() {
        if (!this.mapTooltipShown || this.lockedByUi) {
            return;
        }
        int provinceIndex = this.session.uiState().hoveredProvince();
        if (provinceIndex < 0) {
            return;
        }
        int ownerIndex = this.session.mapView().ownerOf(provinceIndex);
        ProvinceHover hover = null;
        if (this.showsElements() && this.hoverWatch.hasCurrentResponse() && this.hoverWatch.response().found()) {
            hover = this.hoverWatch.response();
        }
        this.tooltip.updateProvince(this.session.uiCatalog(), provinceIndex, ownerIndex, hover, this.session.uiState().mapMode() == MapMode.CULTURAL);
    }

    public void hideMapTooltip() {
        if (this.lockedByUi) {
            return;
        }
        this.mapTooltipShown = false;
        this.session.uiState().hoverProvince(-1);
        this.tooltip.setVisible(false);
    }

    public void updateUiTooltip(String content) {
        this.lockedByUi = true;
        this.mapTooltipShown = false;
        this.tooltip.update(content);
        this.updatePosition(Gdx.input.getX(), Gdx.graphics.getHeight() - Gdx.input.getY());
        this.tooltip.setVisible(true);
        this.tooltip.toFront();
    }

    public void hideUiTooltip() {
        this.lockedByUi = false;
        this.tooltip.setVisible(false);
    }

    private void updatePosition(float screenX, float screenY) {
        this.tooltip.setPosition(screenX + (float) this.uiContext.getCursorManager().getWidth(), screenY - this.uiContext.getCursorManager().getHeight() * 1.5f);
    }

    @Override
    public void dispose() {
    }
}
