package com.populaire.projetguerrefroide.entity;

import java.nio.IntBuffer;

public record RawMeshMulti(float[] vertices, IntBuffer starts, IntBuffer counts) {
}
