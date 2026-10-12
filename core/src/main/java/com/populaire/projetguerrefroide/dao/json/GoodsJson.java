package com.populaire.projetguerrefroide.dao.json;

import com.dslplatform.json.CompiledJson;
import com.dslplatform.json.JsonAttribute;

import java.util.Map;

@CompiledJson(onUnknown = CompiledJson.Behavior.IGNORE)
public record GoodsJson(
    @JsonAttribute(name = "resource_goods", mandatory = true) Map<String, GoodJson> resourceGoods,
    @JsonAttribute(name = "advanced_goods", mandatory = true) Map<String, GoodJson> advancedGoods,
    @JsonAttribute(name = "military_goods", mandatory = true) Map<String, GoodJson> militaryGoods) {
}
