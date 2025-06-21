package com.populaire.projetguerrefroide.politics;

import java.time.LocalDate;

public class Minister {
    private final String name;
    private final Ideology ideology;
    private final String imageNameFile;
    private final float loyalty;
    private final MinisterType type;
    private final LocalDate startDate;
    private final LocalDate deathDate;

    public Minister(String name, Ideology ideology, String imageNameFile, float loyalty, MinisterType type, LocalDate startDate, LocalDate deathDate) {
        this.name = name;
        this.ideology = ideology;
        this.imageNameFile = imageNameFile;
        this.loyalty = loyalty;
        this.type = type;
        this.startDate = startDate;
        this.deathDate = deathDate;
    }

    public String getName() {
        return this.name;
    }

    public Ideology getIdeology() {
        return this.ideology;
    }

    public String getImageNameFile() {
        return this.imageNameFile;
    }

    public float getLoyalty() {
        return this.loyalty;
    }

    public MinisterType getType() {
        return this.type;
    }

    public LocalDate getStartDate() {
        return this.startDate;
    }

    public LocalDate getDeathDate() {
        return this.deathDate;
    }

    public String toString() {
        return "Minister{" +
                " name='" + this.name + '\'' +
                ", ideology='" + this.ideology + '\'' +
                ", imageNameFile='" + this.imageNameFile + '\'' +
                ", loyalty=" + this.loyalty +
                ", type='" + this.type + '\'' +
                ", startDate=" + this.startDate +
                ", deathDate=" + this.deathDate +
                '}';
    }
}
