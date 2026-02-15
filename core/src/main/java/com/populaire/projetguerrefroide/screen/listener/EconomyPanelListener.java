package com.populaire.projetguerrefroide.screen.listener;

import com.populaire.projetguerrefroide.ui.view.SortType;

public interface EconomyPanelListener {
    void onCloseEconomyPanelClicked();
    void onSortRegions(SortType sortType);
    void onBuildingClicked(long buildingId);
    void onExpandBuildingClicked(long buildingId);
    void onSuspendBuildingClicked(long buildingId);
    void onResumeBuildingClicked(long buildingId);
    void onDemolishBuildingClicked(long buildingId);
}
