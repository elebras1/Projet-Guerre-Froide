package com.populaire.projetguerrefroide.ui;

import com.populaire.projetguerrefroide.protocol.request.MapMode;
import com.populaire.projetguerrefroide.protocol.request.SortType;

public final class UiState {
    private long version;
    private int selectedProvince = -1;
    private int hoveredProvince = -1;
    private long selectedBuilding;
    private boolean provincePanelOpen;
    private boolean economyPanelOpen;
    private boolean countryRankingOpen;
    private SortType regionSort = SortType.DEFAULT;
    private MapMode mapMode = MapMode.POLITICAL;
    private int requestedSpeed = 1;
    private boolean requestedPaused = true;
    private int playerCountry = -1;

    public long version() {
        return this.version;
    }

    private void changed() {
        this.version++;
    }

    public int selectedProvince() {
        return this.selectedProvince;
    }

    public void selectProvince(int provinceIndex) {
        if (this.selectedProvince != provinceIndex) {
            this.selectedProvince = provinceIndex;
            this.changed();
        }
    }

    public int hoveredProvince() {
        return this.hoveredProvince;
    }

    public void hoverProvince(int provinceIndex) {
        if (this.hoveredProvince != provinceIndex) {
            this.hoveredProvince = provinceIndex;
            this.changed();
        }
    }

    public long selectedBuilding() {
        return this.selectedBuilding;
    }

    public void selectBuilding(long buildingId) {
        if (this.selectedBuilding != buildingId) {
            this.selectedBuilding = buildingId;
            this.changed();
        }
    }

    public boolean provincePanelOpen() {
        return this.provincePanelOpen;
    }

    public void provincePanelOpen(boolean open) {
        if (this.provincePanelOpen != open) {
            this.provincePanelOpen = open;
            this.changed();
        }
    }

    public boolean economyPanelOpen() {
        return this.economyPanelOpen;
    }

    public void economyPanelOpen(boolean open) {
        if (this.economyPanelOpen != open) {
            this.economyPanelOpen = open;
            this.changed();
        }
    }

    public boolean countryRankingOpen() {
        return this.countryRankingOpen;
    }

    public void countryRankingOpen(boolean open) {
        if (this.countryRankingOpen != open) {
            this.countryRankingOpen = open;
            this.changed();
        }
    }

    public SortType regionSort() {
        return this.regionSort;
    }

    public void regionSort(SortType sortType) {
        if (this.regionSort != sortType) {
            this.regionSort = sortType;
            this.changed();
        }
    }

    public MapMode mapMode() {
        return this.mapMode;
    }

    public void mapMode(MapMode mapMode) {
        if (this.mapMode != mapMode) {
            this.mapMode = mapMode;
            this.changed();
        }
    }

    public int requestedSpeed() {
        return this.requestedSpeed;
    }

    public void requestedSpeed(int speed) {
        if (this.requestedSpeed != speed) {
            this.requestedSpeed = speed;
            this.changed();
        }
    }

    public boolean requestedPaused() {
        return this.requestedPaused;
    }

    public void requestedPaused(boolean paused) {
        if (this.requestedPaused != paused) {
            this.requestedPaused = paused;
            this.changed();
        }
    }

    public int displaySpeed() {
        return this.requestedPaused ? 0 : this.requestedSpeed;
    }

    public int playerCountry() {
        return this.playerCountry;
    }

    public void playerCountry(int countryIndex) {
        if (this.playerCountry != countryIndex) {
            this.playerCountry = countryIndex;
            this.changed();
        }
    }
}
