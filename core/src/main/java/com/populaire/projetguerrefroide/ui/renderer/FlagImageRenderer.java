package com.populaire.projetguerrefroide.ui.renderer;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector4;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.BufferUtils;
import com.badlogic.gdx.utils.Disposable;
import com.github.tommyettinger.ds.ObjectList;
import com.github.xpenatan.webgpu.*;
import com.monstrous.gdx.webgpu.graphics.Binder;
import com.monstrous.gdx.webgpu.graphics.WgTexture;
import com.monstrous.gdx.webgpu.wrappers.*;
import com.populaire.projetguerrefroide.util.WgslUtils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.List;

public class FlagImageRenderer implements Disposable {
    private WebGPUPipeline pipeline;
    private Binder binder;
    private WebGPUVertexBuffer vertexBuffer;
    private WebGPUIndexBuffer indexBuffer;
    private WebGPUUniformBuffer uniformBuffer;
    private int uniformBufferSize;
    private final int vertexSize;
    private final ByteBuffer vertexBB;
    private final FloatBuffer vertexFloats;
    private final Vector4 uvOverlay;
    private final Vector4 uvAlpha;
    private final Vector4 uvFlag;
    private final List<FlagInstance> flagInstances = new ObjectList<>();

    private static class FlagInstance {
        TextureRegion flagRegion;
        TextureRegion overlayRegion;
        TextureRegion alphaRegion;
        float x, y, width, height;

        FlagInstance(TextureRegion flag, TextureRegion overlay, TextureRegion alpha, float x, float y, float width, float height) {
            this.flagRegion = flag;
            this.overlayRegion = overlay;
            this.alphaRegion = alpha;
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
        }
    }

    public FlagImageRenderer() {
        VertexAttributes vertexAttributes = new VertexAttributes(
            new VertexAttribute(VertexAttributes.Usage.Position, 2, ShaderProgram.POSITION_ATTRIBUTE),
            new VertexAttribute(VertexAttributes.Usage.TextureCoordinates, 2, ShaderProgram.TEXCOORD_ATTRIBUTE)
        );

        this.vertexSize = vertexAttributes.vertexSize;

        this.vertexBB = BufferUtils.newUnsafeByteBuffer(4 * this.vertexSize);
        this.vertexBB.order(ByteOrder.LITTLE_ENDIAN);
        this.vertexFloats = this.vertexBB.asFloatBuffer();
        this.uvOverlay = new Vector4(0f, 0f, 1f, 1f);
        this.uvAlpha = new Vector4(0f, 0f, 1f, 1f);
        this.uvFlag = new Vector4(0f, 0f, 1f, 1f);
    }

    public void initialize(Texture overlayTexture, Texture alphaTexture, Texture flagTexture) {
        VertexAttributes vertexAttributes = new VertexAttributes(
            new VertexAttribute(VertexAttributes.Usage.Position, 2, ShaderProgram.POSITION_ATTRIBUTE),
            new VertexAttribute(VertexAttributes.Usage.TextureCoordinates, 2, ShaderProgram.TEXCOORD_ATTRIBUTE)
        );

        this.uniformBufferSize = (16 + 4 * 3) * Float.BYTES;
        this.uniformBuffer = new WebGPUUniformBuffer(this.uniformBufferSize, WGPUBufferUsage.CopyDst.or(WGPUBufferUsage.Uniform));

        this.vertexBuffer = new WebGPUVertexBuffer(WGPUBufferUsage.CopyDst.or(WGPUBufferUsage.Vertex), 4 * vertexSize);
        this.indexBuffer = new WebGPUIndexBuffer(WGPUBufferUsage.CopyDst.or(WGPUBufferUsage.Index), 6 * Short.BYTES, Short.BYTES);

        fillIndexBuffer();

        this.binder = this.createBinder();
        this.pipeline = this.createPipeline(vertexAttributes, WgslUtils.getShaderSource("flag.wgsl"));
        this.bindStaticTextures(overlayTexture, alphaTexture, flagTexture);
    }

