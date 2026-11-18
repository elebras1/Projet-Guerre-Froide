package com.populaire.projetguerrefroide.dao.json;

import com.dslplatform.json.CompiledJson;
import com.dslplatform.json.JsonAttribute;

import java.util.List;
import java.util.Map;

@CompiledJson(onUnknown = CompiledJson.Behavior.IGNORE)
public record RgoModeJson(
    @JsonAttribute(mandatory = true) String technology,
    @JsonAttribute(mandatory = true) List<String> goods,
    Map<String, Double> inputs,
    @JsonAttribute(mandatory = true) Double output) {
}
