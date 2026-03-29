package com.populaire.projetguerrefroide.service;

import java.util.Arrays;

import static com.populaire.projetguerrefroide.ProjetGuerreFroide.GOOD_COUNT;

public class EconomyRuntime {
    private float[] marketGoodProductions;
    private float[] marketGoodConsumptions;
    private int localMarketCapacity;
    private int localMarketCount;

    public EconomyRuntime() {
        this.marketGoodProductions = new float[GOOD_COUNT * 2700];
        this.marketGoodConsumptions = new float[GOOD_COUNT * 2700];
        this.localMarketCapacity = 2700;
        this.localMarketCount = 1;
    }

    public int registerLocalMarket() {
        if(this.localMarketCount == this.localMarketCapacity) {
            this.localMarketCapacity++;
            this.marketGoodConsumptions = Arrays.copyOf(this.marketGoodConsumptions, GOOD_COUNT * this.localMarketCapacity);
            this.marketGoodProductions = Arrays.copyOf(this.marketGoodProductions, GOOD_COUNT * this.localMarketCapacity);
        }
        int id = this.localMarketCount;
        this.localMarketCount++;
        return id;
    }

    public void addMarketGoodProductions(int localMarketIndex, int goodIndex, float production) {
        int index = localMarketIndex * GOOD_COUNT + goodIndex;
        this.marketGoodProductions[index] += production;
    }

    public float getMarketGoodProduction(int localMarketIndex, int goodIndex) {
        int index = localMarketIndex * GOOD_COUNT + goodIndex;
        return this.marketGoodProductions[index];
    }

    public void addMarketGoodConsumptions(int localMarketIndex, int goodIndex, float consumption) {
        int index = localMarketIndex * GOOD_COUNT + goodIndex;
        this.marketGoodConsumptions[index] += consumption;
    }

    public float getMarketGoodConsumption(int localMarketIndex, int goodIndex) {
        int index = localMarketIndex * GOOD_COUNT + goodIndex;
        return this.marketGoodConsumptions[index];
    }

    public void reset() {
        Arrays.fill(this.marketGoodProductions, 0f);
        Arrays.fill(this.marketGoodConsumptions, 0f);
    }
}
