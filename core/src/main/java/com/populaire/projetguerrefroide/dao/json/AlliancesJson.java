package com.populaire.projetguerrefroide.dao.json;

import com.dslplatform.json.CompiledJson;
import com.dslplatform.json.JsonAttribute;

import java.util.List;

@CompiledJson(onUnknown = CompiledJson.Behavior.IGNORE)
public record AlliancesJson(
    @JsonAttribute(mandatory = true) List<AllianceJson> alliances) {
}
