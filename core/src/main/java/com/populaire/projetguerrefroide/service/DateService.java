package com.populaire.projetguerrefroide.service;

import com.populaire.projetguerrefroide.screen.DateListener;

import java.time.LocalDate;

public class DateService {
    private int speed;
    private boolean paused;
    private LocalDate date;
    private double accumulator;
    private static final double[] DAYS_PER_SECOND = {0.0, 0.5, 1.0, 2.0, 5.0, 10.0};
    private final DateListener dateListener;

    public DateService(LocalDate startDate, DateListener dateListener) {
        this.speed = 1;
        this.paused = true;
        this.date = startDate;
        this.accumulator = 0.0;
        this.dateListener = dateListener;
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
            onNewDay(this.date);
        }
    }

    private void onNewDay(LocalDate newDate) {
        this.dateListener.onNewDay(newDate);
    }

    private int getDisplaySpeed() {
        return this.paused ? 0 : this.speed;
    }
}
