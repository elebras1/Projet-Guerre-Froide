package com.populaire.projetguerrefroide.util;

public class IndexedPoint {
    private int index;
    private int x;
    private int y;

    public IndexedPoint() {
        this.index = -1;
        this.x = 0;
        this.y = 0;
    }

    public int getIndex() {
        return this.index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public int getX() {
        return this.x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return this.y;
    }

    public void setY(int y) {
        this.y = y;
    }
}
