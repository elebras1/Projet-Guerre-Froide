package com.populaire.projetguerrefroide.loader;

import com.github.tommyettinger.ds.IntObjectMap;
import com.github.tommyettinger.ds.LongIntMap;

public record ProvinceBuildings(IntObjectMap<LongIntMap> buildings, IntObjectMap<LongIntMap> modes) {
}
