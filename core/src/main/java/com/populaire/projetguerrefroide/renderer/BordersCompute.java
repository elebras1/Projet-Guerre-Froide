package com.populaire.projetguerrefroide.renderer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.BufferUtils;
import com.github.xpenatan.webgpu.*;
import com.monstrous.gdx.webgpu.application.WebGPUContext;
import com.monstrous.gdx.webgpu.application.WgGraphics;
import com.monstrous.gdx.webgpu.graphics.WgShaderProgram;
import com.monstrous.gdx.webgpu.wrappers.*;
import com.populaire.projetguerrefroide.util.WgslUtils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.function.Consumer;

import static com.populaire.projetguerrefroide.ProjetGuerreFroide.WORLD_HEIGHT;
import static com.populaire.projetguerrefroide.ProjetGuerreFroide.WORLD_WIDTH;

public class BordersCompute {
    private static final int PIXELS_BUFFER_SIZE = WORLD_WIDTH * WORLD_HEIGHT * 4;
    private static final int PROVINCE_BUFFER_SIZE = 65536 * 8;
    private final WebGPUComputePipeline computePipeline;
    private final WebGPUContext webgpu;
    private final WebGPUBuffer provincesBuffer;
    private final WebGPUBuffer storageBuffer;
    private final WebGPUBuffer provincesBufferOut;
    private final WebGPUBuffer readBuffer;
    private final WebGPUBindGroup bindGroup;
    private final ByteBuffer cachedCpuBuffer;

    public BordersCompute(ByteBuffer initialPixels) {
        this.webgpu = ((WgGraphics) Gdx.graphics).getContext();
        this.cachedCpuBuffer = BufferUtils.newByteBuffer(PIXELS_BUFFER_SIZE);
        this.cachedCpuBuffer.order(ByteOrder.LITTLE_ENDIAN);
        this.provincesBuffer = new WebGPUBuffer("In Pixels", WGPUBufferUsage.Storage.or(WGPUBufferUsage.CopyDst), PIXELS_BUFFER_SIZE);
        this.provincesBuffer.write(0, initialPixels);
        this.storageBuffer = new WebGPUBuffer("Data Provinces", WGPUBufferUsage.Storage.or(WGPUBufferUsage.CopyDst), PROVINCE_BUFFER_SIZE);
        this.provincesBufferOut = new WebGPUBuffer("Out Pixels", WGPUBufferUsage.Storage.or(WGPUBufferUsage.CopySrc), PIXELS_BUFFER_SIZE);
        this.readBuffer = new WebGPUBuffer("Read Buffer", WGPUBufferUsage.MapRead.or(WGPUBufferUsage.CopyDst), PIXELS_BUFFER_SIZE);
        WgShaderProgram shader = WgslUtils.getShader("borders.wgsl");
        WebGPUBindGroupLayout layout = this.createBindGroupLayout();
        this.bindGroup = this.createBindGroup(layout);
        WebGPUPipelineLayout pipelineLayout = new WebGPUPipelineLayout("map layout", layout);
        this.computePipeline = new WebGPUComputePipeline(shader, "compute", pipelineLayout);
    }

    private WebGPUBindGroupLayout createBindGroupLayout() {
        WebGPUBindGroupLayout layout = new WebGPUBindGroupLayout();
        layout.begin();
        layout.addBuffer(0, WGPUShaderStage.Compute, WGPUBufferBindingType.ReadOnlyStorage, PIXELS_BUFFER_SIZE, false);
        layout.addBuffer(1, WGPUShaderStage.Compute, WGPUBufferBindingType.ReadOnlyStorage, PROVINCE_BUFFER_SIZE, false);
        layout.addBuffer(2, WGPUShaderStage.Compute, WGPUBufferBindingType.Storage, PIXELS_BUFFER_SIZE, false);
        layout.end();
        return layout;
    }

    private WebGPUBindGroup createBindGroup(WebGPUBindGroupLayout layout) {
        WebGPUBindGroup bindGroup = new WebGPUBindGroup(layout);
        bindGroup.begin();
        bindGroup.setBuffer(0, this.provincesBuffer);
        bindGroup.setBuffer(1, this.storageBuffer);
        bindGroup.setBuffer(2, this.provincesBufferOut);
        bindGroup.end();
        return bindGroup;
    }

    public void updateAndRunAsync(ByteBuffer dataProvinces, Consumer<ByteBuffer> onResultReady) {
        this.webgpu.device.getQueue().writeBuffer(this.storageBuffer.getBuffer(), 0, dataProvinces, PROVINCE_BUFFER_SIZE);
        WGPUCommandEncoder encoder = WGPUCommandEncoder.obtain();
        WGPUCommandEncoderDescriptor encoderDesc = WGPUCommandEncoderDescriptor.obtain();
        this.webgpu.device.createCommandEncoder(encoderDesc, encoder);
        WGPUComputePassEncoder pass = new WGPUComputePassEncoder();
        WGPUComputePassDescriptor passDesc = WGPUComputePassDescriptor.obtain();

        encoder.beginComputePass(passDesc, pass);
        pass.setPipeline(this.computePipeline.getPipeline());
        pass.setBindGroup(0, this.bindGroup.getBindGroup(), WGPUVectorInt.NULL);
        pass.setDispatchWorkgroups((WORLD_WIDTH + 7) / 8, (WORLD_HEIGHT + 7) / 8, 1);
        pass.end();

        encoder.copyBufferToBuffer(this.provincesBufferOut.getBuffer(), 0, this.readBuffer.getBuffer(), 0, PIXELS_BUFFER_SIZE);

        WGPUCommandBuffer commandBuffer = WGPUCommandBuffer.obtain();
        encoder.finish(WGPUCommandBufferDescriptor.obtain(), commandBuffer);
        this.webgpu.device.getQueue().submit(commandBuffer);
        commandBuffer.release();

        this.readBuffer.getBuffer().mapAsync(WGPUMapMode.Read, 0, PIXELS_BUFFER_SIZE, WGPUCallbackMode.AllowProcessEvents, new WGPUBufferMapCallback() {
            @Override
            protected void onCallback(WGPUMapAsyncStatus status, String message) {
                if (status == WGPUMapAsyncStatus.Success) {
                    cachedCpuBuffer.clear();
                    readBuffer.getBuffer().getConstMappedRange(0, PIXELS_BUFFER_SIZE, cachedCpuBuffer);
                    cachedCpuBuffer.position(0);
                    cachedCpuBuffer.limit(PIXELS_BUFFER_SIZE);
                    readBuffer.getBuffer().unmap();
                    onResultReady.accept(cachedCpuBuffer);
                } else {
                    Gdx.app.error("WebGPU", "MapAsync error: " + status + " Msg: " + message);
                }
            }
        });
    }
}
