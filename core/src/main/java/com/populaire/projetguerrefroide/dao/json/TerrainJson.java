package com.populaire.projetguerrefroide.dao.json;

import com.dslplatform.json.CompiledJson;
import com.dslplatform.json.JsonAttribute;

@CompiledJson(onUnknown = CompiledJson.Behavior.IGNORE)
public record TerrainJson(
    @JsonAttribute(name = "movement_cost", mandatory = true) long movementCost,
    @JsonAttribute(mandatory = true) long humidity,
    @JsonAttribute(mandatory = true) long temperature,
    @JsonAttribute(mandatory = true) long precipitation,
    @JsonAttribute(mandatory = true) int[] color) {
}
