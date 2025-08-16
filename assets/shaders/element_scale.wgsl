struct Uniforms {
    projTrans: mat4x4<f32>,
    worldWidth: f32,
    zoom: f32,
}

struct VertexInput {
    @location(0) position: vec2<f32>,
    @location(1) texCoord: vec2<f32>,
    @location(2) center: vec2<f32>,
    @builtin(instance_index) instanceIndex: u32,
}

struct VertexOutput {
    @builtin(position) position: vec4<f32>,
    @location(0) texCoord: vec2<f32>,
}

@group(0) @binding(0) var<uniform> uniforms: Uniforms;
@group(0) @binding(1) var texture: texture_2d<f32>;
@group(0) @binding(2) var textureSampler: sampler;

@vertex
fn vs_main(input: VertexInput) -> VertexOutput {
    var output: VertexOutput;

    let offset: vec2<f32> = input.position - input.center;
    let scaledPosition: vec2<f32> = input.center + offset * uniforms.zoom;

    var instanceOffset: f32 = 0.0;
    if (input.instanceIndex == 1u) {
        instanceOffset = -uniforms.worldWidth;
    } else if (input.instanceIndex == 2u) {
        instanceOffset = uniforms.worldWidth;
    }

    let finalPosition: vec4<f32> = vec4(scaledPosition.x + instanceOffset, scaledPosition.y, 0.0, 1.0);
    output.position = uniforms.projTrans * finalPosition;
    output.texCoord = input.texCoord;

    return output;
}


@fragment
fn fs_main(input: VertexOutput) -> @location(0) vec4<f32> {
    return textureSample(texture, textureSampler, input.texCoord);
}