    private void fillIndexBuffer() {
        ByteBuffer bb = BufferUtils.newUnsafeByteBuffer(6 * Short.BYTES);
        bb.order(ByteOrder.LITTLE_ENDIAN);
        bb.asShortBuffer().put(new short[]{0, 1, 2, 0, 2, 3});
        bb.limit(6 * Short.BYTES);
        indexBuffer.setIndices(bb);
        BufferUtils.disposeUnsafeByteBuffer(bb);
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

    private void bindStaticTextures(Texture overlayTexture, Texture alphaTexture, Texture flagTexture) {
        this.binder.setTexture("textureOverlay", ((WgTexture) overlayTexture).getTextureView());
        this.binder.setSampler("textureOverlaySampler", ((WgTexture) overlayTexture).getSampler());
        this.binder.setTexture("textureAlpha", ((WgTexture) alphaTexture).getTextureView());
        this.binder.setSampler("textureAlphaSampler", ((WgTexture) alphaTexture).getSampler());
        this.binder.setTexture("textureFlag", ((WgTexture) flagTexture).getTextureView());
        this.binder.setSampler("textureFlagSampler", ((WgTexture) flagTexture).getSampler());
    }

    private void switchTexture(TextureRegion overlayRegion, TextureRegion alphaRegion, TextureRegion flagRegion) {
        this.binder.setUniform("uvOverlay", this.uvOverlay.set(overlayRegion.getU(), overlayRegion.getV(), overlayRegion.getU2(), overlayRegion.getV2()));
        this.binder.setUniform("uvAlpha", this.uvAlpha.set(alphaRegion.getU(), alphaRegion.getV(), alphaRegion.getU2(), alphaRegion.getV2()));
        this.binder.setUniform("uvFlag", this.uvFlag.set(flagRegion.getU(), flagRegion.getV(), flagRegion.getU2(), flagRegion.getV2()));
        this.uniformBuffer.flush();
    }

    public void setProjectionMatrix(Matrix4 projectionMatrix) {
        this.binder.setUniform("projTrans", projectionMatrix);
        this.uniformBuffer.flush();
    }

    private void createQuadVertices(float x, float y, float width, float height) {
        this.vertexFloats.clear();

        this.vertexFloats.put(x);
        this.vertexFloats.put(y);
        this.vertexFloats.put(0f);
        this.vertexFloats.put(1f);

        this.vertexFloats.put(x + width);
        this.vertexFloats.put(y);
        this.vertexFloats.put(1f);
        this.vertexFloats.put(1f);

        this.vertexFloats.put(x + width);
        this.vertexFloats.put(y + height);
        this.vertexFloats.put(1f);
        this.vertexFloats.put(0f);

        this.vertexFloats.put(x);
        this.vertexFloats.put(y + height);
        this.vertexFloats.put(0f);
        this.vertexFloats.put(0f);

        this.vertexFloats.flip();
    }

    public void addFlag(TextureRegion flagRegion, TextureRegion overlayRegion, TextureRegion alphaRegion, float x, float y, float width, float height) {
        this.flagInstances.add(new FlagInstance(flagRegion, overlayRegion, alphaRegion, x, y, width, height));
    }

    public void render() {
        if (this.flagInstances.isEmpty()) {
            return;
        }

        WebGPURenderPass pass = RenderPassBuilder.create("Flag batch pass");
        pass.setPipeline(this.pipeline);
        this.binder.bindGroup(pass, 0);
        pass.setIndexBuffer(this.indexBuffer.getBuffer(), WGPUIndexFormat.Uint16, 0, 6 * Short.BYTES);

        for (FlagInstance instance : this.flagInstances) {
            switchTexture(instance.overlayRegion, instance.alphaRegion, instance.flagRegion);
            createQuadVertices(instance.x, instance.y, instance.width, instance.height);
            this.vertexBuffer.setVertices(this.vertexBB, 0, 4 * this.vertexSize);
            pass.setVertexBuffer(0, this.vertexBuffer.getBuffer(), 0, 4 * this.vertexSize);
            pass.drawIndexed(6, 1, 0, 0, 0);
        }

        pass.end();
        this.flagInstances.clear();
    }

    @Override
    public void dispose() {
        this.binder.dispose();
        this.vertexBuffer.dispose();
        this.indexBuffer.dispose();
        this.uniformBuffer.dispose();
        BufferUtils.disposeUnsafeByteBuffer(this.vertexBB);
    }
}
