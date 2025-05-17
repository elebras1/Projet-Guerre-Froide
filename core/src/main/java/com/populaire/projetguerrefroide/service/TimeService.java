package com.populaire.projetguerrefroide.service;

import java.time.LocalDate;

public class TimeService {
    private int speed;
    private LocalDate date;
    private double accumulator;
    private static final double[] DAYS_PER_SECOND = {0.0, 0.5, 1.0, 2.0, 5.0, Double.POSITIVE_INFINITY};

    public TimeService(LocalDate startDate) {
        this.speed = 1;
        this.date = startDate;
        this.accumulator = 0.0;
    }

    public void upSpeed() {
        if(this.speed < 5) {
            this.speed++;
        }
    }

    public void downSpeed() {
        if(this.speed > 0) {
            this.speed--;
        }
    }

    public void update(double deltaSeconds) {
        this.accumulator += deltaSeconds * DAYS_PER_SECOND[speed];
        int daysToAdd = (int) this.accumulator;
        if (daysToAdd > 0) {
            this.date = this.date.plusDays(daysToAdd);
            this.accumulator -= daysToAdd;
            onNewDay(daysToAdd, this.date);
        }
    }


    private void onNewDay(int days, LocalDate newDate) {
        System.out.println("Nouvelle date " + newDate);
    }
}
