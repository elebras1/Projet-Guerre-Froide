package com.populaire.projetguerrefroide.dao.json;

import com.dslplatform.json.CompiledJson;
import com.dslplatform.json.JsonAttribute;

@CompiledJson(onUnknown = CompiledJson.Behavior.IGNORE)
public record IdeologyJson(
    @JsonAttribute(mandatory = true) int[] color,
    @JsonAttribute(name = "faction_drifting_speed", mandatory = true) long factionDriftingSpeed) {
}
