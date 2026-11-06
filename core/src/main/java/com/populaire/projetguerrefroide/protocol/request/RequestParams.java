package com.populaire.projetguerrefroide.protocol.request;

public final class RequestParams {
    private int index;
    private long id;
    private int option;

    public RequestParams() {
        this.clear();
    }

    public void clear() {
        this.index = -1;
        this.id = 0;
        this.option = 0;
    }

    public int index() {
        return this.index;
    }

    public long id() {
        return this.id;
    }

    public int option() {
        return this.option;
    }

    public RequestParams index(int index) {
        this.index = index;
        return this;
    }

    public RequestParams id(long id) {
        this.id = id;
        return this;
    }

    public RequestParams option(int option) {
        this.option = option;
        return this;
    }

    public void copyFrom(RequestParams other) {
        this.index = other.index;
        this.id = other.id;
        this.option = other.option;
    }

    public boolean sameAs(RequestParams other) {
        return this.index == other.index && this.id == other.id && this.option == other.option;
    }
}
