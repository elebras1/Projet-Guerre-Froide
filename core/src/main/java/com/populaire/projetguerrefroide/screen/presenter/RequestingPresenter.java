package com.populaire.projetguerrefroide.screen.presenter;

import com.github.tommyettinger.ds.ObjectList;
import com.populaire.projetguerrefroide.protocol.request.Request;
import com.populaire.projetguerrefroide.protocol.request.RequestKind;
import com.populaire.projetguerrefroide.protocol.request.Response;
import com.populaire.projetguerrefroide.screen.GameSession;
import com.populaire.projetguerrefroide.ui.request.RequestWatch;

import java.util.List;

public abstract class RequestingPresenter implements Presenter {
    protected final GameSession session;
    private final List<RequestWatch<?>> watches;

    protected RequestingPresenter(GameSession session) {
        this.session = session;
        this.watches = new ObjectList<>();
    }

    protected <R extends Response> RequestWatch<R> watch(RequestKind kind, R first, R second, RequestWatch.Visibility visibility, RequestWatch.ParamsWriter params, RequestWatch.Staleness staleness, RequestWatch.Applier<R> applier) {
        RequestWatch<R> watch = new RequestWatch<>(new Request<>(kind, first, second), this.session.requests(), this.session.header(), visibility, params, staleness, applier);
        this.watches.add(watch);
        return watch;
    }

    @Override
    public final void update(float delta) {
        for (int i = 0; i < this.watches.size(); i++) {
            this.watches.get(i).update();
        }
        this.afterRequests(delta);
    }

    protected void afterRequests(float delta) {
    }
}
