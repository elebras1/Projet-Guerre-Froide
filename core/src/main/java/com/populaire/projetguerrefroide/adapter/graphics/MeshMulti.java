package com.populaire.projetguerrefroide.adapter.graphics;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL32;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;

import java.nio.IntBuffer;

public class MeshMulti extends Mesh {
    private DrawCommand[] commands;
    private int commandCount;

    public static class DrawCommand {
        public int count;
        public int first;
        public int firstInstance;

        public DrawCommand(int count, int first, int firstInstance) {
            this.count = count;
            this.first = first;
            this.firstInstance = firstInstance;
        }
    }

    public MeshMulti(boolean isStatic, int maxVertices, int maxIndices, VertexAttribute... attributes) {
        super(isStatic, maxVertices, maxIndices, attributes);
        this.commands = new DrawCommand[0];
        this.commandCount = 0;
    }

    public void setIndirectCommands(IntBuffer starts, IntBuffer counts) {
        this.commandCount = counts.remaining();
        this.commands = new DrawCommand[this.commandCount];

        starts.rewind();
        counts.rewind();

        for(int i = 0; i < this.commandCount; i++) {
            int count = counts.get(i);
            int first = starts.get(i);
            this.commands[i] = new DrawCommand(count, first, 0);
        }
    }

    public void render(ShaderProgram shaderProgram, int primitiveType, int offset, int count, int numInstances, int firstInstance) {
        if(this.commands == null || this.commandCount == 0) {
            super.render(shaderProgram, primitiveType, offset, count);
            return;
        }

        this.bind(shaderProgram);

        if (getNumIndices() > 0) {
            for(int i = 0; i < this.commandCount; i++) {
                DrawCommand command = commands[i];
                Gdx.gl32.glDrawElementsInstanced(primitiveType, command.count, GL32.GL_UNSIGNED_SHORT, command.first * 2, numInstances);
            }
        } else {
            for(int i = 0; i < this.commandCount; i++) {
                DrawCommand command = commands[i];
                Gdx.gl32.glDrawArraysInstanced(primitiveType, command.first, command.count, numInstances);
            }
        }

        this.unbind(shaderProgram);
    }
}
