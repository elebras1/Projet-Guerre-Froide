package com.populaire.projetguerrefroide.loader;

import com.github.tommyettinger.ds.IntObjectMap;
import com.github.tommyettinger.ds.LongIntMap;
import com.github.tommyettinger.ds.LongIntOrderedMap;

public record ProvinceBuildings(IntObjectMap<LongIntOrderedMap> buildings, IntObjectMap<LongIntMap> modes) {
}
