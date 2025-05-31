package com.populaire.projetguerrefroide.adapter.dsljson;

import com.dslplatform.json.DslJson;
import com.dslplatform.json.JsonReader;
import com.dslplatform.json.JsonWriter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class JsonMapper {
    private final DslJson<?> dslJson = new DslJson<>();
    private final JsonReader<?> reader = dslJson.newReader();
    private final Tape tape = new Tape(256);

    public JsonValue parse(byte[] buffer) throws IOException {
        reader.process(buffer, buffer.length);
        byte first = reader.getNextToken();

        tape.reset();
        JsonTreeBuilder builder = new JsonTreeBuilder(reader, tape);
        builder.parseValue(first);

        byte[] finalStringBuffer = builder.getStringBuffer();

        return new JsonValue(tape, 0, finalStringBuffer, buffer);
    }

    public void writeValue(Object object, File file) throws IOException {
        JsonWriter writer = dslJson.newWriter();
        dslJson.serialize(writer, object);

        try (FileOutputStream fileOutputStream = new FileOutputStream(file)) {
            writer.toStream(fileOutputStream);
        }
    }
}
