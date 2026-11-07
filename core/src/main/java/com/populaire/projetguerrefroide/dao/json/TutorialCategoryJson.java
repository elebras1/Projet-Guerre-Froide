package com.populaire.projetguerrefroide.dao.json;

import com.dslplatform.json.CompiledJson;
import com.dslplatform.json.JsonAttribute;

import java.util.List;

@CompiledJson(onUnknown = CompiledJson.Behavior.IGNORE)
public record TutorialCategoryJson(
    @JsonAttribute(mandatory = true) String title,
    @JsonAttribute(mandatory = true) String shortTitle,
    @JsonAttribute(mandatory = true) String desc,
    List<TutorialChapterJson> chapters) {
}
