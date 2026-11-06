package com.populaire.projetguerrefroide.protocol.response;

import com.populaire.projetguerrefroide.protocol.request.Response;

public final class BuildingDetails extends Response {
    private long buildingId;
    private int buildingTypeIndex;
    private int regionIndex;
    private int maxLevel;
    private int[] inputGoods = new int[8];
    private int inputGoodCount;
    private int outputGoodIndex;
    private float outputGoodAmount;
    private int workers;
    private int maxWorkers;
    private boolean suspended;
    private boolean stateControlled;

    public void clear(long buildingId) {
        this.buildingId = buildingId;
        this.inputGoodCount = 0;
    }

    public void set(int buildingTypeIndex, int regionIndex, int maxLevel, int outputGoodIndex, float outputGoodAmount, int workers, int maxWorkers, boolean suspended, boolean stateControlled) {
        this.buildingTypeIndex = buildingTypeIndex;
        this.regionIndex = regionIndex;
        this.maxLevel = maxLevel;
        this.outputGoodIndex = outputGoodIndex;
        this.outputGoodAmount = outputGoodAmount;
        this.workers = workers;
        this.maxWorkers = maxWorkers;
        this.suspended = suspended;
        this.stateControlled = stateControlled;
    }

    public void addInputGood(int goodIndex) {
        this.inputGoods = Columns.fit(this.inputGoods, this.inputGoodCount + 1);
        this.inputGoods[this.inputGoodCount++] = goodIndex;
    }

    public long buildingId() {
        return this.buildingId;
    }

    public int buildingTypeIndex() {
        return this.buildingTypeIndex;
    }

    public int regionIndex() {
        return this.regionIndex;
    }

    public int maxLevel() {
        return this.maxLevel;
    }

    public int inputGoodCount() {
        return this.inputGoodCount;
    }

    public int inputGood(int position) {
        return this.inputGoods[position];
    }

    public int outputGoodIndex() {
        return this.outputGoodIndex;
    }

    public float outputGoodAmount() {
        return this.outputGoodAmount;
    }

    public int workers() {
        return this.workers;
    }

    public int maxWorkers() {
        return this.maxWorkers;
    }

    public boolean suspended() {
        return this.suspended;
    }

    public boolean stateControlled() {
        return this.stateControlled;
    }
}
