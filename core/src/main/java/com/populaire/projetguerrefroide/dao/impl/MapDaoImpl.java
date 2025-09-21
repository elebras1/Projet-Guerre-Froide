package com.populaire.projetguerrefroide.dao.impl;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.dslplatform.json.DslJson;
import com.dslplatform.json.JsonReader;
import com.dslplatform.json.NumberConverter;
import com.github.tommyettinger.ds.FloatList;
import com.github.tommyettinger.ds.IntList;
import com.populaire.projetguerrefroide.dao.MapDao;
import com.populaire.projetguerrefroide.entity.RawMeshMulti;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;

public class MapDaoImpl implements MapDao {
    private final String riversMeshFile = "map/rivers_mesh.json";

    @Override
    public RawMeshMulti readRiversMeshJson() {
        FileHandle fh = Gdx.files.internal(this.riversMeshFile);
        try (InputStream is = fh.read()) {
            byte[] bytes = is.readAllBytes();
            DslJson<Object> dslJson = new DslJson<>();
            JsonReader<Object> reader = dslJson.newReader().process(bytes, bytes.length);

            float[] vertices = null;
            IntBuffer startsBuffer = null;
            IntBuffer countsBuffer = null;

            reader.getNextToken();
            if (reader.last() != '{') {
                throw new RuntimeException("Expected '{' at the beginning of JSON object");
            }

            byte token = reader.getNextToken();
            while (token != '}') {
                if (token == ',') {
                    token = reader.getNextToken();
                    continue;
                }

                String name = reader.readKey();

                while (reader.last() != '[') {
                    reader.getNextToken();
                }

                switch (name) {
                    case "vertices":
                        vertices = readFloatArray(reader);
                        break;
                    case "starts":
                        startsBuffer = readIntBuffer(reader);
                        break;
                    case "counts":
                        countsBuffer = readIntBuffer(reader);
                        break;
                    default:
                        reader.skip();
                }

                token = reader.getNextToken();
            }

            if (vertices == null)
                throw new RuntimeException("Missing 'vertices' in JSON");

            return new RawMeshMulti(vertices, startsBuffer, countsBuffer);

        } catch (IOException ioException) {
            ioException.printStackTrace();
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        return null;
    }

    private float[] readFloatArray(JsonReader<?> reader) throws Exception {
        FloatList temp = new FloatList();

        if (reader.last() != '[') {
            throw new RuntimeException("Expected '[' at the beginning of float array");
        }

        byte token = reader.getNextToken();
        while (token != ']') {
            if (token == ',') {
                token = reader.getNextToken();
                continue;
            }

            temp.add(NumberConverter.deserializeFloat(reader));
            token = reader.getNextToken();
        }

        float[] result = new float[temp.size()];
        for (int i = 0; i < result.length; i++) {
            result[i] = temp.get(i);
        }
        return result;
    }

    private IntBuffer readIntBuffer(JsonReader<?> reader) throws Exception {
        IntList temp = new IntList();

        if (reader.last() != '[') {
            throw new RuntimeException("Expected '[' at the beginning of int array");
        }

        byte token = reader.getNextToken();
        while (token != ']') {
            if (token == ',') {
                token = reader.getNextToken();
                continue;
            }

            temp.add(NumberConverter.deserializeInt(reader));
            token = reader.getNextToken();
        }

        IntBuffer buffer = ByteBuffer
            .allocateDirect(temp.size() * Integer.BYTES)
            .order(ByteOrder.nativeOrder())
            .asIntBuffer();

        for (int i = 0; i < temp.size(); i++) {
            buffer.put(temp.get(i));
        }

        buffer.flip();
        return buffer;
    }
}
