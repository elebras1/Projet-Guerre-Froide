package com.populaire.projetguerrefroide.util;

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
    private ByteBuffer commandBuffer;
    private int commandCount;
    private int indirectBufferID;

    public MeshMultiDrawIndirect(boolean isStatic, int maxVertices, int maxIndices, VertexAttribute... attributes) {
        this.mesh = new Mesh(isStatic, maxVertices, maxIndices, attributes);
    }

    public void setVertices(float[] vertices) {
        this.mesh.setVertices(vertices);
    }

    public void setIndirectCommands(IntBuffer starts, IntBuffer counts) {
        this.commandCount = counts.remaining();
        this.commandBuffer = BufferUtils.newByteBuffer(this.commandCount * 16);
        this.indirectBufferID = GL43.glGenBuffers();

        for (int i = 0; i < this.commandCount; i++) {
            int count = counts.get(i);
            int first = starts.get(i);

            this.commandBuffer.putInt(count);
            this.commandBuffer.putInt(3);
            this.commandBuffer.putInt(first);
            this.commandBuffer.putInt(0);
        }
        this.commandBuffer.flip();

        GL43.glBindBuffer(GL43.GL_DRAW_INDIRECT_BUFFER, this.indirectBufferID);
        GL43.glBufferData(GL43.GL_DRAW_INDIRECT_BUFFER, this.commandBuffer, GL43.GL_STATIC_DRAW);
        GL43.glBindBuffer(GL43.GL_DRAW_INDIRECT_BUFFER, 0);
    }

    public void bind(ShaderProgram shader) {
        this.mesh.bind(shader);
    }

    public void unbind(ShaderProgram shader) {
        this.mesh.unbind(shader);
    }

    public int getCommandCount() {
        return this.commandCount;
    }

    public int getIndirectBufferID() {
        return this.indirectBufferID;
    }

    @Override
    public void dispose() {
        this.mesh.dispose();
        GL43.glDeleteBuffers(this.indirectBufferID);
    }
}
