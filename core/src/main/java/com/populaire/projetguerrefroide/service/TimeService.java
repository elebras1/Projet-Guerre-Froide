package com.populaire.projetguerrefroide.service;

import com.github.tommyettinger.ds.ObjectList;
import com.populaire.projetguerrefroide.screen.listener.TimeListener;

import java.time.LocalDate;
import java.util.List;

public class TimeService {
    private int speed;
    private boolean paused;
    private LocalDate date;
    private double accumulator;
    private static final double[] DAYS_PER_SECOND = {0.0, 0.5, 1.0, 2.0, 5.0, 10.0};
    private final List<TimeListener> timeListeners;

    public TimeService(LocalDate startDate) {
        this.speed = 1;
        this.paused = true;
        this.date = startDate;
        this.accumulator = 0.0;
        this.timeListeners = new ObjectList<>();
    }

    public void addListener(TimeListener timeListener) {
        this.timeListeners.add(timeListener);
    }

    public void initialize() {
        this.onNewDay(this.date);
    }

    public int upSpeed() {
        if(this.speed < 5) {
            this.speed++;
        }

        return this.getDisplaySpeed();
    }

    public int downSpeed() {
        if(this.speed > 1) {
            this.speed--;
        }

        return this.getDisplaySpeed();
    }

    public int togglePause() {
        this.paused = !this.paused;
        return this.getDisplaySpeed();
    }

    public void update(double deltaSeconds) {
        if (this.paused) {
            return;
        }
        this.accumulator += deltaSeconds * DAYS_PER_SECOND[speed];
        int daysToAdd = (int) this.accumulator;
        if (daysToAdd > 0) {
            this.date = this.date.plusDays(daysToAdd);
            this.accumulator -= daysToAdd;
            this.onNewDay(this.date);
        }
    }

    private void onNewDay(LocalDate newDate) {
        for(TimeListener timeListener : this.timeListeners) {
            timeListener.onNewDay(newDate);
        }
    }

    private int getDisplaySpeed() {
        return this.paused ? 0 : this.speed;
    }
}
