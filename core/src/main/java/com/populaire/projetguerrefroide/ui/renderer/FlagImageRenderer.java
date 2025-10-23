package com.populaire.projetguerrefroide.ui.renderer;

import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.utils.Disposable;
import com.github.xpenatan.webgpu.*;
import com.monstrous.gdx.webgpu.graphics.Binder;
import com.monstrous.gdx.webgpu.graphics.WgMesh;
import com.monstrous.gdx.webgpu.graphics.WgTexture;
import com.monstrous.gdx.webgpu.wrappers.*;
import com.populaire.projetguerrefroide.util.IndexedPoint;
import com.populaire.projetguerrefroide.util.WgslUtils;

public class FlagImageRenderer implements Disposable {
    private final int width;
    private final int height;
    private final float[] vertices;
    private final short[] indices;
    private final WgMesh mesh;
    private final WebGPUUniformBuffer uniformBuffer;
    private final Binder binder;
    private final WebGPUPipeline pipeline;
    private final int uniformBufferSize;
    private int numberFlags;
    private int cursorX;
    private int cursorY;
    private int cursorRowHeight;
    private final int cursorPadding;
    private static final int NUMBER_MAX_FLAGS = 250;
    private static final int VERTICES_PER_FLAG = 4;
    private static final int INDICES_PER_FLAG = 6;
    private static final int FLOATS_PER_VERTEX = 10;

    public FlagImageRenderer(Texture overlayTexture, Texture alphaTexture, Texture flagTexture, int width, int height) {
        this.width = width;
        this.height = height;
        this.uniformBufferSize = 16 * Float.BYTES;
        this.uniformBuffer = new WebGPUUniformBuffer(this.uniformBufferSize, WGPUBufferUsage.CopyDst.or(WGPUBufferUsage.Uniform));
        VertexAttributes vertexAttributes = new VertexAttributes(
            new VertexAttribute(VertexAttributes.Usage.Position, 2, ShaderProgram.POSITION_ATTRIBUTE),
            new VertexAttribute(VertexAttributes.Usage.TextureCoordinates, 2, ShaderProgram.TEXCOORD_ATTRIBUTE),
            new VertexAttribute(VertexAttributes.Usage.Normal, 2, "uvOverlay"),
            new VertexAttribute(VertexAttributes.Usage.Tangent, 2, "uvAlpha"),
            new VertexAttribute(VertexAttributes.Usage.BiNormal, 2, "uvFlag")
        );

        this.vertices = new float[NUMBER_MAX_FLAGS * VERTICES_PER_FLAG * FLOATS_PER_VERTEX];
        this.indices = new short[NUMBER_MAX_FLAGS * INDICES_PER_FLAG];
        this.initializeIndices();
        this.mesh = this.generateMeshProvinces(vertexAttributes);
        this.binder = this.createBinder();
        this.pipeline = this.createPipeline(vertexAttributes, WgslUtils.getShaderSource("flag.wgsl"));
        this.bindStaticTextures(overlayTexture, alphaTexture, flagTexture);
        this.numberFlags = 0;
        this.cursorX = 0;
        this.cursorY = 0;
        this.cursorPadding = 2;
        this.cursorRowHeight = 0;
    }

    private WgMesh generateMeshProvinces(VertexAttributes vertexAttributes) {

        WgMesh mesh = new WgMesh(false, this.vertices.length / FLOATS_PER_VERTEX, this.indices.length, vertexAttributes);
        mesh.setIndices(this.indices);

        return mesh;
    }

    private Binder createBinder(){
        Binder binder = new Binder();
        binder.defineGroup(0, this.createBindGroupLayout());

        binder.defineBinding("uniforms", 0, 0);
        binder.defineBinding("textureFlag", 0, 1);
        binder.defineBinding("textureFlagSampler", 0, 2);
        binder.defineBinding("textureOverlay", 0, 3);
        binder.defineBinding("textureOverlaySampler", 0, 4);
        binder.defineBinding("textureAlpha", 0, 5);
        binder.defineBinding("textureAlphaSampler", 0, 6);

        int offset = 0;
        binder.defineUniform("projTrans", 0, 0, offset);

        binder.setBuffer("uniforms", this.uniformBuffer, 0, this.uniformBufferSize);

        return binder;
    }

