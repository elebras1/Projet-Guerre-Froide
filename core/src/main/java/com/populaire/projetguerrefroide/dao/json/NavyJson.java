package com.populaire.projetguerrefroide.dao.json;

import com.dslplatform.json.CompiledJson;
import com.dslplatform.json.JsonAttribute;

import java.util.List;

@CompiledJson(onUnknown = CompiledJson.Behavior.IGNORE)
public record NavyJson(
    @JsonAttribute(name = "name_id", mandatory = true) String nameId,
    @JsonAttribute(mandatory = true) long province,
    @JsonAttribute(mandatory = true) long base,
    @JsonAttribute(mandatory = true) List<UnitInstanceJson> ships) {
}
