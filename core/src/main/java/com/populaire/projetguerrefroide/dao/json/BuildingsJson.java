package com.populaire.projetguerrefroide.dao.json;

import com.dslplatform.json.CompiledJson;
import com.dslplatform.json.JsonAttribute;

import java.util.Map;

@CompiledJson(onUnknown = CompiledJson.Behavior.IGNORE)
public record BuildingsJson(
    @JsonAttribute(name = "economy_building", mandatory = true) Map<String, BuildingJson> economyBuilding,
    @JsonAttribute(name = "special_building", mandatory = true) Map<String, BuildingJson> specialBuilding,
    @JsonAttribute(name = "development_building", mandatory = true) Map<String, BuildingJson> developmentBuilding) {
}
