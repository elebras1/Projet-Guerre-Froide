package com.populaire.projetguerrefroide.entity;

import java.util.Date;

public class Minister {
    private final String name;
    private final String ideology;
    private final String imageNameFile;
    private final float loyalty;
    private final String headOfState;
    private final String headOfGovernment;
    private final Date startDate;
    private final Date deathDate;
    private final int base;

    public Minister(String name, String ideology, String imageNameFile, float loyalty, String headOfState, String headOfGovernment, Date startDate, Date deathDate, int base) {
        this.name = name;
        this.ideology = ideology;
        this.imageNameFile = imageNameFile;
        this.loyalty = loyalty;
        this.headOfState = headOfState;
        this.headOfGovernment = headOfGovernment;
        this.startDate = startDate;
        this.deathDate = deathDate;
        this.base = base;
    }

    public String getName() {
        return this.name;
    }

    public String getIdeology() {
        return this.ideology;
    }

    public String getImageNameFile() {
        return this.imageNameFile;
    }

    public float getLoyalty() {
        return this.loyalty;
    }

    public String getHeadOfState() {
        return this.headOfState;
    }

    public String getHeadOfGovernment() {
        return this.headOfGovernment;
    }

    public Date getStartDate() {
        return this.startDate;
    }

    public Date getDeathDate() {
        return this.deathDate;
    }

    public int getBase() {
        return this.base;
    }

    public String toString() {
        return "Minister{" +
                " name='" + this.name + '\'' +
                ", ideology='" + this.ideology + '\'' +
                ", imageNameFile='" + this.imageNameFile + '\'' +
                ", loyalty=" + this.loyalty +
                ", headOfState='" + this.headOfState + '\'' +
                ", headOfGovernment='" + this.headOfGovernment + '\'' +
                ", startDate=" + this.startDate +
                ", deathDate=" + this.deathDate +
                ", base=" + this.base +
                '}';
    }
}
