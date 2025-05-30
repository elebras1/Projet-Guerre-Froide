package com.populaire.projetguerrefroide.adapter.dsljson;

import com.dslplatform.json.JsonReader;
import com.dslplatform.json.NumberConverter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

class JsonTreeBuilder {
    private final JsonReader<?> reader;
    private final Tape tape;

    private byte[] stringBuffer = new byte[1024];
    private int stringPtr = 0;

    JsonTreeBuilder(JsonReader<?> reader, Tape tape) {
        this.reader = reader;
        this.tape = tape;
    }

    void parseValue(byte token) throws IOException {
        switch (token) {
            case '{' -> parseObject();
            case '[' -> parseArray();
            case '"' -> appendString(reader.readString());
            case 't' -> {
                reader.wasTrue();
                tape.append(0, Tape.TRUE_VALUE);
            }
            case 'f' -> {
                reader.wasFalse();
                tape.append(0, Tape.FALSE_VALUE);
            }
            case 'n' -> {
                reader.wasNull();
                tape.append(0, Tape.NULL_VALUE);
            }
            default -> parseNumber();
        }
    }

    private void parseNumber() throws IOException {
        try {
            double doubleValue = NumberConverter.deserializeDouble(reader);
            if (doubleValue == (long) doubleValue && !Double.isInfinite(doubleValue)) {
                tape.appendInt64((long) doubleValue);
            } else {
                tape.appendDouble(doubleValue);
            }
        } catch (Exception e) {
            throw new IOException("Invalid number format", e);
        }
    }

    private void appendString(String s) {
        byte[] str = s.getBytes(StandardCharsets.UTF_8);
        int len = str.length;
        int offset = stringPtr;

        ensureStringBufferCapacity(len + 4);

        stringBuffer[stringPtr++] = (byte) (len >> 24);
        stringBuffer[stringPtr++] = (byte) (len >> 16);
        stringBuffer[stringPtr++] = (byte) (len >> 8);
        stringBuffer[stringPtr++] = (byte) len;

        System.arraycopy(str, 0, stringBuffer, stringPtr, len);
        stringPtr += len;

        tape.append(offset, Tape.STRING);
    }

    private void ensureStringBufferCapacity(int needed) {
        int required = stringPtr + needed;
        if (required > stringBuffer.length) {
            int newSize = Math.max(stringBuffer.length * 2, required);
            stringBuffer = Arrays.copyOf(stringBuffer, newSize);
        }
    }

    private void parseObject() throws IOException {
        int start = tape.tapeIdx;
        tape.append(0, Tape.START_OBJECT);
        int count = 0;

        byte next = reader.getNextToken();
        if (next == '}') {
            tape.write(start, tape.tapeIdx, Tape.START_OBJECT, count);
            return;
        }

        do {
            if (next != '"') {
                throw new IOException("Expected string key in object, got: " + (char) next);
            }
            String key = reader.readString();
            appendString(key);

            next = reader.getNextToken();
            if (next != ':') {
                throw new IOException("Expected ':' after object key, got: " + (char) next);
            }

            next = reader.getNextToken();
            parseValue(next);
            count++;

            next = reader.getNextToken();
            if (next == ',') {
                next = reader.getNextToken();
            }

        } while (next != '}');

        tape.write(start, tape.tapeIdx, Tape.START_OBJECT, count);
    }

    private void parseArray() throws IOException {
        int start = tape.tapeIdx;
        tape.append(0, Tape.START_ARRAY);
        int count = 0;

        byte next = reader.getNextToken();
        if (next == ']') {
            tape.write(start, tape.tapeIdx, Tape.START_ARRAY, count);
            return;
        }

        do {
            parseValue(next);
            count++;

            next = reader.getNextToken();
            if (next == ',') {
                next = reader.getNextToken();
            }

        } while (next != ']');

        tape.write(start, tape.tapeIdx, Tape.START_ARRAY, count);
    }

    byte[] getStringBuffer() {
        return stringBuffer;
    }
}
