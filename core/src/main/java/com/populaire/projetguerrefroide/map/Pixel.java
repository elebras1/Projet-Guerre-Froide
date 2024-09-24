package com.populaire.projetguerrefroide.map;

public class Pixel {
    private final short x;
    private final short y;
    public Pixel(short x, short y) {
        this.x = x;
        this.y = y;
    }

    public short getX() {
        return this.x;
    }

    public short getY() {
        return this.y;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Pixel pixel = (Pixel) o;
        return this.x == pixel.x && this.y == pixel.y;
    }

    @Override
    public int hashCode() {
        return 31 * this.x + this.y;
    }

    @Override
    public String toString() {
        return "Pixel{" +
                "x=" + this.x +
                ", y=" + this.y +
                '}';
    }
}
