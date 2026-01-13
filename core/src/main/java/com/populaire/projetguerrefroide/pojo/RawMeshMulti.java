package com.populaire.projetguerrefroide.pojo;

import java.nio.IntBuffer;

public record RawMeshMulti(float[] vertices, IntBuffer starts, IntBuffer counts) {
}
