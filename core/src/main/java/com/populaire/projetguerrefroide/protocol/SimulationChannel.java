package com.populaire.projetguerrefroide.protocol;

import com.populaire.projetguerrefroide.protocol.catalog.Catalog;
import com.populaire.projetguerrefroide.protocol.command.GameCommand;
import com.populaire.projetguerrefroide.protocol.event.EventRing;
import com.populaire.projetguerrefroide.protocol.request.RequestBoard;

public interface SimulationChannel {
    void start();

    void send(GameCommand command);

    void requestSpeed(int speed);

    void requestPaused(boolean paused);

    RequestBoard requests();

    SimulationHeader header();

    EventRing events();

    LoadingStage loadingStage();

    Catalog catalog();

    Throwable failure();

    void pump();

    void shutdown();
}
