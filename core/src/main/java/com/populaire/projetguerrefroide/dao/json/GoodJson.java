package com.populaire.projetguerrefroide.dao.json;

import com.dslplatform.json.CompiledJson;
import com.dslplatform.json.JsonAttribute;

@CompiledJson(onUnknown = CompiledJson.Behavior.IGNORE)
public record GoodJson(
    @JsonAttribute(mandatory = true) double cost,
    Double value,
    @JsonAttribute(name = "stock_decay") Double stockDecay,
    Boolean tradable,
    @JsonAttribute(name = "tariff_category", mandatory = true) String tariffCategory,
    @JsonAttribute(mandatory = true) int[] color) {
}
