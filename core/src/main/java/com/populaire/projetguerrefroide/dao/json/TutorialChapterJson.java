package com.populaire.projetguerrefroide.dao.json;

import com.dslplatform.json.CompiledJson;
import com.dslplatform.json.JsonAttribute;

@CompiledJson(onUnknown = CompiledJson.Behavior.IGNORE)
public record TutorialChapterJson(
    @JsonAttribute(mandatory = true) String title,
    @JsonAttribute(mandatory = true) String desc,
    @JsonAttribute(mandatory = true) String buttonText) {
}
