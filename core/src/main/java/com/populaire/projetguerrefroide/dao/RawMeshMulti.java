package com.populaire.projetguerrefroide.dao;

import java.nio.IntBuffer;

public record RawMeshMulti(float[] vertices, IntBuffer starts, IntBuffer counts) {
}
