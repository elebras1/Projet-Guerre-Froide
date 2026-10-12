package com.populaire.projetguerrefroide.dao.json;

import com.dslplatform.json.CompiledJson;
import com.dslplatform.json.JsonAttribute;

import java.util.Map;

@CompiledJson(onUnknown = CompiledJson.Behavior.IGNORE)
public record NationalIdeasJson(
    @JsonAttribute(mandatory = true) Map<String, ColoredJson> cultures,
    @JsonAttribute(mandatory = true) Map<String, ColoredJson> religions,
    @JsonAttribute(name = "national_identity", mandatory = true) Map<String, Map<String, Double>> nationalIdentity,
    @JsonAttribute(name = "national_attitude", mandatory = true) Map<String, Map<String, Double>> nationalAttitude) {
}
