package com.populaire.projetguerrefroide.dao.json;

import com.dslplatform.json.CompiledJson;
import com.dslplatform.json.JsonAttribute;

@CompiledJson(onUnknown = CompiledJson.Behavior.IGNORE)
public record RelationJson(
    @JsonAttribute(mandatory = true) String country1,
    @JsonAttribute(mandatory = true) String country2,
    @JsonAttribute(mandatory = true) long value) {
}
