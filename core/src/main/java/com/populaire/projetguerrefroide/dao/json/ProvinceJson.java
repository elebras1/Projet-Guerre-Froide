package com.populaire.projetguerrefroide.dao.json;

import com.dslplatform.json.CompiledJson;
import com.dslplatform.json.JsonAttribute;

import java.util.List;

@CompiledJson(onUnknown = CompiledJson.Behavior.IGNORE)
public record ProvinceJson(
    @JsonAttribute(name = "add_core", mandatory = true) List<String> addCore,
    @JsonAttribute(mandatory = true) String owner,
    @JsonAttribute(mandatory = true) String controller,
    @JsonAttribute(mandatory = true) String terrain,
    @JsonAttribute(name = "population_total", mandatory = true) PopulationTotalJson populationTotal,
    @JsonAttribute(name = "economy_buildings") List<ProvinceBuildingJson> economyBuildings,
    String good,
    List<ProvinceBuildingJson> buildings) {
}
