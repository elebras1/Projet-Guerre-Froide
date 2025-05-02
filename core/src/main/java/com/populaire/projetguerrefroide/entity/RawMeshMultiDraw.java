package com.populaire.projetguerrefroide.entity;

import java.nio.IntBuffer;

public class RawMeshMultiDraw {
    private final float[] vertices;
    private final IntBuffer starts;
    private final IntBuffer counts;

    public RawMeshMultiDraw(float[] vertices, IntBuffer starts, IntBuffer counts) {
        this.vertices = vertices;
        this.starts = starts;
        this.counts = counts;
    }

    public float[] getVertices() {
        return this.vertices;
    }

    public IntBuffer getStarts() {
        return this.starts;
    }

    public IntBuffer getCounts() {
        return this.counts;
    }
}
