package com.populaire.projetguerrefroide.configuration;

import java.util.List;

public record TutorialCategory(String titleId, String shortId, String descriptionId, List<TutorialChapter> chapters) {
}
