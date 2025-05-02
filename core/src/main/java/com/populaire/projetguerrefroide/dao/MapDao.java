package com.populaire.projetguerrefroide.dao;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.github.tommyettinger.ds.FloatList;
import com.github.tommyettinger.ds.IntList;
import com.populaire.projetguerrefroide.entity.RawMeshMultiDraw;

import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;

public class MapDao {
    private final JsonFactory factory = new JsonFactory();
    private final String riversMeshFile = "map/rivers_mesh.json";

    public RawMeshMultiDraw readRiversMeshJson() {
        FileHandle fh = Gdx.files.internal(this.riversMeshFile);
        try (InputStream is = fh.read(); JsonParser parser = this.factory.createParser(is)) {

            float[] vertices = null;
            IntBuffer startsBuffer = null;
            IntBuffer countsBuffer = null;

            while (parser.nextToken() != null) {
                if (parser.currentToken() == JsonToken.FIELD_NAME) {
                    String name = parser.currentName();
                    parser.nextToken();

                    switch (name) {
                        case "vertices" -> vertices = this.readFloatArray(parser);
                        case "starts" -> startsBuffer = this.readIntBuffer(parser);
                        case "counts" -> countsBuffer = this.readIntBuffer(parser);
                        default -> parser.skipChildren();
                    }
                }
            }

            if (vertices == null)
                throw new RuntimeException("Fichier JSON incomplet");

            return new RawMeshMultiDraw(vertices, startsBuffer, countsBuffer);

        } catch (Exception e) {
            System.out.println("readRiversMeshJson" + e.getMessage());
            return null;
        }
    }

    private float[] readFloatArray(JsonParser parser) throws Exception {
        FloatList temp = new FloatList();
        while (parser.nextToken() != JsonToken.END_ARRAY) {
            temp.add(parser.getFloatValue());
        }
        float[] result = new float[temp.size()];
        for (int i = 0; i < result.length; i++) {
            result[i] = temp.get(i);
        }
        return result;
    }


    private IntBuffer readIntBuffer(JsonParser parser) throws Exception {
        IntList temp = new IntList();
        while (parser.nextToken() != JsonToken.END_ARRAY) {
            temp.add(parser.getIntValue());
        }
        IntBuffer buffer = ByteBuffer
            .allocateDirect(temp.size() * Integer.BYTES)
            .order(ByteOrder.nativeOrder())
            .asIntBuffer();

        for(int i = 0; i < temp.size(); i++) {
            buffer.put(temp.get(i));
        }

        buffer.flip();
        return buffer;
    }
}
