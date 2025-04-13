package com.populaire.projetguerrefroide.entity;

public class RawMesh {
    private final float[] vertices;
    private final short[] indices;

    public RawMesh(float[] vertices, short[] indices) {
        this.vertices = vertices;
        this.indices = indices;
    }

    public float[] getVertices() {
        return this.vertices;
    }

    public short[] getIndices() {
        return this.indices;
    }
}
