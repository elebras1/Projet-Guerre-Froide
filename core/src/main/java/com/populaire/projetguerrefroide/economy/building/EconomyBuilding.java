package com.populaire.projetguerrefroide.economy.building;

import com.populaire.projetguerrefroide.economy.good.Good;

import java.util.Map;

public class EconomyBuilding extends Building {
    private final ProductionType baseType;
    private final ProductionType artisansType;
    private final Map<Good, Integer> inputGoods;
    private final Map<Good, Integer> outputGoods;
    private final short maxLevel;
    private final int color;

    public EconomyBuilding(ProductionType baseTemplate, ProductionType artisansTemplate, String name, int cost, short time, Map<Good, Integer> inputGoods, Map<Good, Integer> outputGoods, short maxLevel, int color) {
        super(name, cost, time);
        this.baseType = baseTemplate;
        this.artisansType = artisansTemplate;
        this.inputGoods = inputGoods;
        this.outputGoods = outputGoods;
        this.maxLevel = maxLevel;
        this.color = color;
    }

    public ProductionType getBaseType() {
        return this.baseType;
    }

    public ProductionType getArtisansType() {
        return this.artisansType;
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
            ", baseType=" + this.baseType +
            ", artisansType=" + this.artisansType +
            ", inputGoods=" + this.inputGoods +
            ", outputGoods=" + this.outputGoods +
            ", maxLevel=" + this.maxLevel +
            ", color=" + this.color +
            '}';
    }
}
