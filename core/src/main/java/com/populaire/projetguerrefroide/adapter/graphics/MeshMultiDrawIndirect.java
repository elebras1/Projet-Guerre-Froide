package com.populaire.projetguerrefroide.adapter.graphics;

import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.utils.BufferUtils;
import com.badlogic.gdx.utils.Disposable;
import org.lwjgl.opengl.GL43;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

public class MeshMultiDrawIndirect implements Disposable {
    private final Mesh mesh;
    private int commandCount;
    private int indirectBufferID;

    public MeshMultiDrawIndirect(boolean isStatic, int maxVertices, int maxIndices, VertexAttribute... attributes) {
        this.mesh = new Mesh(isStatic, maxVertices, maxIndices, attributes);
    }

    public void setVertices(float[] vertices) {
        this.mesh.setVertices(vertices);
    }

    public void setIndices(short[] indices) {
        this.mesh.setIndices(indices);
    }

    public void setIndirectCommands(IntBuffer starts, IntBuffer counts) {
        this.commandCount = counts.remaining();
        ByteBuffer commandBuffer = BufferUtils.newByteBuffer(this.commandCount * 16);
        this.indirectBufferID = GL43.glGenBuffers();

        for (int i = 0; i < this.commandCount; i++) {
            int count = counts.get(i);
            int first = starts.get(i);

            commandBuffer.putInt(count);
            commandBuffer.putInt(3);
            commandBuffer.putInt(first);
            commandBuffer.putInt(0);
        }
        commandBuffer.flip();

        GL43.glBindBuffer(GL43.GL_DRAW_INDIRECT_BUFFER, this.indirectBufferID);
        GL43.glBufferData(GL43.GL_DRAW_INDIRECT_BUFFER, commandBuffer, GL43.GL_STATIC_DRAW);
        GL43.glBindBuffer(GL43.GL_DRAW_INDIRECT_BUFFER, 0);
    }

    public void bind(ShaderProgram shader) {
        this.mesh.bind(shader);
        GL43.glBindBuffer(GL43.GL_DRAW_INDIRECT_BUFFER, this.indirectBufferID);
    }

    public void unbind(ShaderProgram shader) {
        GL43.glBindBuffer(GL43.GL_DRAW_INDIRECT_BUFFER, 0);
        this.mesh.unbind(shader);
    }

    public int getCommandCount() {
        return this.commandCount;
    }

    @Override
    public void dispose() {
        this.mesh.dispose();
        GL43.glDeleteBuffers(this.indirectBufferID);
    }
}
