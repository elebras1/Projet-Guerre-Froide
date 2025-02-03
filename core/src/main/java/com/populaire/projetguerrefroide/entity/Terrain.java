package com.populaire.projetguerrefroide.entity;

public class Terrain {
    private final String name;
    private final byte movementCost;
    private final byte temperature;
    private final byte humidity;
    private final byte precipitation;
    private final int color;

    public Terrain(String name, byte movementCost, byte temperature, byte humidity, byte precipitation, int color) {
        this.name = name;
        this.movementCost = movementCost;
        this.temperature = temperature;
        this.humidity = humidity;
        this.precipitation = precipitation;
        this.color = color;
    }

    public String getName() {
        return this.name;
    }

    public byte getMovementCost() {
        return this.movementCost;
    }

    public byte getTemperature() {
        return this.temperature;
    }

    public byte getHumidity() {
        return this.humidity;
    }

    public byte getPrecipitation() {
        return this.precipitation;
    }

    public int getColor() {
        return this.color;
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Terrain terrain)) return false;

        return this.name.equals(terrain.name);
    }

    @Override
    public int hashCode() {
        return this.name.hashCode();
    }

    @Override
    public String toString() {
        return "Terrain{" +
            "name='" + this.name + '\'' +
            ", movementCost=" + this.movementCost +
            ", temperature=" + this.temperature +
            ", humidity=" + this.humidity +
            ", precipitation=" + this.precipitation +
            ", color=" + this.color +
            '}';
    }
}
