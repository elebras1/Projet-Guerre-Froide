package com.populaire.projetguerrefroide.dao;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.populaire.projetguerrefroide.entity.RawMesh;

import java.io.IOException;

public class MapDao {
    private final ObjectMapper mapper;
    private final String mapPath = "map/";
    private final String riversMeshFile = this.mapPath + "rivers_mesh.json";

    public MapDao() {
        this.mapper = new ObjectMapper();
    }

    private JsonNode openJson(String fileName) throws IOException {
        FileHandle fileHandle = Gdx.files.internal(fileName);
        return this.mapper.readTree(fileHandle.readString());
    }

    public RawMesh readRiversMeshJson() {
        try {
            JsonNode jsonNode = openJson(this.riversMeshFile);
            float[] vertices = new float[jsonNode.get("vertices").size()];
            short[] indices = new short[jsonNode.get("indices").size()];

            for (int i = 0; i < vertices.length; i++) {
                vertices[i] = jsonNode.get("vertices").get(i).floatValue();
            }
            for (int i = 0; i < indices.length; i++) {
                indices[i] = jsonNode.get("indices").get(i).shortValue();
            }
            return new RawMesh(vertices, indices);
        } catch (Exception e) {
            System.err.println("readRiversMeshJson: " + e);
        }

        return null;
    }
}
