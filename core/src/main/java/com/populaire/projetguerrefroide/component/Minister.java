package com.populaire.projetguerrefroide.component;

import com.github.elebras1.flecs.annotation.Component;
import com.github.elebras1.flecs.annotation.FixedString;

import static com.populaire.projetguerrefroide.util.Constants.MAX_IMAGE_NAME_LENGTH;

@Component
public record Minister(String name, @FixedString(length = MAX_IMAGE_NAME_LENGTH) String imageFileName, float loyalty, int startDate, int deathDate, long countryId, long ideologyId, long typeId) {
}
