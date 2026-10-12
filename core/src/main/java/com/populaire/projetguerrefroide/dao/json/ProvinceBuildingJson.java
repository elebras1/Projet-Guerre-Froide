package com.populaire.projetguerrefroide.dao.json;

import com.dslplatform.json.CompiledJson;
import com.dslplatform.json.JsonAttribute;

@CompiledJson(onUnknown = CompiledJson.Behavior.IGNORE)
public record ProvinceBuildingJson(
    @JsonAttribute(mandatory = true) String name,
    @JsonAttribute(mandatory = true) long size,
    @JsonAttribute(name = "input_mode") String inputMode) {
}
