package com.populaire.projetguerrefroide.util;

import com.badlogic.gdx.Gdx;

public class WgslUtils {

    public static String getShaderSource(String shaderFileName) {
        return Gdx.files.internal("shaders/" + shaderFileName).readString();
    }

    public static String getShaderSource(String vertexShaderFileName, String fragmentShaderFileName) {
        String vertexShader = Gdx.files.internal("shaders/" + vertexShaderFileName).readString();
        String fragmentShader = Gdx.files.internal("shaders/" + fragmentShaderFileName).readString();
        return vertexShader + "\n" + fragmentShader;
    }
}
