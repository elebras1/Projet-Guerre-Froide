package com.populaire.projetguerrefroide.protocol.response;

import com.populaire.projetguerrefroide.protocol.request.Response;

public final class RegionsBuildings extends Response {
    private int[] regionIndexes = new int[32];
    private int[] regionPopulations = new int[32];
    private int[] regionWorkers = new int[32];
    private int[] regionWorkerRatios = new int[32];
    private int[] regionDevelopmentIndexes = new int[32];
    private int[] regionBuildingStarts = new int[32];
    private int[] regionBuildingCounts = new int[32];
    private int regionCount;
    private long[] buildingIds = new long[256];
    private int[] buildingTypes = new int[256];
    private int[] buildingSizes = new int[256];
    private int[] buildingMaxLevels = new int[256];
    private int[] buildingWorkers = new int[256];
    private float[] buildingProductions = new float[256];
    private int[] buildingLevelsQueued = new int[256];
    private boolean[] buildingSuspended = new boolean[256];
    private int buildingCount;

    public void clear() {
        this.regionCount = 0;
        this.buildingCount = 0;
    }

    public void addRegion(int regionIndex, int population, int workers, int workerRatio, int developmentIndex) {
        int size = this.regionCount + 1;
        this.regionIndexes = Columns.fit(this.regionIndexes, size);
        this.regionPopulations = Columns.fit(this.regionPopulations, size);
        this.regionWorkers = Columns.fit(this.regionWorkers, size);
        this.regionWorkerRatios = Columns.fit(this.regionWorkerRatios, size);
        this.regionDevelopmentIndexes = Columns.fit(this.regionDevelopmentIndexes, size);
        this.regionBuildingStarts = Columns.fit(this.regionBuildingStarts, size);
        this.regionBuildingCounts = Columns.fit(this.regionBuildingCounts, size);
        this.regionIndexes[this.regionCount] = regionIndex;
        this.regionPopulations[this.regionCount] = population;
        this.regionWorkers[this.regionCount] = workers;
        this.regionWorkerRatios[this.regionCount] = workerRatio;
        this.regionDevelopmentIndexes[this.regionCount] = developmentIndex;
        this.regionBuildingStarts[this.regionCount] = this.buildingCount;
        this.regionBuildingCounts[this.regionCount] = 0;
        this.regionCount++;
    }

    public void addBuilding(long buildingId, int buildingTypeIndex, int size, int maxLevel, int workers, float production, int levelsQueued, boolean suspended) {
        int capacity = this.buildingCount + 1;
        this.buildingIds = Columns.fit(this.buildingIds, capacity);
        this.buildingTypes = Columns.fit(this.buildingTypes, capacity);
        this.buildingSizes = Columns.fit(this.buildingSizes, capacity);
        this.buildingMaxLevels = Columns.fit(this.buildingMaxLevels, capacity);
        this.buildingWorkers = Columns.fit(this.buildingWorkers, capacity);
        this.buildingProductions = Columns.fit(this.buildingProductions, capacity);
        this.buildingLevelsQueued = Columns.fit(this.buildingLevelsQueued, capacity);
        this.buildingSuspended = Columns.fit(this.buildingSuspended, capacity);
        this.buildingIds[this.buildingCount] = buildingId;
        this.buildingTypes[this.buildingCount] = buildingTypeIndex;
        this.buildingSizes[this.buildingCount] = size;
        this.buildingMaxLevels[this.buildingCount] = maxLevel;
        this.buildingWorkers[this.buildingCount] = workers;
        this.buildingProductions[this.buildingCount] = production;
        this.buildingLevelsQueued[this.buildingCount] = levelsQueued;
        this.buildingSuspended[this.buildingCount] = suspended;
        this.buildingCount++;
        this.regionBuildingCounts[this.regionCount - 1]++;
    }

    public int regionCount() {
        return this.regionCount;
    }

    public int regionIndex(int region) {
        return this.regionIndexes[region];
    }

    public int regionPopulation(int region) {
        return this.regionPopulations[region];
    }

    public int regionWorkers(int region) {
        return this.regionWorkers[region];
    }

    public int regionWorkerRatio(int region) {
        return this.regionWorkerRatios[region];
    }

    public int regionDevelopmentIndex(int region) {
        return this.regionDevelopmentIndexes[region];
    }

    public int regionBuildingStart(int region) {
        return this.regionBuildingStarts[region];
    }

    public int regionBuildingCount(int region) {
        return this.regionBuildingCounts[region];
    }

    public int buildingCount() {
        return this.buildingCount;
    }

    public long buildingId(int building) {
        return this.buildingIds[building];
    }

    public int buildingType(int building) {
        return this.buildingTypes[building];
    }

    public int buildingSize(int building) {
        return this.buildingSizes[building];
    }

    public int buildingMaxLevel(int building) {
        return this.buildingMaxLevels[building];
    }

    public int buildingWorkers(int building) {
        return this.buildingWorkers[building];
    }

    public float buildingProduction(int building) {
        return this.buildingProductions[building];
    }

    public int buildingLevelsQueued(int building) {
        return this.buildingLevelsQueued[building];
    }

    public boolean buildingSuspended(int building) {
        return this.buildingSuspended[building];
    }
}
