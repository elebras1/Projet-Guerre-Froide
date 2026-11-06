package com.populaire.projetguerrefroide.simulation.request;

import com.populaire.projetguerrefroide.protocol.SimulationHeader;
import com.populaire.projetguerrefroide.protocol.catalog.Catalog;
import com.populaire.projetguerrefroide.protocol.request.Request;
import com.populaire.projetguerrefroide.protocol.request.RequestBoard;
import com.populaire.projetguerrefroide.protocol.response.*;
import com.populaire.projetguerrefroide.simulation.SimulationContext;
import com.populaire.projetguerrefroide.simulation.SimulationState;

public final class RequestService {
    private final CountryOverviewHandler countryOverview;
    private final CountrySummaryHandler countrySummary;
    private final ProvinceDetailsHandler provinceDetails;
    private final ProvinceHoverHandler provinceHover;
    private final RegionsBuildingsHandler regionsBuildings;
    private final BuildingDetailsHandler buildingDetails;
    private final PoliticalMapHandler politicalMap;
    private final MapModeColorsHandler mapModeColors;
    private final MapBuildingsHandler mapBuildings;

    public RequestService(SimulationContext simulationContext, SimulationState state, SimulationHeader header) {
        this.countryOverview = new CountryOverviewHandler(simulationContext, state, header);
        this.countrySummary = new CountrySummaryHandler(simulationContext, state, header);
        this.provinceDetails = new ProvinceDetailsHandler(simulationContext, state, header);
        this.provinceHover = new ProvinceHoverHandler(simulationContext, state, header);
        this.regionsBuildings = new RegionsBuildingsHandler(simulationContext, state, header);
        this.buildingDetails = new BuildingDetailsHandler(simulationContext, state, header);
        this.politicalMap = new PoliticalMapHandler(simulationContext, state, header);
        this.mapModeColors = new MapModeColorsHandler(simulationContext, state, header);
        this.mapBuildings = new MapBuildingsHandler(simulationContext, state, header);
    }

    public void setCatalog(Catalog catalog) {
        this.countryOverview.setCatalog(catalog);
        this.countrySummary.setCatalog(catalog);
        this.provinceDetails.setCatalog(catalog);
        this.provinceHover.setCatalog(catalog);
        this.regionsBuildings.setCatalog(catalog);
        this.buildingDetails.setCatalog(catalog);
        this.politicalMap.setCatalog(catalog);
        this.mapModeColors.setCatalog(catalog);
        this.mapBuildings.setCatalog(catalog);
    }

    public int serve(RequestBoard board, long publishedBatch) {
        int count = board.count();
        int served = 0;
        for (int i = 0; i < count; i++) {
            Request<?> request = board.get(i);
            if (request.isPendingUpTo(publishedBatch)) {
                this.serve(request);
                served++;
            }
        }
        return served;
    }

    @SuppressWarnings("unchecked")
    private boolean serve(Request<?> request) {
        return switch (request.kind()) {
            case COUNTRY_OVERVIEW -> this.countryOverview.serve((Request<CountryOverview>) request);
            case COUNTRY_SUMMARY -> this.countrySummary.serve((Request<CountrySummary>) request);
            case PROVINCE_DETAILS -> this.provinceDetails.serve((Request<ProvinceDetails>) request);
            case PROVINCE_HOVER -> this.provinceHover.serve((Request<ProvinceHover>) request);
            case REGIONS_BUILDINGS -> this.regionsBuildings.serve((Request<RegionsBuildings>) request);
            case BUILDING_DETAILS -> this.buildingDetails.serve((Request<BuildingDetails>) request);
            case POLITICAL_MAP -> this.politicalMap.serve((Request<PoliticalMap>) request);
            case MAP_MODE_COLORS -> this.mapModeColors.serve((Request<MapModeColors>) request);
            case MAP_BUILDINGS -> this.mapBuildings.serve((Request<MapBuildings>) request);
        };
    }
}
