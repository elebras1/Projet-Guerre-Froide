package com.populaire.projetguerrefroide.dao.json;

import com.dslplatform.json.CompiledJson;
import com.dslplatform.json.JsonAttribute;

@CompiledJson(onUnknown = CompiledJson.Behavior.IGNORE)
public record MinisterJson(
    @JsonAttribute(mandatory = true) String name,
    @JsonAttribute(mandatory = true) String ideology,
    @JsonAttribute(mandatory = true) double loyalty,
    @JsonAttribute(mandatory = true) String picture,
    @JsonAttribute(mandatory = true) String type,
    @JsonAttribute(name = "start_date", mandatory = true) String startDate,
    @JsonAttribute(name = "death_date", mandatory = true) String deathDate) {
}
