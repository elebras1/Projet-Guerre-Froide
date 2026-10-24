package com.populaire.projetguerrefroide.dao.json;

import com.dslplatform.json.CompiledJson;
import com.dslplatform.json.JsonAttribute;

import java.util.Map;

@CompiledJson(onUnknown = CompiledJson.Behavior.IGNORE)
public record BuildingJson(
    @JsonAttribute(mandatory = true) long time,
    @JsonAttribute(name = "base_type") String baseType,
    @JsonAttribute(name = "max_level") Long maxLevel,
    Boolean onmap,
    @JsonAttribute(name = "goods_cost", mandatory = true) Map<String, Double> goodsCost,
    @JsonAttribute(name = "maintenance_goods") Map<String, Double> maintenanceGoods,
    @JsonAttribute(name = "input_goods") Map<String, Double> inputGoods,
    @JsonAttribute(name = "advanced_input_goods") Map<String, Double> advancedInputGoods,
    @JsonAttribute(name = "output_goods") Map<String, Double> outputGoods,
    Map<String, Double> modifier) {
}
