package com.populaire.projetguerrefroide.dao.json;

import com.dslplatform.json.CompiledJson;
import com.dslplatform.json.JsonAttribute;

@CompiledJson(onUnknown = CompiledJson.Behavior.IGNORE)
public record UnitInstanceJson(
    @JsonAttribute(mandatory = true) String type,
    @JsonAttribute(mandatory = true) double strength,
    @JsonAttribute(mandatory = true) double experience,
    @JsonAttribute(name = "name_id", mandatory = true) String nameId) {
}
