package com.populaire.projetguerrefroide.graphics;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;

public class ShaderManager {
    private final ShaderProgram mapShader;

    public ShaderManager() {
        this.mapShader = this.loadShader("map_v.glsl", "map_f.glsl");
        ShaderProgram.pedantic = false;
    }

    private ShaderProgram loadShader(String vertexShaderFile, String fragmentShaderFile) {
        String vertexShader = Gdx.files.internal("shaders/" + vertexShaderFile).readString();
        String fragmentShader = Gdx.files.internal("shaders/" + fragmentShaderFile).readString();
        ShaderProgram shaderProgram = new ShaderProgram(vertexShader, fragmentShader);

        if (!shaderProgram.isCompiled()) {
            System.err.println("Error compiling shader: " + shaderProgram.getLog());
            return null;
        }
        return shaderProgram;
    }

    public ShaderProgram getMapShader() {
        return this.mapShader;
    }
}
