package com.populaire.projetguerrefroide.dao.json;

import com.dslplatform.json.CompiledJson;

import java.util.Map;

/** RGO production modes (annex 12 of the economy plan, decisions 188 and 191); {@code enabled} is the game rule. */
@CompiledJson(onUnknown = CompiledJson.Behavior.IGNORE)
public record RgoModesJson(
    Boolean enabled,
    Map<String, RgoModeJson> modes) {
}
