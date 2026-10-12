package com.populaire.projetguerrefroide.dao.json;

import com.dslplatform.json.CompiledJson;
import com.dslplatform.json.JsonAttribute;

@CompiledJson(onUnknown = CompiledJson.Behavior.IGNORE)
public record LeaderJson(
    @JsonAttribute(mandatory = true) String name,
    @JsonAttribute(mandatory = true) long skill,
    @JsonAttribute(name = "force_type", mandatory = true) String forceType,
    @JsonAttribute(mandatory = true) String trait) {
}
