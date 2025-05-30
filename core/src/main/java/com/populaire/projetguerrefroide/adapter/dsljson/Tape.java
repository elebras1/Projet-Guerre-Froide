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
        tape = new long[capacity];
    }

    void append(long val, char type) {
        ensureCapacity();
        tape[tapeIdx++] = val | (((long) type) << 56);
    }

    void appendInt64(long val) {
        append(0, INT64);
        ensureCapacity();
        tape[tapeIdx++] = val;
    }

    void appendDouble(double val) {
        append(0, DOUBLE);
        ensureCapacity();
        tape[tapeIdx++] = Double.doubleToRawLongBits(val);
    }

    private void ensureCapacity() {
        if (tapeIdx >= tape.length) {
            long[] newTape = new long[tape.length * 2];
            System.arraycopy(tape, 0, newTape, 0, tape.length);
            tape = newTape;
        }
    }

    char getType(int idx) {
        return (char) (tape[idx] >> 56);
    }

    long getValue(int idx) {
        return tape[idx] & JSON_VALUE_MASK;
    }

    long getInt64Value(int idx) {
        return tape[idx + 1];
    }

    double getDouble(int idx) {
        long bits = tape[idx + 1];
        return Double.longBitsToDouble(bits);
    }

    int getMatchingBraceIndex(int idx) {
        return (int) tape[idx];
    }

    int getScopeCount(int idx) {
        return (int) ((tape[idx] >> 32) & JSON_COUNT_MASK);
    }

    void write(int idx, int endIdx, char type, int count) {
        long typeInfo = ((long) type) << 56;
        long endInfo = endIdx & 0xFFFFFFFFL;
        long countInfo = ((long) count & JSON_COUNT_MASK) << 32;
        tape[idx] = typeInfo | countInfo | endInfo;
    }

    int computeNextIndex(int idx) {
        char t = getType(idx);
        if (t == START_ARRAY || t == START_OBJECT) return getMatchingBraceIndex(idx);
        else if (t == INT64 || t == DOUBLE) return idx + 2;
        else return idx + 1;
    }

    void reset() {
        tapeIdx = 0;
    }
}
