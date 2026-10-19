package com.populaire.projetguerrefroide.dao.json;

import com.dslplatform.json.CompiledJson;
import com.dslplatform.json.JsonAttribute;

import java.util.Map;

@CompiledJson(onUnknown = CompiledJson.Behavior.IGNORE)
public record PopulationTypeJson(
    @JsonAttribute(mandatory = true) int[] color,
    @JsonAttribute(name = "life_needs", mandatory = true) Map<String, Double> lifeNeeds,
    @JsonAttribute(name = "everyday_needs", mandatory = true) Map<String, Double> everydayNeeds,
    @JsonAttribute(name = "luxury_needs", mandatory = true) Map<String, Double> luxuryNeeds,
    @JsonAttribute(mandatory = true) String strata,
    @JsonAttribute(name = "income_type", mandatory = true) String incomeType,
    @JsonAttribute(name = "promote_to") Map<String, Double> promoteTo,
    @JsonAttribute(name = "demote_to") Map<String, Double> demoteTo,
    @JsonAttribute(name = "state_posts") StatePostsJson statePosts) {
}
