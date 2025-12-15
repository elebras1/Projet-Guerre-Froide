package com.populaire.projetguerrefroide.entity;

import java.time.LocalDate;
import java.util.List;

public record Bookmark(String iconNameFile, String nameId, String descriptionId, LocalDate date, List<String> countriesId) {
}
