package com.populaire.projetguerrefroide.dao.json;

import com.dslplatform.json.CompiledJson;
import com.dslplatform.json.JsonAttribute;

import java.util.List;
import java.util.Map;

@CompiledJson(onUnknown = CompiledJson.Behavior.IGNORE)
public record GovernmentJson(
    @JsonAttribute(name = "associated_ideologies", mandatory = true) List<String> associatedIdeologies,
    @JsonAttribute(mandatory = true) Map<String, List<String>> laws,
    @JsonAttribute(mandatory = true) ElectionJson election) {
}
