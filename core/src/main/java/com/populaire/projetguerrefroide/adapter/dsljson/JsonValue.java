package com.populaire.projetguerrefroide.adapter.dsljson;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;

import static com.populaire.projetguerrefroide.adapter.dsljson.Tape.*;
import static java.nio.charset.StandardCharsets.UTF_8;

public class JsonValue {

    private final Tape tape;
    private final int tapeIdx;
    private final byte[] stringBuffer;

    JsonValue(Tape tape, int tapeIdx, byte[] stringBuffer) {
        this.tape = tape;
        this.tapeIdx = tapeIdx;
        this.stringBuffer = stringBuffer;
    }

    public boolean isArray() {
        return this.tape.getType(this.tapeIdx) == START_ARRAY;
    }

    public boolean isObject() {
        return this.tape.getType(this.tapeIdx) == START_OBJECT;
    }

    public boolean isLong() {
        return this.tape.getType(this.tapeIdx) == INT64;
    }

    public boolean isDouble() {
        return this.tape.getType(this.tapeIdx) == DOUBLE;
    }

    public boolean isBoolean() {
        char type = this.tape.getType(this.tapeIdx);
        return type == TRUE_VALUE || type == FALSE_VALUE;
    }

    public boolean isNull() {
        return this.tape.getType(this.tapeIdx) == NULL_VALUE;
    }

    public boolean isString() {
        return this.tape.getType(this.tapeIdx) == STRING;
    }

    public Iterator<JsonValue> arrayIterator() {
        return new ArrayIterator(this.tapeIdx);
    }

    public Iterator<Map.Entry<String, JsonValue>> objectIterator() {
        return new ObjectIterator(this.tapeIdx);
    }

    public long asLong() {
        return tape.getInt64Value(tapeIdx);
    }

    public double asDouble() {
        if (this.tape.getType(this.tapeIdx) == INT64) {
            return this.tape.getInt64Value(this.tapeIdx);
        }
        return this.tape.getDouble(this.tapeIdx);
    }

    public boolean asBoolean() {
        return this.tape.getType(this.tapeIdx) == TRUE_VALUE;
    }

    public char asChar() {
        if (this.tape.getType(this.tapeIdx) == STRING) {
            int stringBufferIdx = (int) this.tape.getValue(this.tapeIdx);
            int len = IntegerUtils.toInt(this.stringBuffer, stringBufferIdx);
            if (len == 1) {
                return (char) this.stringBuffer[stringBufferIdx + Integer.BYTES];
            }
        }

        return 0;
    }

    public String asString() {
        return getString(this.tapeIdx);
    }

    private String getString(int tapeIdx) {
        int stringBufferIdx = (int) this.tape.getValue(tapeIdx);
        int len = IntegerUtils.toInt(this.stringBuffer, stringBufferIdx);
        return new String(this.stringBuffer, stringBufferIdx + Integer.BYTES, len, UTF_8);
    }

    public JsonValue get(String name) {
        byte[] bytes = name.getBytes(UTF_8);
        int idx = this.tapeIdx + 1;
        int endIdx = this.tape.getMatchingBraceIndex(this.tapeIdx) - 1;
        while (idx < endIdx) {
            int stringBufferIdx = (int) this.tape.getValue(idx);
            int len = IntegerUtils.toInt(this.stringBuffer, stringBufferIdx);
            int valIdx = this.tape.computeNextIndex(idx);
            idx = this.tape.computeNextIndex(valIdx);
            int from = stringBufferIdx + Integer.BYTES;
            int to = from + len;
            if (Arrays.compare(bytes, 0, bytes.length, this.stringBuffer, from, to) == 0) {
                return new JsonValue(this.tape, valIdx, this.stringBuffer);
            }
        }
        return null;
    }

    public JsonValue get(int index) {
        int idx = this.tapeIdx + 1;
        int endIdx = this.tape.getMatchingBraceIndex(this.tapeIdx);

        for (int i = 0; i < index; i++) {
            if (idx >= endIdx) return null;
            idx = this.tape.computeNextIndex(idx);
        }

        if (idx < endIdx) {
            return new JsonValue(this.tape, idx, this.stringBuffer);
        }

        return null;
    }

    public int getSize() {
        return this.tape.getScopeCount(this.tapeIdx);
    }

    @Override
    public String toString() {
        switch (this.tape.getType(this.tapeIdx)) {
            case INT64 -> {
                return String.valueOf(asLong());
            }
            case DOUBLE -> {
                return String.valueOf(asDouble());
            }
            case TRUE_VALUE, FALSE_VALUE -> {
                return String.valueOf(asBoolean());
            }
            case STRING -> {
                return asString();
            }
            case NULL_VALUE -> {
                return "null";
            }
            case START_OBJECT -> {
                return "<object>";
            }
            case START_ARRAY -> {
                return "<array>";
            }
            default -> {
                return "unknown";
            }
        }
    }

    private class ArrayIterator implements Iterator<JsonValue> {
        private final int endIdx;
        private int idx;

        ArrayIterator(int startIdx) {
            this.idx = startIdx + 1;
            this.endIdx = tape.getMatchingBraceIndex(startIdx);
        }

        @Override
        public boolean hasNext() {
            return this.idx < this.endIdx;
        }

        @Override
        public JsonValue next() {
            if (hasNext()) {
                JsonValue value = new JsonValue(tape, this.idx, stringBuffer);
                this.idx = tape.computeNextIndex(this.idx);
                return value;
            }
            throw new NoSuchElementException("No more elements");
        }
    }

    private class ObjectIterator implements Iterator<Map.Entry<String, JsonValue>> {

        private final int endIdx;
        private int idx;

        ObjectIterator(int startIdx) {
            this.idx = startIdx + 1;
            this.endIdx = tape.getMatchingBraceIndex(startIdx) - 1;
        }

        @Override
        public boolean hasNext() {
            return this.idx < this.endIdx;
        }

        @Override
        public Map.Entry<String, JsonValue> next() {
            String key = getString(this.idx);
            this.idx = tape.computeNextIndex(this.idx);
            JsonValue value = new JsonValue(tape, this.idx, stringBuffer);
            this.idx = tape.computeNextIndex(this.idx);
            return new ObjectField(key, value);
        }
    }

    private static class ObjectField implements Map.Entry<String, JsonValue> {

        private final String key;
        private final JsonValue value;

        ObjectField(String key, JsonValue value) {
            this.key = key;
            this.value = value;
        }

        @Override
        public String getKey() {
            return this.key;
        }

        @Override
        public JsonValue getValue() {
            return this.value;
        }

        @Override
        public JsonValue setValue(JsonValue value) {
            throw new UnsupportedOperationException("Object fields are immutable");
        }
    }
}
