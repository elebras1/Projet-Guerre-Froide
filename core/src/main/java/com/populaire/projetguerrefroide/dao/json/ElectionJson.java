package com.populaire.projetguerrefroide.dao.json;

import com.dslplatform.json.CompiledJson;
import com.dslplatform.json.JsonAttribute;

@CompiledJson(onUnknown = CompiledJson.Behavior.IGNORE)
public record ElectionJson(
    @JsonAttribute(name = "head_of_state") Boolean headOfState,
    @JsonAttribute(name = "head_of_government") Boolean headOfGovernment,
    Long duration) {
}
