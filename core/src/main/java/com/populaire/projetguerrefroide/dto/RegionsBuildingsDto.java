package com.populaire.projetguerrefroide.dto;

import com.github.tommyettinger.ds.ByteList;
import com.github.tommyettinger.ds.FloatList;
import com.github.tommyettinger.ds.IntList;
import com.github.tommyettinger.ds.ObjectIntMap;

import java.util.List;
import java.util.Set;

public record RegionsBuildingsDto(Set<String> regionIds, IntList regionInternalIds, ObjectIntMap<String> regionIdLookup, IntList buildingIds, IntList buildingValues, IntList buildingStarts, IntList buildingCounts, List<String> buildingNames, ByteList buildingTypes, ByteList buildingMaxLevels, ByteList developpementIndexValues, IntList populationAmounts, IntList buildingWorkersAmount, ByteList buildingWorkersRatio, FloatList buildingProductionValues) {
}
