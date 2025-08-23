struct Uniforms {
    projTrans: mat4x4<f32>,
    worldWidth: f32,
    time: f32,
    zoom: f32,
}

struct VertexInput {
    @location(0) position: vec2<f32>,
    @location(1) texCoord: vec2<f32>,
    @location(2) width: f32,
    @builtin(instance_index) instanceIndex: u32,
}

struct VertexOutput {
    @builtin(position) position: vec4<f32>,
    @location(0) texCoord: vec2<f32>,
    @location(1) width: f32,
    @location(2) worldCoord: vec2<f32>,
}

@group(0) @binding(0) var<uniform> uniforms: Uniforms;
@group(0) @binding(1) var texture: texture_2d<f32>;
@group(0) @binding(2) var textureSampler: sampler;
@group(0) @binding(3) var textureColorMapWater: texture_2d<f32>;
@group(0) @binding(4) var textureColorMapWaterSampler: sampler;

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
    output.width = input.width;
    output.worldCoord = vec2<f32>(input.position.x + instanceOffset, input.position.y);

    return output;
}

@fragment
fn fs_main(input: VertexOutput) -> @location(0) vec4<f32> {
    let scrolledCoords: vec2<f32> = vec2<f32>(input.texCoord.x, input.texCoord.y - uniforms.time);
    var fragColor = textureSample(texture, textureSampler, scrolledCoords);
    fragColor *= textureSample(textureColorMapWater, textureColorMapWaterSampler, input.worldCoord);
    fragColor = vec4<f32>(vec3<f32>(1.0) - vec3<f32>(0.8) * (vec3<f32>(1.0) - fragColor.rgb), fragColor.a);

    let zoomBlend = clamp((uniforms.zoom - 0.8) * 0.37037, 0.0, 1.0);

    let widthThreshold = mix(0.25, 0.9, zoomBlend);
    let visibilityMask = select(1.0, step(widthThreshold, input.width), uniforms.zoom > 0.8);

    fragColor = vec4<f32>(fragColor.rgb, fragColor.a * visibilityMask);

    return fragColor;
}
