package com.populaire.projetguerrefroide.dao.json;

import com.dslplatform.json.CompiledJson;
import com.dslplatform.json.JsonAttribute;

import java.util.List;
import java.util.Map;

@CompiledJson(onUnknown = CompiledJson.Behavior.IGNORE)
public record TechnologyEffectsJson(
    Map<String, Double> modifiers,
    @JsonAttribute(name = "rgo_productivity") Map<String, Double> rgoProductivity,
    @JsonAttribute(name = "factory_productivity") Map<String, Double> factoryProductivity,
    @JsonAttribute(name = "production_methods") List<String> productionMethods) {
}