    private void initializeIndices() {
        for (int i = 0; i < NUMBER_MAX_FLAGS; i++) {
            int vertexStart = i * VERTICES_PER_FLAG;
            int indexStart = i * INDICES_PER_FLAG;

            this.indices[indexStart] = (short)(vertexStart);
            this.indices[indexStart + 1] = (short)(vertexStart + 1);
            this.indices[indexStart + 2] = (short)(vertexStart + 2);

            this.indices[indexStart + 3] = (short)(vertexStart);
            this.indices[indexStart + 4] = (short)(vertexStart + 2);
            this.indices[indexStart + 5] = (short)(vertexStart + 3);
        }
    }

    private WebGPUBindGroupLayout createBindGroupLayout() {
        WebGPUBindGroupLayout layout = new WebGPUBindGroupLayout("bind group layout flagImage");
        layout.begin();
        layout.addBuffer(0, WGPUShaderStage.Vertex.or(WGPUShaderStage.Fragment), WGPUBufferBindingType.Uniform, this.uniformBufferSize, false);
        layout.addTexture(1, WGPUShaderStage.Fragment, WGPUTextureSampleType.Float, WGPUTextureViewDimension._2D, false);
        layout.addSampler(2, WGPUShaderStage.Fragment, WGPUSamplerBindingType.Filtering);
        layout.addTexture(3, WGPUShaderStage.Fragment, WGPUTextureSampleType.Float, WGPUTextureViewDimension._2D, false);
        layout.addSampler(4, WGPUShaderStage.Fragment, WGPUSamplerBindingType.Filtering);
        layout.addTexture(5, WGPUShaderStage.Fragment, WGPUTextureSampleType.Float, WGPUTextureViewDimension._2D, false);
        layout.addSampler(6, WGPUShaderStage.Fragment, WGPUSamplerBindingType.Filtering);
        layout.end();
        return layout;
    }

    private WebGPUPipeline createPipeline(VertexAttributes vertexAttributes, String shaderSource) {
        PipelineSpecification pipelineSpec = new PipelineSpecification(vertexAttributes, shaderSource);
        pipelineSpec.name = "pipeline";
        pipelineSpec.enableBlending();
        return new WebGPUPipeline(this.binder.getPipelineLayout("pipeline layout flagImage"), pipelineSpec);
    }

    private void bindStaticTextures(Texture overlayTexture, Texture alphaTexture, Texture flagTexture) {
        this.binder.setTexture("textureOverlay", ((WgTexture) overlayTexture).getTextureView());
        this.binder.setSampler("textureOverlaySampler", ((WgTexture) overlayTexture).getSampler());
        this.binder.setTexture("textureAlpha", ((WgTexture) alphaTexture).getTextureView());
        this.binder.setSampler("textureAlphaSampler", ((WgTexture) alphaTexture).getSampler());
        this.binder.setTexture("textureFlag", ((WgTexture) flagTexture).getTextureView());
        this.binder.setSampler("textureFlagSampler", ((WgTexture) flagTexture).getSampler());
    }

