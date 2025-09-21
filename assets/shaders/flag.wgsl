struct Uniforms {
    projTrans: mat4x4<f32>,
}

struct VertexInput {
    @location(0) position: vec2<f32>,
    @location(1) texCoord: vec2<f32>,
    @location(2) uvOverlay: vec2<f32>,
    @location(3) uvAlpha: vec2<f32>,
    @location(4) uvFlag: vec2<f32>
}

struct VertexOutput {
    @builtin(position) position: vec4<f32>,
    @location(0) texCoord: vec2<f32>,
    @location(1) uvOverlay: vec2<f32>,
    @location(2) uvAlpha: vec2<f32>,
    @location(3) uvFlag: vec2<f32>
}

@group(0) @binding(0) var<uniform> uniforms: Uniforms;
@group(0) @binding(1) var textureFlag: texture_2d<f32>;
@group(0) @binding(2) var textureFlagSampler: sampler;
@group(0) @binding(3) var textureOverlay: texture_2d<f32>;
@group(0) @binding(4) var textureOverlaySampler: sampler;
@group(0) @binding(5) var textureAlpha: texture_2d<f32>;
@group(0) @binding(6) var textureAlphaSampler: sampler;

@vertex
fn vs_main(input: VertexInput) -> VertexOutput {
    var output: VertexOutput;

    output.position = uniforms.projTrans * vec4<f32>(input.position, 0.0, 1.0);
    output.texCoord = input.texCoord;
    output.uvOverlay = input.uvOverlay;
    output.uvAlpha = input.uvAlpha;
    output.uvFlag = input.uvFlag;

    return output;
}

@fragment
fn fs_main(input: VertexOutput) -> @location(0) vec4<f32> {
    let flagColor = textureSample(textureFlag, textureFlagSampler, input.uvFlag);
    var overlayColor = textureSample(textureOverlay, textureOverlaySampler, input.uvOverlay);
    overlayColor = vec4<f32>(overlayColor.rgb, overlayColor.a * 1.5);
    let alphaColor = textureSample(textureAlpha, textureAlphaSampler, input.uvAlpha);

    let color = mix(flagColor, overlayColor, overlayColor.a);
    var fragColor = vec4<f32>(color.rgb, alphaColor.a);
    if(alphaColor.a < 1) {
        fragColor = overlayColor;
    } else {
        fragColor = mix(alphaColor, color, alphaColor.a);
        if (fragColor.a > 0.0) {
            fragColor.a = 1.0;
        }
    }

    return fragColor;
}
