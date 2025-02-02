package com.populaire.projetguerrefroide.economy.building;

import com.populaire.projetguerrefroide.economy.good.Good;

import java.util.Map;

public class EconomyBuilding extends Building {
    private final int workforce;
    private final Map<Good, Integer> inputGoods;
    private final Map<Good, Integer> outputGoods;
    private final short maxLevel;
    private final int color;

    public EconomyBuilding(String name, int cost, short time, int workforce, Map<Good, Integer> inputGoods, Map<Good, Integer> outputGoods, short maxLevel, int color) {
        super(name, cost, time);
        this.workforce = workforce;
        this.inputGoods = inputGoods;
        this.outputGoods = outputGoods;
        this.maxLevel = maxLevel;
        this.color = color;
    }

    public int getWorkforce() {
        return this.workforce;
    }

    public Map<Good, Integer> getInputGoods() {
        return this.inputGoods;
    }

    public Map<Good, Integer> getOutputGoods() {
        return this.outputGoods;
    }

    public short getMaxLevel() {
        return this.maxLevel;
    }

    public int getColor() {
        return this.color;
    }

    @Override
    public String toString() {
        return "EconomyBuilding{" +
            "name='" + this.getName() + '\'' +
            ", cost=" + this.getCost() +
            ", time=" + this.getTime() +
            ", onMap=" + this.isOnMap() +
            ", workforce=" + this.workforce +
            ", inputGoods=" + this.inputGoods +
            ", outputGoods=" + this.outputGoods +
            ", maxLevel=" + this.maxLevel +
            ", color=" + this.color +
            '}';
    }
}
