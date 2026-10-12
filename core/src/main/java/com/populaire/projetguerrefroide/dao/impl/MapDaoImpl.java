package com.populaire.projetguerrefroide.dao.impl;

import com.badlogic.gdx.Gdx;
import com.dslplatform.json.DslJson;
import com.populaire.projetguerrefroide.dao.MapDao;
import com.populaire.projetguerrefroide.dao.json.RiversMeshJson;
import com.populaire.projetguerrefroide.pojo.RawMeshMulti;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;

public class MapDaoImpl implements MapDao {
    private final String riversMeshFile = "map/rivers_mesh.json";

    @Override
    public RawMeshMulti readRiversMesh() {
        try {
            byte[] bytes = Gdx.files.internal(this.riversMeshFile).readBytes();
            RiversMeshJson riversMesh = new DslJson<>().deserialize(RiversMeshJson.class, bytes, bytes.length);
            return new RawMeshMulti(riversMesh.vertices(), this.toIntBuffer(riversMesh.starts()), this.toIntBuffer(riversMesh.counts()));
        } catch (IOException ioException) {
            ioException.printStackTrace();
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        return null;
    }

    private IntBuffer toIntBuffer(int[] values) {
        IntBuffer buffer = ByteBuffer
            .allocateDirect(values.length * Integer.BYTES)
            .order(ByteOrder.nativeOrder())
            .asIntBuffer();
        buffer.put(values);
        buffer.flip();
        return buffer;
    }
}
