package com.populaire.projetguerrefroide.adapter.dsljson;

import com.dslplatform.json.DslJson;
import com.dslplatform.json.JsonReader;
import com.dslplatform.json.JsonWriter;

import java.io.*;

public class JsonMapper {
    private final DslJson<?> dslJson;
    private final JsonReader<?> reader;
    private final Tape tape;
    private final JsonTreeBuilder builder;
    private byte[] buffer;

    public JsonMapper() {
        this.dslJson = new DslJson<>();
        this.reader = this.dslJson.newReader();
        this.tape = new Tape(512);
        this.builder = new JsonTreeBuilder(this.reader, this.tape, 2048);
        this.buffer = new byte[8192];
    }

    public JsonValue parse(BufferedInputStream inputStream, int length) throws IOException {
        try (inputStream) {
            this.ensureCapacity(length);

            int offset = 0;
            while (offset < length) {
                int bytesRead = inputStream.read(this.buffer, offset, length - offset);
                if (bytesRead == -1) {
                    throw new EOFException("Expected " + length + " bytes, but got " + offset);
                }
                offset += bytesRead;
            }

            this.reader.process(this.buffer, length);
            return this.getJsonValue();
        }
    }


    public JsonValue parse(byte[] buffer) throws IOException {
        if (buffer == null || buffer.length == 0) {
            throw new IllegalArgumentException("Buffer cannot be null or empty");
        }
        this.buffer = buffer;
        this.reader.process(buffer, buffer.length);
        return this.getJsonValue();
    }

    public <T> T readValue(byte[] buffer, Class<T> type) throws IOException {
        return this.dslJson.deserialize(type, buffer, buffer.length);
    }

    public void writeValue(Object object, File file) throws IOException {
        JsonWriter writer = this.dslJson.newWriter();
        this.dslJson.serialize(writer, object);

        try (FileOutputStream fileOutputStream = new FileOutputStream(file)) {
            writer.toStream(fileOutputStream);
        }
    }

    private JsonValue getJsonValue() throws IOException {
        byte first = this.reader.getNextToken();

        this.tape.reset();
        this.builder.resetStringBuffer();
        this.builder.parseValue(first);

        byte[] finalStringBuffer = this.builder.getStringBuffer();

        return new JsonValue(this.tape, 0, finalStringBuffer);
    }

    private void ensureCapacity(int minCapacity) {
        if (minCapacity > this.buffer.length) {
            int newSize = Math.max(this.buffer.length * 2, minCapacity);
            byte[] newBuffer = new byte[newSize];
            System.arraycopy(this.buffer, 0, newBuffer, 0, this.buffer.length);
            this.buffer = newBuffer;
        }
    }
}
