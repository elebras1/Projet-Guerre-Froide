package com.populaire.projetguerrefroide.ui.request;

import com.populaire.projetguerrefroide.protocol.SimulationHeader;
import com.populaire.projetguerrefroide.protocol.request.Request;
import com.populaire.projetguerrefroide.protocol.request.RequestBoard;
import com.populaire.projetguerrefroide.protocol.request.RequestParams;
import com.populaire.projetguerrefroide.protocol.request.Response;
import com.populaire.projetguerrefroide.ui.format.CalendarUtils;

public final class RequestWatch<R extends Response> {
    private final Request<R> request;
    private final RequestBoard board;
    private final SimulationHeader header;
    private final Visibility visibility;
    private final ParamsWriter paramsWriter;
    private final Staleness staleness;
    private final Applier<R> applier;
    private final RequestParams wanted;
    private boolean forced;

    public RequestWatch(Request<R> request, RequestBoard board, SimulationHeader header, Visibility visibility, ParamsWriter paramsWriter, Staleness staleness, Applier<R> applier) {
        this.request = board.register(request);
        this.board = board;
        this.header = header;
        this.visibility = visibility;
        this.paramsWriter = paramsWriter;
        this.staleness = staleness;
        this.applier = applier;
        this.wanted = new RequestParams();
        this.forced = true;
    }

    public void update() {
        if (this.request.receive()) {
            R response = this.request.response();
            this.writeWanted();
            if (response.params().sameAs(this.wanted)) {
                this.applier.apply(response);
            }
        }
        if (!this.visibility.visible() || !this.request.isIdle()) {
            return;
        }
        this.writeWanted();
        boolean needed = this.forced
            || !this.request.hasResponse()
            || !this.request.response().params().sameAs(this.wanted)
            || this.staleness.stale(this.request.response(), this.header);
        if (needed) {
            this.request.params().copyFrom(this.wanted);
            if (this.board.submit(this.request)) {
                this.forced = false;
            }
        }
    }

    public void invalidate() {
        this.forced = true;
    }

    public boolean hasCurrentResponse() {
        if (!this.request.hasResponse()) {
            return false;
        }
        this.writeWanted();
        return this.request.response().params().sameAs(this.wanted);
    }

    public R response() {
        return this.request.hasResponse() ? this.request.response() : null;
    }

    private void writeWanted() {
        this.wanted.clear();
        this.paramsWriter.write(this.wanted);
    }

    public interface Visibility {
        boolean visible();
    }

    public interface ParamsWriter {
        void write(RequestParams params);
    }

    public interface Staleness {
        Staleness EVERY_REVISION = (response, header) -> response.revision() < header.revision();
        Staleness OWNERSHIP = (response, header) -> response.ownershipVersion() < header.ownershipVersion();
        Staleness NEVER = (response, header) -> false;
        Staleness MONTHLY = (response, header) -> CalendarUtils.monthKey(header.startEpochDay() + response.tick()) != CalendarUtils.monthKey(header.epochDay());
        Staleness OWNERSHIP_OR_MONTHLY = (response, header) -> OWNERSHIP.stale(response, header) || MONTHLY.stale(response, header);

        boolean stale(Response response, SimulationHeader header);
    }

    public interface Applier<R extends Response> {
        void apply(R response);
    }
}
