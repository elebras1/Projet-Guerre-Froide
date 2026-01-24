package com.populaire.projetguerrefroide.screen.presenter;

import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.populaire.projetguerrefroide.dto.ProvinceDto;
import com.populaire.projetguerrefroide.screen.listener.TimeListener;
import com.populaire.projetguerrefroide.service.GameContext;
import com.populaire.projetguerrefroide.service.WorldService;
import com.populaire.projetguerrefroide.ui.view.ProvincePanel;
import com.populaire.projetguerrefroide.ui.widget.WidgetFactory;

import java.time.LocalDate;

public class ProvincePanelPresenter implements Presenter, TimeListener {
    private final GameContext gameContext;
    private final WorldService worldService;
    private final WidgetFactory widgetFactory;
    private final Skin skinProvince;
    private final Skin skinUi;
    private final Skin skinFlags;
    private ProvincePanel provincePanel;

    public ProvincePanelPresenter(GameContext gameContext, WorldService worldService, WidgetFactory widgetFactory, Skin skinProvince, Skin skinUi, Skin skinFlags) {
        this.gameContext = gameContext;
        this.worldService = worldService;
        this.widgetFactory = widgetFactory;
        this.skinProvince = skinProvince;
        this.skinUi = skinUi;
        this.skinFlags = skinFlags;
    }

    @Override
    public void initialize(Stage stage) {
        this.provincePanel = new ProvincePanel(this.widgetFactory, this.skinProvince, this.skinUi, this.skinFlags, this.gameContext.getLabelStylePool(), this.gameContext.getLocalisation());
        this.provincePanel.setPosition(0, 0);
        this.provincePanel.setVisible(false);
        stage.addActor(this.provincePanel);
    }

    @Override
    public void refresh() {
        if(this.worldService.isProvinceSelected()) {
            ProvinceDto provinceDto = this.worldService.buildProvinceDetails();
            this.provincePanel.setData(provinceDto);
            this.provincePanel.setVisible(true);
        }
    }

    @Override
    public void update(float delta) {

    }

    @Override
    public void onNewDay(LocalDate date) {
        this.provincePanel.setResourceProduced(this.worldService.getResourceGoodsProduction());
    }

    @Override
    public void dispose() {

    }
}
