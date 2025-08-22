struct Uniforms {
    projTrans: mat4x4<f32>,
    worldWidth: f32,
    zoom: f32,
}

struct VertexInput {
    @location(0) position: vec2<f32>,
    @location(1) texCoord: vec2<f32>,
    @builtin(instance_index) instanceIndex: u32,
}

struct VertexOutput {
    @builtin(position) position: vec4<f32>,
    @location(0) texCoord: vec2<f32>,
    @location(1) opacity: f32,
}

@group(0) @binding(0) var<uniform> uniforms: Uniforms;
@group(0) @binding(1) var texture: texture_2d<f32>;
@group(0) @binding(2) var textureSampler: sampler;

const thickness: f32 = 0.04;

@vertex
fn vs_main(input: VertexInput) -> VertexOutput {
    var output: VertexOutput;

    var instanceOffset: f32 = 0.0;
    if (input.instanceIndex == 1u) {
        instanceOffset = -uniforms.worldWidth;
    } else if (input.instanceIndex == 2u) {
        instanceOffset = uniforms.worldWidth;
    }

    output.position = uniforms.projTrans * vec4<f32>(input.position + vec2<f32>(instanceOffset, 0.0), 0.0, 1.0);
    output.texCoord = input.texCoord;
    output.opacity = 1.0;
    if (uniforms.zoom < 0.4) {
        let x: f32 = uniforms.zoom * 50.0 - 1.0 / thickness;
        output.opacity = exp(-(x * x) * 0.01);
    }

    return output;
}

@fragment
fn fs_main(input: VertexOutput) -> @location(0) vec4<f32> {
    let color = textureSample(texture, textureSampler, input.texCoord);
    return vec4(color.rgb, color.a * input.opacity);
}
