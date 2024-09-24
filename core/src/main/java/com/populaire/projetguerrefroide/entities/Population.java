package com.populaire.projetguerrefroide.entities;

public class Population {
    private int size;
    private int template;

    public Population(int amount, int template) {
        this.size = amount;
        this.template = template;
    }

    public int getSize() {
        return this.size;
    }

    public String toString() {
        return "Population{" +
                "amount=" + this.size +
                ", template=" + this.template +
                '}';
    }
}