    private void updateMesh(IndexedPoint indexedPoint, TextureRegion overlayRegion, TextureRegion alphaRegion, TextureRegion flagRegion, float width, float height) {
        boolean flagExist = indexedPoint.getIndex() != -1;

        int vertexOffset = this.numberFlags * VERTICES_PER_FLAG * FLOATS_PER_VERTEX;
        int x = this.cursorX + this.cursorPadding;
        int y = this.cursorY + this.cursorPadding;

        if(flagExist) {
            vertexOffset = indexedPoint.getIndex() * VERTICES_PER_FLAG * FLOATS_PER_VERTEX;
            x = indexedPoint.getX();
            y = indexedPoint.getY();
        }

        float overlayU1 = overlayRegion.getU();
        float overlayV1 = overlayRegion.getV();
        float overlayU2 = overlayRegion.getU2();
        float overlayV2 = overlayRegion.getV2();

        float alphaU1 = alphaRegion.getU();
        float alphaV1 = alphaRegion.getV();
        float alphaU2 = alphaRegion.getU2();
        float alphaV2 = alphaRegion.getV2();

        float flagU1 = flagRegion.getU();
        float flagV1 = flagRegion.getV();
        float flagU2 = flagRegion.getU2();
        float flagV2 = flagRegion.getV2();

        this.vertices[vertexOffset++] = x;
        this.vertices[vertexOffset++] = y;
        this.vertices[vertexOffset++] = 0f;
        this.vertices[vertexOffset++] = 1f;
        this.vertices[vertexOffset++] = overlayU1;
        this.vertices[vertexOffset++] = overlayV2;
        this.vertices[vertexOffset++] = alphaU1;
        this.vertices[vertexOffset++] = alphaV2;
        this.vertices[vertexOffset++] = flagU1;
        this.vertices[vertexOffset++] = flagV2;

        this.vertices[vertexOffset++] = x + width;
        this.vertices[vertexOffset++] = y;
        this.vertices[vertexOffset++] = 1f;
        this.vertices[vertexOffset++] = 1f;
        this.vertices[vertexOffset++] = overlayU2;
        this.vertices[vertexOffset++] = overlayV2;
        this.vertices[vertexOffset++] = alphaU2;
        this.vertices[vertexOffset++] = alphaV2;
        this.vertices[vertexOffset++] = flagU2;
        this.vertices[vertexOffset++] = flagV2;

        this.vertices[vertexOffset++] = x + width;
        this.vertices[vertexOffset++] = y + height;
        this.vertices[vertexOffset++] = 1f;
        this.vertices[vertexOffset++] = 0f;
        this.vertices[vertexOffset++] = overlayU2;
        this.vertices[vertexOffset++] = overlayV1;
        this.vertices[vertexOffset++] = alphaU2;
        this.vertices[vertexOffset++] = alphaV1;
        this.vertices[vertexOffset++] = flagU2;
        this.vertices[vertexOffset++] = flagV1;

        this.vertices[vertexOffset++] = x;
        this.vertices[vertexOffset++] = y + height;
        this.vertices[vertexOffset++] = 0f;
        this.vertices[vertexOffset++] = 0f;
        this.vertices[vertexOffset++] = overlayU1;
        this.vertices[vertexOffset++] = overlayV1;
        this.vertices[vertexOffset++] = alphaU1;
        this.vertices[vertexOffset++] = alphaV1;
        this.vertices[vertexOffset++] = flagU1;
        this.vertices[vertexOffset] = flagV1;

        if(!flagExist) {
            indexedPoint.setIndex(this.numberFlags);
            indexedPoint.setX(x);
            indexedPoint.setY(y);
            this.moveCursor((int)width, (int)height);
            this.numberFlags++;
        }
    }

    public void moveCursor(int widthElement, int heightElement) {
        if (this.cursorX + widthElement + this.cursorPadding > this.width) {
            this.cursorX = 0;
            this.cursorY += this.cursorRowHeight;
            this.cursorRowHeight = 0;
        }

        if (this.cursorY + heightElement + this.cursorPadding > this.height) {
            return;
        }

        if (this.cursorRowHeight < heightElement + this.cursorPadding * 2) {
            this.cursorRowHeight = heightElement + this.cursorPadding * 2;
        }

        this.cursorX += widthElement + this.cursorPadding * 2;
    }

    public void setProjectionMatrix(Matrix4 projectionMatrix) {
        this.binder.setUniform("projTrans", projectionMatrix);
        this.uniformBuffer.flush();
    }

    public void add(IndexedPoint indexedPoint, TextureRegion overlayRegion, TextureRegion alphaRegion, TextureRegion flagRegion, float width, float height) {
        if(this.numberFlags >= NUMBER_MAX_FLAGS) {
            throw  new IllegalArgumentException("Too many flags in indexed image");
        }
        this.updateMesh(indexedPoint, overlayRegion, alphaRegion, flagRegion, width, height);
    }

    public void render() {
        this.mesh.setVertices(this.vertices);
        WebGPURenderPass pass = RenderPassBuilder.create("Provinces pass");
        pass.setPipeline(this.pipeline);
        this.binder.bindGroup(pass, 0);
        int indicesToRender = this.numberFlags * INDICES_PER_FLAG;
        this.mesh.render(pass, GL20.GL_TRIANGLES, 0, indicesToRender, 1, 0);
        pass.end();
    }

    @Override
    public void dispose() {
        this.binder.dispose();
        this.uniformBuffer.dispose();
    }
}
