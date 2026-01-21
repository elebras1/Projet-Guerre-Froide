package com.populaire.projetguerrefroide.util;

import com.badlogic.gdx.Gdx;
import com.monstrous.gdx.webgpu.graphics.WgShaderProgram;

public class WgslUtils {

    public static String getShaderSource(String shaderFileName) {
        return Gdx.files.internal("shaders/" + shaderFileName).readString();
    }

    public static WgShaderProgram getShader(String shaderFileName) {
        return new WgShaderProgram(Gdx.files.internal("shaders/" + shaderFileName));
    }
}
