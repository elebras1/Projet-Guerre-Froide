package com.populaire.projetguerrefroide.dao.json;

import com.dslplatform.json.CompiledJson;
import com.dslplatform.json.JsonAttribute;

@CompiledJson(onUnknown = CompiledJson.Behavior.IGNORE)
public record PositionJson(
    @JsonAttribute(mandatory = true) long x,
    @JsonAttribute(mandatory = true) long y) {
}
