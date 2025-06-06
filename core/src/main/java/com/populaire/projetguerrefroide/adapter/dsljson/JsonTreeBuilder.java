package com.populaire.projetguerrefroide.adapter.dsljson;

import com.dslplatform.json.JsonReader;
import com.dslplatform.json.NumberConverter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

class JsonTreeBuilder {
    private final JsonReader<?> reader;
    private final Tape tape;
    private byte[] stringBuffer;
    private int stringPtr = 0;

    JsonTreeBuilder(JsonReader<?> reader, Tape tape, int capacity) {
        this.reader = reader;
        this.tape = tape;
        this.stringBuffer = new byte[capacity];
    }

    protected void parseValue(byte token) throws IOException {
        switch (token) {
            case '{' -> this.parseObject();
            case '[' -> this.parseArray();
            case '"' -> this.appendString(this.reader.readString());
            case 't' -> {
                this.reader.wasTrue();
                this.tape.append(0, Tape.TRUE_VALUE);
            }
            case 'f' -> {
                this.reader.wasFalse();
                this.tape.append(0, Tape.FALSE_VALUE);
            }
            case 'n' -> {
                this.reader.wasNull();
                this.tape.append(0, Tape.NULL_VALUE);
            }
            default -> this.parseNumber();
        }
    }

    protected byte[] getStringBuffer() {
        return this.stringBuffer;
    }

    protected void resetStringBuffer() {
        this.stringPtr = 0;
    }

    private void parseNumber() throws IOException {
        try {
            double doubleValue = NumberConverter.deserializeDouble(this.reader);
            if (doubleValue == (long) doubleValue && !Double.isInfinite(doubleValue)) {
                this.tape.appendInt64((long) doubleValue);
            } else {
                this.tape.appendDouble(doubleValue);
            }
        } catch (Exception e) {
            throw new IOException("Invalid number format", e);
        }
    }

    private void appendString(String s) {
        byte[] str = s.getBytes(StandardCharsets.UTF_8);
        int len = str.length;
        int offset = this.stringPtr;

        this.ensureStringBufferCapacity(len + 4);

        this.stringBuffer[this.stringPtr++] = (byte) (len >> 24);
        this.stringBuffer[this.stringPtr++] = (byte) (len >> 16);
        this.stringBuffer[this.stringPtr++] = (byte) (len >> 8);
        this.stringBuffer[this.stringPtr++] = (byte) len;

        System.arraycopy(str, 0, this.stringBuffer, this.stringPtr, len);
        this.stringPtr += len;

        this.tape.append(offset, Tape.STRING);
    }

    private void ensureStringBufferCapacity(int needed) {
        int required = this.stringPtr + needed;
        if (required > this.stringBuffer.length) {
            int newSize = Math.max(this.stringBuffer.length * 2, required);
            this.stringBuffer = Arrays.copyOf(this.stringBuffer, newSize);
        }
    }

    private void parseObject() throws IOException {
        int start = this.tape.tapeIdx;
        this.tape.append(0, Tape.START_OBJECT);
        int count = 0;

        byte next = this.reader.getNextToken();
        if (next == '}') {
            this.tape.write(start, this.tape.tapeIdx, Tape.START_OBJECT, count);
            return;
        }

        do {
            if (next != '"') {
                throw new IOException("Expected string key in object, got: " + (char) next);
            }

            this.appendString();

            next = this.reader.getNextToken();
            if (next != ':') {
                throw new IOException("Expected ':' after object key, got: " + (char) next);
            }

            next = this.reader.getNextToken();
            this.parseValue(next);
            count++;

            next = this.reader.getNextToken();
            if (next == ',') {
                next = this.reader.getNextToken();
            }

        } while (next != '}');

        this.tape.write(start, this.tape.tapeIdx, Tape.START_OBJECT, count);
    }

    private void appendString() throws IOException {
        int offset = this.stringPtr;

        this.ensureStringBufferCapacity(4);
        int lengthPos = this.stringPtr;
        this.stringPtr += 4;

        int bytesRead = this.readString();

        this.stringBuffer[lengthPos] = (byte) (bytesRead >> 24);
        this.stringBuffer[lengthPos + 1] = (byte) (bytesRead >> 16);
        this.stringBuffer[lengthPos + 2] = (byte) (bytesRead >> 8);
        this.stringBuffer[lengthPos + 3] = (byte) bytesRead;

        this.tape.append(offset, Tape.STRING);
    }

    private int readString() throws IOException {
        int startPos = this.stringPtr;

        while (true) {
            byte b = this.reader.read();

            if (b == '"') {
                if (this.stringPtr == startPos || this.stringBuffer[this.stringPtr - 1] != '\\') {
                    break;
                }
                if (!isEscaped()) {
                    break;
                }
            }

            this.ensureStringBufferCapacity(1);
            this.stringBuffer[this.stringPtr++] = b;
        }

        return this.stringPtr - startPos;
    }

    private boolean isEscaped() {
        int backslashCount = 0;
        int pos = this.stringPtr - 1;

        while (pos >= 0 && this.stringBuffer[pos] == '\\') {
            backslashCount++;
            pos--;
        }

        return (backslashCount % 2) == 1;
    }

    private void parseArray() throws IOException {
        int start = this.tape.tapeIdx;
        this.tape.append(0, Tape.START_ARRAY);
        int count = 0;

        byte next = reader.getNextToken();
        if (next == ']') {
            this.tape.write(start, this.tape.tapeIdx, Tape.START_ARRAY, count);
            return;
        }

        do {
            this.parseValue(next);
            count++;

            next = this.reader.getNextToken();
            if (next == ',') {
                next = this.reader.getNextToken();
            }

        } while (next != ']');

        tape.write(start, this.tape.tapeIdx, Tape.START_ARRAY, count);
    }
}
