package com.populaire.projetguerrefroide.screen.presenter;

import com.badlogic.gdx.scenes.scene2d.Stage;
import com.populaire.projetguerrefroide.protocol.request.RequestKind;
import com.populaire.projetguerrefroide.protocol.response.ProvinceDetails;
import com.populaire.projetguerrefroide.screen.GameFlowHandler;
import com.populaire.projetguerrefroide.screen.GameSession;
import com.populaire.projetguerrefroide.ui.UiContext;
import com.populaire.projetguerrefroide.ui.format.TextFormat;
import com.populaire.projetguerrefroide.ui.request.RequestWatch;
import com.populaire.projetguerrefroide.ui.view.province.ProvincePanel;
import com.populaire.projetguerrefroide.ui.view.province.ProvincePanelListener;
import com.populaire.projetguerrefroide.ui.widget.UiBuilder;

public class ProvincePanelPresenter extends RequestingPresenter implements ProvincePanelListener {
    private final UiContext uiContext;
    private final GameFlowHandler gameFlowHandler;
    private final TextFormat textFormat;
    private ProvincePanel provincePanel;
    private RequestWatch<ProvinceDetails> provinceWatch;
    private int panelProvince;
    private boolean opening;

    public ProvincePanelPresenter(GameSession session, UiContext uiContext, GameFlowHandler gameFlowHandler, TextFormat textFormat) {
        super(session);
        this.uiContext = uiContext;
        this.gameFlowHandler = gameFlowHandler;
        this.textFormat = textFormat;
        this.panelProvince = -1;
    }

    @Override
    public void initialize(Stage stage) {
        UiBuilder ui = new UiBuilder(this.uiContext);
        this.provincePanel = new ProvincePanel(ui, this.session.uiCatalog(), this.textFormat, this);
        this.provincePanel.setPosition(0, 0);
        this.provincePanel.setVisible(false);
        stage.addActor(this.provincePanel);
        this.provinceWatch = this.watch(RequestKind.PROVINCE_DETAILS, new ProvinceDetails(), new ProvinceDetails(),
            () -> this.panelProvince >= 0 && (this.opening || this.provincePanel.isVisible()),
            params -> params.index(this.panelProvince), RequestWatch.Staleness.EVERY_REVISION, this::applyProvince);
    }

    public void open(int provinceIndex) {
        if (provinceIndex < 0) {
            return;
        }
        this.panelProvince = provinceIndex;
        this.opening = true;
        this.provinceWatch.invalidate();
    }

    private void applyProvince(ProvinceDetails province) {
        if (!province.found()) {
            this.opening = false;
            return;
        }
        this.provincePanel.setData(province);
        if (this.opening) {
            this.provincePanel.setVisible(true);
            this.opening = false;
        }
    }

    @Override
    public void updateHoverTooltip(String content) {
        this.gameFlowHandler.showTooltip(content);
    }

    @Override
    public void hideUiTooltip() {
        this.gameFlowHandler.hideTooltip();
    }

    @Override
    public void dispose() {
    }
}
