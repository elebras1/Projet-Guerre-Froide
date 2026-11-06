package com.populaire.projetguerrefroide.protocol;

public final class SimulationHeader {
    public static final int MIN_SPEED = 1;
    public static final int MAX_SPEED = 5;
    private final long startEpochDay;
    private volatile int tick;
    private volatile int speed;
    private volatile boolean paused;
    private volatile long ownershipVersion;
    private volatile long revision;

    public SimulationHeader(long startEpochDay) {
        this.startEpochDay = startEpochDay;
        this.speed = 1;
        this.paused = true;
        this.ownershipVersion = 1;
    }

    public long startEpochDay() {
        return this.startEpochDay;
    }

    public long epochDay() {
        return this.startEpochDay + this.tick;
    }

    public int tick() {
        return this.tick;
    }

    public int speed() {
        return this.speed;
    }

    public boolean paused() {
        return this.paused;
    }

    public long ownershipVersion() {
        return this.ownershipVersion;
    }

    public long revision() {
        return this.revision;
    }

    public void publishClock(int speed, boolean paused) {
        this.speed = speed;
        this.paused = paused;
    }

    public void publishTick(int tick) {
        this.tick = tick;
        this.revision = this.revision + 1;
    }

    public void publishOwnershipChange() {
        this.ownershipVersion = this.ownershipVersion + 1;
        this.revision = this.revision + 1;
    }

    public void publishChange() {
        this.revision = this.revision + 1;
    }
}
