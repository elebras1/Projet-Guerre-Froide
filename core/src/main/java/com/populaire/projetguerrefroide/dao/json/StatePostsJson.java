package com.populaire.projetguerrefroide.dao.json;

import com.dslplatform.json.CompiledJson;
import com.dslplatform.json.JsonAttribute;

@CompiledJson(onUnknown = CompiledJson.Behavior.IGNORE)
public record StatePostsJson(
    @JsonAttribute(name = "management_ratio", mandatory = true) double managementRatio,
    @JsonAttribute(name = "administration_ratio", mandatory = true) double administrationRatio,
    @JsonAttribute(name = "tax_collection_floor", mandatory = true) double taxCollectionFloor,
    @JsonAttribute(name = "management_output_bonus", mandatory = true) double managementOutputBonus) {
}
