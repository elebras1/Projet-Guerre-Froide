package com.populaire.projetguerrefroide.screen;

import com.badlogic.gdx.utils.Disposable;
import com.populaire.projetguerrefroide.protocol.LoadingStage;
import com.populaire.projetguerrefroide.protocol.SimulationChannel;
import com.populaire.projetguerrefroide.protocol.SimulationHeader;
import com.populaire.projetguerrefroide.protocol.event.EventRing;
import com.populaire.projetguerrefroide.protocol.request.RequestBoard;
import com.populaire.projetguerrefroide.renderer.MapView;
import com.populaire.projetguerrefroide.ui.UiCatalog;
import com.populaire.projetguerrefroide.ui.UiContext;
import com.populaire.projetguerrefroide.ui.UiState;
import com.populaire.projetguerrefroide.ui.skin.SkinNames.Skins;

public final class GameSession implements Disposable {
    private final SimulationChannel channel;
    private final UiContext uiContext;
    private final UiState uiState;
    private UiCatalog uiCatalog;
    private MapView mapView;
    private boolean disposed;

    public GameSession(SimulationChannel channel, UiContext uiContext) {
        this.channel = channel;
        this.uiContext = uiContext;
        this.uiState = new UiState();
    }

    public LoadingStage loadingStage() {
        return this.channel.loadingStage();
    }

    public boolean prepare() {
        this.checkFailure();
        if (this.mapView == null) {
            if (this.channel.loadingStage() != LoadingStage.READY) {
                return false;
            }
            this.uiCatalog = new UiCatalog(this.channel.catalog(), this.uiContext.getLocalisation(), this.uiContext.getAssetManager().get(Skins.FLAGS));
            this.mapView = new MapView(this.channel, this.uiCatalog, this.uiContext, this.uiState);
        }
        this.beginFrame();
        this.mapView.update();
        this.endFrame();
        return this.mapView.isReady();
    }

    public void beginFrame() {
        this.checkFailure();
        this.channel.pump();
        EventRing events = this.channel.events();
        while (events.poll()) {
            this.uiState.version();
        }
    }

    public void endFrame() {
        this.channel.requests().flush();
    }

    private void checkFailure() {
        Throwable failure = this.channel.failure();
        if (failure != null) {
            throw new IllegalStateException("Simulation failed", failure);
        }
    }

    public SimulationChannel channel() {
        return this.channel;
    }

    public SimulationHeader header() {
        return this.channel.header();
    }

    public RequestBoard requests() {
        return this.channel.requests();
    }

    public UiState uiState() {
        return this.uiState;
    }

    public UiCatalog uiCatalog() {
        return this.uiCatalog;
    }

    public MapView mapView() {
        return this.mapView;
    }

    @Override
    public void dispose() {
        if (this.disposed) {
            return;
        }
        this.disposed = true;
        if (this.mapView != null) {
            this.mapView.dispose();
        }
        this.channel.shutdown();
    }
}
