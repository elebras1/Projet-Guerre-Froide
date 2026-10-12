package com.populaire.projetguerrefroide.dao.json;

import com.dslplatform.json.CompiledJson;
import com.dslplatform.json.JsonAttribute;

import java.util.Map;

@CompiledJson(onUnknown = CompiledJson.Behavior.IGNORE)
public record PopulationTotalJson(
    @JsonAttribute(mandatory = true) long amount,
    @JsonAttribute(mandatory = true) long template,
    @JsonAttribute(mandatory = true) Map<String, Double> populations,
    @JsonAttribute(mandatory = true) Map<String, Double> cultures,
    @JsonAttribute(mandatory = true) Map<String, Double> religions) {
}
