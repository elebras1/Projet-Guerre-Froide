package com.populaire.projetguerrefroide.protocol.response;

import com.populaire.projetguerrefroide.protocol.request.Response;

public final class ProvinceDetails extends Response {
    private int provinceIndex;
    private int regionIndex;
    private int terrainIndex;
    private int resourceGoodIndex;
    private int populationRegion;
    private int workersRegion;
    private int developmentIndexRegion;
    private int incomeRegion;
    private int industryRegion;
    private int ownerIndex;
    private int colonizerIndex;
    private float resourceProduced;
    private int infrastructureValue;
    private int guerillaValue;
    private int populationProvince;
    private float incomeProvince;
    private float revoltRisk;
    private int navalBaseLevel;
    private int airBaseLevel;
    private int radarStationLevel;
    private int antiAircraftGunsLevel;
    private int[] cores = new int[8];
    private int coreCount;
    private int[] regionProvinces = new int[32];
    private int[] regionProvinceAdults = new int[32];
    private int regionProvinceCount;
    private int[] economyBuildingTypes = new int[32];
    private int[] economyBuildingSizes = new int[32];
    private int economyBuildingCount;
    private int[] specialTypes = new int[16];
    private int[] specialLevels = new int[16];
    private int[] specialMaxLevels = new int[16];
    private int[] specialStaffPosts = new int[16];
    private int[] specialStaffFilled = new int[16];
    private float[] specialEffectiveLevels = new float[16];
    private float[] specialStorageCapacities = new float[16];
    private int specialCount;

    public void clear(int provinceIndex) {
        this.provinceIndex = provinceIndex;
        this.coreCount = 0;
        this.regionProvinceCount = 0;
        this.economyBuildingCount = 0;
        this.specialCount = 0;
        this.navalBaseLevel = 0;
        this.airBaseLevel = 0;
        this.radarStationLevel = 0;
        this.antiAircraftGunsLevel = 0;
    }

    public void setProvince(int regionIndex, int terrainIndex, int resourceGoodIndex, int ownerIndex, int colonizerIndex, float resourceProduced, int populationProvince) {
        this.regionIndex = regionIndex;
        this.terrainIndex = terrainIndex;
        this.resourceGoodIndex = resourceGoodIndex;
        this.ownerIndex = ownerIndex;
        this.colonizerIndex = colonizerIndex;
        this.resourceProduced = resourceProduced;
        this.populationProvince = populationProvince;
    }

    public void setRegion(int populationRegion, int workersRegion, int developmentIndexRegion, int incomeRegion, int industryRegion) {
        this.populationRegion = populationRegion;
        this.workersRegion = workersRegion;
        this.developmentIndexRegion = developmentIndexRegion;
        this.incomeRegion = incomeRegion;
        this.industryRegion = industryRegion;
    }

    public void setLocal(int infrastructureValue, int guerillaValue, float incomeProvince, float revoltRisk) {
        this.infrastructureValue = infrastructureValue;
        this.guerillaValue = guerillaValue;
        this.incomeProvince = incomeProvince;
        this.revoltRisk = revoltRisk;
    }

    public void setDevelopmentLevels(int navalBaseLevel, int airBaseLevel, int radarStationLevel, int antiAircraftGunsLevel) {
        this.navalBaseLevel = navalBaseLevel;
        this.airBaseLevel = airBaseLevel;
        this.radarStationLevel = radarStationLevel;
        this.antiAircraftGunsLevel = antiAircraftGunsLevel;
    }

    public void addCore(int countryIndex) {
        this.cores = Columns.fit(this.cores, this.coreCount + 1);
        this.cores[this.coreCount++] = countryIndex;
    }

    public void addRegionProvince(int provinceIndex, int adults) {
        this.regionProvinces = Columns.fit(this.regionProvinces, this.regionProvinceCount + 1);
        this.regionProvinceAdults = Columns.fit(this.regionProvinceAdults, this.regionProvinceCount + 1);
        this.regionProvinces[this.regionProvinceCount] = provinceIndex;
        this.regionProvinceAdults[this.regionProvinceCount] = adults;
        this.regionProvinceCount++;
    }

    public void sortRegionProvincesByAdults() {
        for (int i = 1; i < this.regionProvinceCount; i++) {
            int province = this.regionProvinces[i];
            int adults = this.regionProvinceAdults[i];
            int j = i - 1;
            while (j >= 0 && this.regionProvinceAdults[j] < adults) {
                this.regionProvinces[j + 1] = this.regionProvinces[j];
                this.regionProvinceAdults[j + 1] = this.regionProvinceAdults[j];
                j--;
            }
            this.regionProvinces[j + 1] = province;
            this.regionProvinceAdults[j + 1] = adults;
        }
    }

