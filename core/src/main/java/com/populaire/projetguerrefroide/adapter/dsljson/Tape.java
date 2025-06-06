package com.populaire.projetguerrefroide.adapter.dsljson;

class Tape {
    protected static final char START_ARRAY = '[';
    protected static final char START_OBJECT = '{';
    protected static final char STRING = '"';
    protected static final char INT64 = 'l';
    protected static final char DOUBLE = 'd';
    protected static final char TRUE_VALUE = 't';
    protected static final char FALSE_VALUE = 'f';
    protected static final char NULL_VALUE = 'n';

    private static final long JSON_VALUE_MASK = 0x00FFFFFFFFFFFFFFL;
    private static final int JSON_COUNT_MASK = 0xFFFFFF;

    private long[] tape;
    protected int tapeIdx;

    Tape(int capacity) {
        this.tape = new long[capacity];
    }

    protected void append(long val, char type) {
        ensureCapacity();
        this.tape[this.tapeIdx++] = val | (((long) type) << 56);
    }

    protected void appendInt64(long val) {
        append(0, INT64);
        ensureCapacity();
        this.tape[this.tapeIdx++] = val;
    }

    protected void appendDouble(double val) {
        append(0, DOUBLE);
        ensureCapacity();
        this.tape[this.tapeIdx++] = Double.doubleToRawLongBits(val);
    }

    private void ensureCapacity() {
        if (this.tapeIdx >= this.tape.length) {
            long[] newTape = new long[this.tape.length * 2];
            System.arraycopy(this.tape, 0, newTape, 0, this.tape.length);
            this.tape = newTape;
        }
    }

    protected char getType(int idx) {
        return (char) (this.tape[idx] >> 56);
    }

    protected long getValue(int idx) {
        return this.tape[idx] & JSON_VALUE_MASK;
    }

    protected long getInt64Value(int idx) {
        return this.tape[idx + 1];
    }

    protected double getDouble(int idx) {
        long bits = this.tape[idx + 1];
        return Double.longBitsToDouble(bits);
    }

    protected int getMatchingBraceIndex(int idx) {
        return (int) this.tape[idx];
    }

    protected int getScopeCount(int idx) {
        return (int) ((this.tape[idx] >> 32) & JSON_COUNT_MASK);
    }

    protected void write(int idx, int endIdx, char type, int count) {
        long typeInfo = ((long) type) << 56;
        long endInfo = endIdx & 0xFFFFFFFFL;
        long countInfo = ((long) count & JSON_COUNT_MASK) << 32;
        this.tape[idx] = typeInfo | countInfo | endInfo;
    }

    protected int computeNextIndex(int idx) {
        char t = getType(idx);
        if (t == START_ARRAY || t == START_OBJECT) return getMatchingBraceIndex(idx);
        else if (t == INT64 || t == DOUBLE) return idx + 2;
        else return idx + 1;
    }

    protected void reset() {
        this.tapeIdx = 0;
    }
}
