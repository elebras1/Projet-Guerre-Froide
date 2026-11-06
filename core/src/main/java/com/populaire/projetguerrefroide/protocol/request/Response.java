package com.populaire.projetguerrefroide.protocol.request;

public abstract class Response {
    private final RequestParams params = new RequestParams();
    private long revision;
    private int tick;
    private long ownershipVersion;
    private boolean found;

    public RequestParams params() {
        return this.params;
    }

    public long revision() {
        return this.revision;
    }

    public int tick() {
        return this.tick;
    }

    public long ownershipVersion() {
        return this.ownershipVersion;
    }

    public boolean found() {
        return this.found;
    }

    public void stamp(RequestParams params, long revision, int tick, long ownershipVersion) {
        this.params.copyFrom(params);
        this.revision = revision;
        this.tick = tick;
        this.ownershipVersion = ownershipVersion;
        this.found = true;
    }

    public void notFound() {
        this.found = false;
    }
}
