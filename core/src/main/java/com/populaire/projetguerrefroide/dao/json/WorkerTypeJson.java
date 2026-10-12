package com.populaire.projetguerrefroide.dao.json;

import com.dslplatform.json.CompiledJson;
import com.dslplatform.json.JsonAttribute;

@CompiledJson(onUnknown = CompiledJson.Behavior.IGNORE)
public record WorkerTypeJson(
    @JsonAttribute(mandatory = true) String poptype,
    @JsonAttribute(mandatory = true) double ratio,
    @JsonAttribute(name = "effect_multiplier", mandatory = true) double effectMultiplier) {
}