    public void addEconomyBuilding(int buildingTypeIndex, int size) {
        this.economyBuildingTypes = Columns.fit(this.economyBuildingTypes, this.economyBuildingCount + 1);
        this.economyBuildingSizes = Columns.fit(this.economyBuildingSizes, this.economyBuildingCount + 1);
        this.economyBuildingTypes[this.economyBuildingCount] = buildingTypeIndex;
        this.economyBuildingSizes[this.economyBuildingCount] = size;
        this.economyBuildingCount++;
    }

    public void sortEconomyBuildingsBySize() {
        for (int i = 1; i < this.economyBuildingCount; i++) {
            int type = this.economyBuildingTypes[i];
            int size = this.economyBuildingSizes[i];
            int j = i - 1;
            while (j >= 0 && this.economyBuildingSizes[j] < size) {
                this.economyBuildingTypes[j + 1] = this.economyBuildingTypes[j];
                this.economyBuildingSizes[j + 1] = this.economyBuildingSizes[j];
                j--;
            }
            this.economyBuildingTypes[j + 1] = type;
            this.economyBuildingSizes[j + 1] = size;
        }
    }

    public void addSpecialBuilding(int buildingTypeIndex, int level, int maxLevel, int staffPosts, int staffFilled, float effectiveLevel, float storageCapacity) {
        int size = this.specialCount + 1;
        this.specialTypes = Columns.fit(this.specialTypes, size);
        this.specialLevels = Columns.fit(this.specialLevels, size);
        this.specialMaxLevels = Columns.fit(this.specialMaxLevels, size);
        this.specialStaffPosts = Columns.fit(this.specialStaffPosts, size);
        this.specialStaffFilled = Columns.fit(this.specialStaffFilled, size);
        this.specialEffectiveLevels = Columns.fit(this.specialEffectiveLevels, size);
        this.specialStorageCapacities = Columns.fit(this.specialStorageCapacities, size);
        this.specialTypes[this.specialCount] = buildingTypeIndex;
        this.specialLevels[this.specialCount] = level;
        this.specialMaxLevels[this.specialCount] = maxLevel;
        this.specialStaffPosts[this.specialCount] = staffPosts;
        this.specialStaffFilled[this.specialCount] = staffFilled;
        this.specialEffectiveLevels[this.specialCount] = effectiveLevel;
        this.specialStorageCapacities[this.specialCount] = storageCapacity;
        this.specialCount++;
    }

    public int provinceIndex() {
        return this.provinceIndex;
    }

    public int regionIndex() {
        return this.regionIndex;
    }

    public int terrainIndex() {
        return this.terrainIndex;
    }

    public int resourceGoodIndex() {
        return this.resourceGoodIndex;
    }

    public int populationRegion() {
        return this.populationRegion;
    }

    public int workersRegion() {
        return this.workersRegion;
    }

    public int developmentIndexRegion() {
        return this.developmentIndexRegion;
    }

    public int incomeRegion() {
        return this.incomeRegion;
    }

    public int industryRegion() {
        return this.industryRegion;
    }

    public int ownerIndex() {
        return this.ownerIndex;
    }

    public int colonizerIndex() {
        return this.colonizerIndex;
    }

    public float resourceProduced() {
        return this.resourceProduced;
    }

    public int infrastructureValue() {
        return this.infrastructureValue;
    }

    public int guerillaValue() {
        return this.guerillaValue;
    }

    public int populationProvince() {
        return this.populationProvince;
    }

    public float incomeProvince() {
        return this.incomeProvince;
    }

    public float revoltRisk() {
        return this.revoltRisk;
    }

    public int navalBaseLevel() {
        return this.navalBaseLevel;
    }

    public int airBaseLevel() {
        return this.airBaseLevel;
    }

    public int radarStationLevel() {
        return this.radarStationLevel;
    }

    public int antiAircraftGunsLevel() {
        return this.antiAircraftGunsLevel;
    }

    public int coreCount() {
        return this.coreCount;
    }

    public int core(int position) {
        return this.cores[position];
    }

    public int regionProvinceCount() {
        return this.regionProvinceCount;
    }

    public int regionProvince(int position) {
        return this.regionProvinces[position];
    }

    public int economyBuildingCount() {
        return this.economyBuildingCount;
    }

    public int economyBuildingType(int position) {
        return this.economyBuildingTypes[position];
    }

    public int specialCount() {
        return this.specialCount;
    }

    public int specialType(int position) {
        return this.specialTypes[position];
    }

    public int specialLevel(int position) {
        return this.specialLevels[position];
    }

    public int specialMaxLevel(int position) {
        return this.specialMaxLevels[position];
    }

    public int specialStaffPosts(int position) {
        return this.specialStaffPosts[position];
    }

    public int specialStaffFilled(int position) {
        return this.specialStaffFilled[position];
    }

    public float specialEffectiveLevel(int position) {
        return this.specialEffectiveLevels[position];
    }

    public float specialStorageCapacity(int position) {
        return this.specialStorageCapacities[position];
    }
}
