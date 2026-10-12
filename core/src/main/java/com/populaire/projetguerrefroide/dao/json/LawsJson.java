package com.populaire.projetguerrefroide.dao.json;

import com.dslplatform.json.CompiledJson;
import com.dslplatform.json.JsonAttribute;

import java.util.Map;

@CompiledJson(onUnknown = CompiledJson.Behavior.IGNORE)
public record LawsJson(
    @JsonAttribute(name = "base_enactment_days", mandatory = true) long baseEnactmentDays,
    @JsonAttribute(mandatory = true) Map<String, LawGroupJson> groups) {
}
