package com.populaire.projetguerrefroide.economy.building;

import com.github.tommyettinger.ds.ObjectFloatMap;
import com.populaire.projetguerrefroide.economy.good.Good;

public class EconomyBuilding extends Building {
    private final ProductionType baseType;
    private final ProductionType artisansType;
    private final ObjectFloatMap<Good> goodsCost;
    private final ObjectFloatMap<Good> inputGoods;
    private final ObjectFloatMap<Good> outputGoods;
    private final short maxLevel;
    private final int color;

    public EconomyBuilding(ProductionType baseTemplate, ProductionType artisansTemplate, String name, short time, ObjectFloatMap<Good> goodsCost, ObjectFloatMap<Good> inputGoods, ObjectFloatMap<Good> outputGoods, short maxLevel, int color) {
        super(name, time);
        this.baseType = baseTemplate;
        this.artisansType = artisansTemplate;
        this.goodsCost = goodsCost;
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

    public ObjectFloatMap<Good> getGoodsCost() {
        return this.goodsCost;
    }

    public ObjectFloatMap<Good> getInputGoods() {
        return this.inputGoods;
    }

    public ObjectFloatMap<Good> getOutputGoods() {
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
            ", goodsCost=" + this.goodsCost +
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
