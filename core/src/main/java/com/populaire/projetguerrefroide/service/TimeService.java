package com.populaire.projetguerrefroide.service;

import java.time.LocalDate;

public class TimeService {
    private byte speed;
    private LocalDate date;

    public TimeService(LocalDate date) {
        this.speed = 0;
        this.date = date;
    }
}
