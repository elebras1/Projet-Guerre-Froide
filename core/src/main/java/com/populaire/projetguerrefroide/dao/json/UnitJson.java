package com.populaire.projetguerrefroide.dao.json;

import com.dslplatform.json.CompiledJson;
import com.dslplatform.json.JsonAttribute;

import java.util.Map;

@CompiledJson(onUnknown = CompiledJson.Behavior.IGNORE)
public record UnitJson(
    @JsonAttribute(mandatory = true) String type,
    @JsonAttribute(name = "unit_type", mandatory = true) String unitType,
    @JsonAttribute(mandatory = true) double attack,
    @JsonAttribute(mandatory = true) double defence,
    @JsonAttribute(mandatory = true) double priority,
    @JsonAttribute(name = "default_organisation", mandatory = true) double defaultOrganisation,
    @JsonAttribute(name = "supply_consumption", mandatory = true) double supplyConsumption,
    @JsonAttribute(name = "weighted_value", mandatory = true) double weightedValue,
    @JsonAttribute(name = "build_time", mandatory = true) double buildTime,
    @JsonAttribute(name = "maximum_speed", mandatory = true) double maximumSpeed,
    @JsonAttribute(name = "max_strength", mandatory = true) double maxStrength,
    @JsonAttribute(name = "build_cost", mandatory = true) Map<String, Double> buildCost,
    @JsonAttribute(name = "supply_cost", mandatory = true) Map<String, Double> supplyCost) {
}
