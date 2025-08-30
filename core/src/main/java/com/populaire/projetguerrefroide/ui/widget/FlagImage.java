package com.populaire.projetguerrefroide.ui.widget;

import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Vector4;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.utils.Disposable;
import com.github.xpenatan.webgpu.*;
import com.monstrous.gdx.webgpu.graphics.Binder;
import com.monstrous.gdx.webgpu.graphics.WgMesh;
import com.monstrous.gdx.webgpu.graphics.WgTexture;
import com.monstrous.gdx.webgpu.wrappers.*;
import com.populaire.projetguerrefroide.adapter.graphics.WgScreenViewport;
import com.populaire.projetguerrefroide.util.WgslUtils;

public class FlagImage extends Actor implements Disposable {
    private TextureRegion flagTexture;
    private final TextureRegion overlayTexture;
    private final TextureRegion alphaTexture;
    private final WgMesh mesh;
    private final WebGPUPipeline pipeline;
    private final Binder binder;
    private final WebGPUUniformBuffer uniformBuffer;
    private final int uniformBufferSize;
    private float[] vertices;

    public FlagImage(TextureRegion overlay, TextureRegion alpha) {
        this.setSize(overlay.getRegionWidth(), overlay.getRegionHeight());
        this.overlayTexture = overlay;
        this.alphaTexture = alpha;
        VertexAttributes vertexAttributes = new VertexAttributes(new VertexAttribute(VertexAttributes.Usage.Position, 2, ShaderProgram.POSITION_ATTRIBUTE), new VertexAttribute(VertexAttributes.Usage.TextureCoordinates, 2, ShaderProgram.TEXCOORD_ATTRIBUTE));
        this.mesh = this.createMesh(vertexAttributes);
        this.uniformBufferSize = (16 + 4 * 3) * Float.BYTES;
        this.uniformBuffer = new WebGPUUniformBuffer(this.uniformBufferSize, WGPUBufferUsage.CopyDst.or(WGPUBufferUsage.Uniform));
        this.binder = this.createBinder();
        this.pipeline = this.createPipeline(vertexAttributes, WgslUtils.getShaderSource("flag.wgsl"));
        this.bindStaticTextures();
    }

    private WgMesh createMesh(VertexAttributes vertexAttributes) {
        WgMesh mesh = new WgMesh(false, 4, 6, vertexAttributes);

        this.vertices = new float[] {
            this.getX(), this.getY(), 0f, 1f,
            this.getX() + this.getWidth(), this.getY(), 1f, 1f,
            this.getX() + this.getWidth(), this.getY() + this.getHeight(), 1f, 0f,
            this.getX(), this.getY() + this.getHeight(), 0f, 0f
        };


        short[] indices = new short[] {
            0, 1, 2,
            0, 2, 3
        };

        mesh.setVertices(this.vertices);
        mesh.setIndices(indices);

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
        offset += 16 * Float.BYTES;
        binder.defineUniform("uvFlag", 0, 0, offset);
        offset += 4 * Float.BYTES;
        binder.defineUniform("uvOverlay", 0, 0, offset);
        offset += 4 * Float.BYTES;
        binder.defineUniform("uvAlpha", 0, 0, offset);

        binder.setBuffer("uniforms", this.uniformBuffer, 0, this.uniformBufferSize);

        return binder;
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

    private void bindStaticTextures() {
        this.binder.setTexture("textureOverlay", ((WgTexture) this.overlayTexture.getTexture()).getTextureView());
        this.binder.setSampler("textureOverlaySampler", ((WgTexture) this.overlayTexture.getTexture()).getSampler());
        this.binder.setUniform("uvOverlay", new Vector4(this.overlayTexture.getU(), this.overlayTexture.getV(), this.overlayTexture.getU2(), this.overlayTexture.getV2()));
        this.binder.setTexture("textureAlpha", ((WgTexture) this.alphaTexture.getTexture()).getTextureView());
        this.binder.setSampler("textureAlphaSampler", ((WgTexture) this.alphaTexture.getTexture()).getSampler());
        this.binder.setUniform("uvAlpha", new Vector4(this.alphaTexture.getU(), this.alphaTexture.getV(), this.alphaTexture.getU2(), this.alphaTexture.getV2()));
        this.uniformBuffer.flush();
    }

    public void setFlag(TextureRegion flag) {
        this.flagTexture = flag;
        this.binder.setTexture("textureFlag", ((WgTexture) this.flagTexture.getTexture()).getTextureView());
        this.binder.setSampler("textureFlagSampler", ((WgTexture) this.flagTexture.getTexture()).getSampler());
        this.binder.setUniform("uvFlag", new Vector4(this.flagTexture.getU(), this.flagTexture.getV(), this.flagTexture.getU2(), this.flagTexture.getV2()));
        this.uniformBuffer.flush();
    }

    @Override
    public void positionChanged() {
        if(this.binder == null || this.getStage() == null) {
            return;
        }

        this.updateMeshVertices();
        this.binder.setUniform("projTrans", ((WgScreenViewport)this.getStage().getViewport()).getProjectionMatrix());
        this.uniformBuffer.flush();
    }

    private void updateMeshVertices() {
        int i = 0;
        this.vertices[i++] = this.getX();
        this.vertices[i++] = this.getY();
        this.vertices[i++] = 0f;
        this.vertices[i++] = 1f;
        this.vertices[i++] = this.getX() + this.getWidth();
        this.vertices[i++] = this.getY();
        this.vertices[i++] = 1f;
        this.vertices[i++] = 1f;
        this.vertices[i++] = this.getX() + this.getWidth();
        this.vertices[i++] = this.getY() + this.getHeight();
        this.vertices[i++] = 1f;
        this.vertices[i++] = 0f;
        this.vertices[i++] = this.getX();
        this.vertices[i++] = this.getY() + this.getHeight();
        this.vertices[i++] = 0f;
        this.vertices[i] = 0f;

        this.mesh.setVertices(this.vertices);
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        if(flagTexture == null) {
            return;
        }

        batch.end();
        this.positionChanged();

        WebGPURenderPass pass = RenderPassBuilder.create("Flag image pass");
        pass.setPipeline(this.pipeline);
        this.binder.bindGroup(pass, 0);

        this.mesh.render(pass, GL20.GL_TRIANGLES, 0, this.mesh.getNumIndices(), 1, 0);

        pass.end();
        batch.begin();
    }

    @Override
    public void dispose() {
        this.mesh.dispose();
        this.pipeline.dispose();
        this.binder.dispose();
        this.uniformBuffer.dispose();
    }
}
