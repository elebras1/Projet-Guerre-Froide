package com.populaire.projetguerrefroide.dao.json;

import com.dslplatform.json.CompiledJson;
import com.dslplatform.json.JsonAttribute;

@CompiledJson(onUnknown = CompiledJson.Behavior.IGNORE)
public record GoodJson(
    @JsonAttribute(mandatory = true) double cost,
    Double value,
    @JsonAttribute(name = "stock_decay") Double stockDecay,
    @JsonAttribute(mandatory = true) int[] color) {
}
