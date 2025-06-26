package com.populaire.projetguerrefroide.economy.good;

import com.github.tommyettinger.ds.FloatList;
import com.github.tommyettinger.ds.IntList;

import java.util.List;

public class MilitaryGoodStore extends GoodStore {
    public MilitaryGoodStore(List<String> names, FloatList costs, IntList colors) {
        super(names, costs, colors);
    }

    @Override
    public String toString() {
        return "MilitaryGood{" +
            "names='" + this.getNames() + '\'' +
            ", costs=" + this.getCosts() +
            ", colors=" + this.getColors() +
            '}';
    }
}
