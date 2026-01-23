package com.populaire.projetguerrefroide.pojo;

public class MutableInt {
    private int value;

    public MutableInt(int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public void increment() {
        this.value++;
    }

    public void increment(int value) {
        this.value += value;
    }

    public void decrement() {
        this.value--;
    }

    public void decrement(int value) {
        this.value -= value;
    }

    @Override
    public String toString() {
        return "MutableInt{" +
                "value=" + this.value +
                '}';
    }
}
