package com.populaire.projetguerrefroide.util.collection;

import com.github.tommyettinger.ds.ObjectList;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class ObjectPool<T> {
    private final ObjectList<T> items;
    private final Supplier<T> factory;
    private final Consumer<T> reset;
    private int used;

    public ObjectPool(Supplier<T> factory, Consumer<T> reset) {
        this.items = new ObjectList<>();
        this.factory = factory;
        this.reset = reset;
    }

    public T obtain() {
        if (this.used == this.items.size()) {
            this.items.add(this.factory.get());
        }
        return this.items.get(this.used++);
    }

    public void releaseAll() {
        for (int i = 0; i < this.used; i++) {
            this.reset.accept(this.items.get(i));
        }
        this.used = 0;
    }
}
