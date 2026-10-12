package com.populaire.projetguerrefroide.dao.json;

import com.dslplatform.json.CompiledJson;
import com.dslplatform.json.JsonAttribute;

import java.util.Map;

@CompiledJson(onUnknown = CompiledJson.Behavior.IGNORE)
public record LawGroupJson(
    @JsonAttribute(name = "factor_enactment_days", mandatory = true) long factorEnactmentDays,
    @JsonAttribute(mandatory = true) Map<String, LawJson> laws) {
}
