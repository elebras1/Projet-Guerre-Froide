package com.populaire.projetguerrefroide.dao.json;

import com.dslplatform.json.CompiledJson;
import com.dslplatform.json.JsonAttribute;

import java.util.List;

@CompiledJson(onUnknown = CompiledJson.Behavior.IGNORE)
public record ProductionTypeJson(
    @JsonAttribute(mandatory = true) long workforce,
    @JsonAttribute(mandatory = true) List<String> workers) {
}
