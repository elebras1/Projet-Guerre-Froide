package com.populaire.projetguerrefroide.dao.json;

import com.dslplatform.json.CompiledJson;
import com.dslplatform.json.JsonAttribute;

import java.util.List;

@CompiledJson(onUnknown = CompiledJson.Behavior.IGNORE)
public record BookmarkJson(
    @JsonAttribute(mandatory = true) String icon,
    @JsonAttribute(mandatory = true) String name,
    @JsonAttribute(mandatory = true) String desc,
    @JsonAttribute(mandatory = true) String date,
    List<String> country) {
}
