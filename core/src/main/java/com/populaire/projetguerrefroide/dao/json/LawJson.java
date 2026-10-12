package com.populaire.projetguerrefroide.dao.json;

import com.dslplatform.json.CompiledJson;
import com.dslplatform.json.JsonAttribute;

import java.util.Map;

@CompiledJson(onUnknown = CompiledJson.Behavior.IGNORE)
public record LawJson(
    @JsonAttribute(mandatory = true) Map<String, Double> modifiers,
    Map<String, Double> overrides,
    @JsonAttribute(name = "interest_ideologies", mandatory = true) Map<String, Long> interestIdeologies) {
}
