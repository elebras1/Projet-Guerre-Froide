package com.populaire.projetguerrefroide.protocol.request;

import java.util.concurrent.atomic.AtomicInteger;

public final class Request<R extends Response> {
    private static final int IDLE = 0;
    private static final int PENDING = 1;
    private static final int READY = 2;
    private final AtomicInteger state;
    private final RequestKind kind;
    private final RequestParams params;
    private R shown;
    private R filling;
    private long batch;
    private boolean received;

    public Request(RequestKind kind, R first, R second) {
        this.state = new AtomicInteger(IDLE);
        this.kind = kind;
        this.params = new RequestParams();
        this.shown = first;
        this.filling = second;
    }

    public RequestKind kind() {
        return this.kind;
    }

    public boolean isIdle() {
        return this.state.get() == IDLE;
    }

    public RequestParams params() {
        return this.params;
    }

    boolean submit(long batch) {
        if (this.state.get() != IDLE) {
            return false;
        }
        this.batch = batch;
        this.state.set(PENDING);
        return true;
    }

    public boolean receive() {
        if (this.state.get() != READY) {
            return false;
        }
        R previous = this.shown;
        this.shown = this.filling;
        this.filling = previous;
        this.received = true;
        this.state.set(IDLE);
        return true;
    }

    public boolean hasResponse() {
        return this.received;
    }

    public R response() {
        return this.shown;
    }

    public boolean isPendingUpTo(long publishedBatch) {
        return this.state.get() == PENDING && this.batch <= publishedBatch;
    }

    public RequestParams pendingParams() {
        return this.params;
    }

    public R filling() {
        return this.filling;
    }

    public void complete() {
        this.state.set(READY);
    }
}
