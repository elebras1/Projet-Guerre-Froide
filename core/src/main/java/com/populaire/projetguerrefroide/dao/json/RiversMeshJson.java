package com.populaire.projetguerrefroide.dao.json;

import com.dslplatform.json.CompiledJson;
import com.dslplatform.json.JsonAttribute;

@CompiledJson(onUnknown = CompiledJson.Behavior.IGNORE)
public record RiversMeshJson(
    @JsonAttribute(mandatory = true) float[] vertices,
    @JsonAttribute(mandatory = true) int[] starts,
    @JsonAttribute(mandatory = true) int[] counts) {
}
