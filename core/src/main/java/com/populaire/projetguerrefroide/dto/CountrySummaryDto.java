package com.populaire.projetguerrefroide.dto;

import java.util.List;

public record CountrySummaryDto(String countryNameId, int population, String government, String portrait, String leaderFullName, String colonizerId, List<String> allies) {
}
