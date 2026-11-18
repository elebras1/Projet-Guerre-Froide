package com.populaire.projetguerrefroide.dao.json;

import com.dslplatform.json.CompiledJson;

import java.util.Map;

@CompiledJson(onUnknown = CompiledJson.Behavior.IGNORE)
public record RgoModesJson(
    Boolean enabled,
    Map<String, RgoModeJson> modes) {
}
