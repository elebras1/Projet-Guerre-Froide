package com.populaire.projetguerrefroide.simulation;

import com.populaire.projetguerrefroide.protocol.SimulationHeader;

public final class SimulationClock {
    public static final int MIN_SPEED = SimulationHeader.MIN_SPEED;
    public static final int MAX_SPEED = SimulationHeader.MAX_SPEED;
    private static final double[] DAYS_PER_SECOND = {0.0, 0.5, 1.0, 2.0, 5.0, 10.0};
    private static final long NANOS_PER_SECOND = 1_000_000_000L;
    private int speed;
    private boolean paused;
    private long lastTickStart;
    private long nextTickAt;
    private int tick;

    public SimulationClock() {
        this.speed = MIN_SPEED;
        this.paused = true;
    }

    public int speed() {
        return this.speed;
    }

    public boolean paused() {
        return this.paused;
    }

    public int tick() {
        return this.tick;
    }

    public boolean apply(int requestedSpeed, boolean requestedPaused, long now) {
        int clampedSpeed = Math.max(MIN_SPEED, Math.min(MAX_SPEED, requestedSpeed));
        boolean changed = false;
        if (clampedSpeed != this.speed) {
            this.speed = clampedSpeed;
            this.nextTickAt = Math.max(now, this.lastTickStart + this.period());
            changed = true;
        }
        if (requestedPaused != this.paused) {
            this.paused = requestedPaused;
            if (!this.paused) {
                this.nextTickAt = now + this.period();
            }
            changed = true;
        }
        return changed;
    }

    public boolean tickDue(long now) {
        return !this.paused && now >= this.nextTickAt;
    }

    public void ticked(long tickStart, long tickEnd) {
        this.tick++;
        this.lastTickStart = tickStart;
        this.nextTickAt = Math.max(tickStart + this.period(), tickEnd);
    }

    public long nanosUntilNextTick(long now) {
        if (this.paused) {
            return Long.MAX_VALUE;
        }
        return Math.max(0L, this.nextTickAt - now);
    }

    private long period() {
        return (long) (NANOS_PER_SECOND / DAYS_PER_SECOND[this.speed]);
    }
}
