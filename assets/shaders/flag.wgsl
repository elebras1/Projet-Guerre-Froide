struct Uniforms {
    uvFlag: vec4<f32>,
    uvOverlay: vec4<f32>,
    uvAlpha: vec4<f32>,
}

struct VertexInput {
    @location(0) position: vec3<f32>,
    @location(1) texCoord: vec2<f32>
}

struct VertexOutput {
    @builtin(position) position: vec4<f32>,
    @location(0) texCoord: vec2<f32>
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

    output.position = vec4<f32>(input.position, 1.0);
    output.texCoord = input.texCoord;

    return output;
}

@fragment
fn fs_main(input: VertexOutput) -> @location(0) vec4<f32> {
    let uvFlag = mix(uniforms.uvFlag.xy, uniforms.uvFlag.zw, input.texCoord);
    let uvOverlay = mix(uniforms.uvOverlay.xy, uniforms.uvOverlay.zw, input.texCoord);
    let uvAlpha = mix(uniforms.uvAlpha.xy, uniforms.uvAlpha.zw, input.texCoord);

    let flagColor = textureSample(textureFlag, textureFlagSampler, uvFlag);
    let overlayColor = textureSample(textureOverlay, textureOverlaySampler, uvOverlay);
    let alphaColor = textureSample(textureAlpha, textureAlphaSampler, uvAlpha);

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
