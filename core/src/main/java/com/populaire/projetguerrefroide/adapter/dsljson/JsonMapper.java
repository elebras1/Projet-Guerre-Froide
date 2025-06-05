package com.populaire.projetguerrefroide.adapter.dsljson;

import com.dslplatform.json.DslJson;
import com.dslplatform.json.JsonReader;
import com.dslplatform.json.JsonWriter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class JsonMapper {
    private final DslJson<?> dslJson;
    private final JsonReader<?> reader;
    private final Tape tape;
    private final JsonTreeBuilder builder;

    public JsonMapper() {
        this.dslJson = new DslJson<>();
        this.reader = this.dslJson.newReader();
        this.tape = new Tape(512);
        this.builder = new JsonTreeBuilder(this.reader, this.tape, 2048);
    }

    public JsonValue parse(byte[] buffer) throws IOException {
        this.reader.process(buffer, buffer.length);
        byte first = this.reader.getNextToken();

        this.tape.reset();
        this.builder.resetStringBuffer();
        this.builder.parseValue(first);

        byte[] finalStringBuffer = this.builder.getStringBuffer();

        return new JsonValue(this.tape, 0, finalStringBuffer);
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
}
