package com.populaire.projetguerrefroide.adapter.graphics;

import com.badlogic.gdx.graphics.VertexAttributes;
import com.monstrous.gdx.webgpu.graphics.WgMesh;
import com.monstrous.gdx.webgpu.graphics.g3d.WgIndexBuffer;
import com.monstrous.gdx.webgpu.graphics.g3d.WgVertexBuffer;
import com.monstrous.gdx.webgpu.wrappers.WebGPURenderPass;

import java.nio.IntBuffer;

public class WgMeshMulti extends WgMesh {

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

    public WgMeshMulti(boolean isStatic, int maxVertices, int maxIndices, VertexAttributes attributes) {
        super(isStatic, maxVertices, maxIndices, attributes);
        this.commands = new DrawCommand[0];
        this.commandCount = 0;
    }

    public void setIndirectCommands(IntBuffer starts, IntBuffer counts) {
        this.commandCount = counts.remaining();
        this.commands = new DrawCommand[this.commandCount];

        starts.rewind();
        counts.rewind();

        for (int i = 0; i < this.commandCount; i++) {
            int count = counts.get(i);
            int first = starts.get(i);
            this.commands[i] = new DrawCommand(count, first, 0);
        }
    }

    @Override
    public void render(WebGPURenderPass renderPass, int primitiveType, int offset, int size, int numInstances, int firstInstance) {
        if (this.commandCount == 0 || this.commands == null) {
            super.render(renderPass, primitiveType, offset, size, numInstances, firstInstance);
            return;
        }

        ((WgVertexBuffer)this.vertices).bind(renderPass);

        if (getIndexData().getNumIndices() > 0) {
            ((WgIndexBuffer)getIndexData()).bind(renderPass);

            for (int i = 0; i < this.commandCount; i++) {
                DrawCommand command = commands[i];
                renderPass.drawIndexed(command.count, numInstances, command.first, 0, command.firstInstance);
            }
        } else {
            for (int i = 0; i < commandCount; i++) {
                DrawCommand command = commands[i];
                renderPass.draw(command.count, numInstances, command.first, command.firstInstance);
            }
        }
    }
}
