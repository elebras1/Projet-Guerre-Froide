package com.populaire.projetguerrefroide.dao.json;

import com.dslplatform.json.CompiledJson;

import java.util.Map;

@CompiledJson(onUnknown = CompiledJson.Behavior.IGNORE)
public record TechnologyEffectsJson(Map<String, Double> modifiers) {
}
