package com.populaire.projetguerrefroide.protocol.request;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public final class RequestBoard {
    private static final int CAPACITY = 128;
    private final Request<?>[] requests;
    private final AtomicInteger count;
    private final AtomicLong publishedBatch;
    private final Runnable waker;
    private long currentBatch;
    private boolean submitted;

    public RequestBoard(Runnable waker) {
        this.requests = new Request<?>[CAPACITY];
        this.count = new AtomicInteger();
        this.publishedBatch = new AtomicLong();
        this.waker = waker;
        this.currentBatch = 1;
    }

    public <R extends Response> Request<R> register(Request<R> request) {
        int index = this.count.get();
        if (index == CAPACITY) {
            throw new IllegalStateException("Request board is full");
        }
        this.requests[index] = request;
        this.count.set(index + 1);
        return request;
    }

    public boolean submit(Request<?> request) {
        if (request.submit(this.currentBatch)) {
            this.submitted = true;
            return true;
        }
        return false;
    }

    public void flush() {
        if (!this.submitted) {
            return;
        }
        this.publishedBatch.set(this.currentBatch);
        this.currentBatch++;
        this.submitted = false;
        this.waker.run();
    }

    public int count() {
        return this.count.get();
    }

    public Request<?> get(int index) {
        return this.requests[index];
    }

    public long publishedBatch() {
        return this.publishedBatch.get();
    }
}
