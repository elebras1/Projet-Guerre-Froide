package com.populaire.projetguerrefroide.protocol.event;

import java.lang.invoke.VarHandle;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicLongArray;

public final class EventRing {
    private final int mask;
    private final int[] kinds;
    private final long[] firsts;
    private final long[] seconds;
    private final double[] values;
    private final AtomicLongArray sequences;
    private final AtomicLong head;
    private long tail;
    private long lost;
    private int kind;
    private long first;
    private long second;
    private double value;

    public EventRing(int capacityPowerOfTwo) {
        if (Integer.bitCount(capacityPowerOfTwo) != 1) {
            throw new IllegalArgumentException("Capacity must be a power of two");
        }
        this.mask = capacityPowerOfTwo - 1;
        this.kinds = new int[capacityPowerOfTwo];
        this.firsts = new long[capacityPowerOfTwo];
        this.seconds = new long[capacityPowerOfTwo];
        this.values = new double[capacityPowerOfTwo];
        this.sequences = new AtomicLongArray(capacityPowerOfTwo);
        this.head = new AtomicLong();
    }

    public void publish(EventKind kind, long first, long second, double value) {
        long position = this.head.get();
        int slot = (int) (position & this.mask);
        this.sequences.set(slot, position * 2 + 1);
        this.kinds[slot] = kind.ordinal();
        this.firsts[slot] = first;
        this.seconds[slot] = second;
        this.values[slot] = value;
        this.sequences.set(slot, position * 2 + 2);
        this.head.set(position + 1);
    }

    public boolean poll() {
        long written = this.head.get();
        while (this.tail < written) {
            int capacity = this.mask + 1;
            if (written - this.tail > capacity) {
                this.lost += written - capacity - this.tail;
                this.tail = written - capacity;
            }
            long position = this.tail;
            int slot = (int) (position & this.mask);
            long expected = position * 2 + 2;
            this.tail++;
            if (this.sequences.get(slot) != expected) {
                this.lost++;
                continue;
            }
            int readKind = this.kinds[slot];
            long readFirst = this.firsts[slot];
            long readSecond = this.seconds[slot];
            double readValue = this.values[slot];
            VarHandle.acquireFence();
            if (this.sequences.get(slot) != expected) {
                this.lost++;
                continue;
            }
            this.kind = readKind;
            this.first = readFirst;
            this.second = readSecond;
            this.value = readValue;
            return true;
        }
        return false;
    }

    public EventKind kind() {
        return EventKind.of(this.kind);
    }

    public long first() {
        return this.first;
    }

    public long second() {
        return this.second;
    }

    public double value() {
        return this.value;
    }

    public long lost() {
        return this.lost;
    }
}
