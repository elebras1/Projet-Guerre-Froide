package com.populaire.projetguerrefroide.dao.json;

import com.dslplatform.json.CompiledJson;
import com.dslplatform.json.JsonAttribute;

import java.util.List;

@CompiledJson(onUnknown = CompiledJson.Behavior.IGNORE)
public record TechnologyJson(
    @JsonAttribute(mandatory = true) String tree,
    @JsonAttribute(mandatory = true) int era,
    @JsonAttribute(mandatory = true) int year,
    @JsonAttribute(mandatory = true) int time,
    @JsonAttribute(mandatory = true) List<String> requires,
    @JsonAttribute(mandatory = true) TechnologyEffectsJson effects) {
}
