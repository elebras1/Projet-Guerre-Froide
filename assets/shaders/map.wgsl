struct Uniforms {
    projTrans: mat4x4<f32>,
    worldWidth: f32,
    zoom: f32,
    time: f32,
    showTerrain: i32,
    colorProvinceSelected: vec4<f32>,
}

struct VertexInput {
    @location(0) position: vec2<f32>,
    @location(1) texCoord: vec2<f32>,
    @builtin(instance_index) instanceIndex: u32,
}

struct VertexOutput {
    @builtin(position) position: vec4<f32>,
    @location(0) texCoord: vec2<f32>,
}

@group(0) @binding(0) var<uniform> uniforms: Uniforms;
@group(0) @binding(1) var textureProvinces: texture_2d<f32>;
@group(0) @binding(2) var textureProvincesSampler: sampler;
@group(0) @binding(3) var textureMapMode: texture_2d<f32>;
@group(0) @binding(4) var textureMapModeSampler: sampler;
@group(0) @binding(5) var textureColorMapWater: texture_2d<f32>;
@group(0) @binding(6) var textureColorMapWaterSampler: sampler;
@group(0) @binding(7) var textureWaterNormal: texture_2d<f32>;
@group(0) @binding(8) var textureWaterNormalSampler: sampler;
@group(0) @binding(9) var textureTerrain: texture_2d<f32>;
@group(0) @binding(10) var textureTerrainSampler: sampler;
@group(0) @binding(11) var textureTerrainsheet: texture_2d_array<f32>;
@group(0) @binding(12) var textureTerrainsheetSampler: sampler;
@group(0) @binding(13) var textureColormap: texture_2d<f32>;
@group(0) @binding(14) var textureColormapSampler: sampler;
@group(0) @binding(15) var textureProvincesStripes: texture_2d<f32>;
@group(0) @binding(16) var textureProvincesStripesSampler: sampler;
@group(0) @binding(17) var textureStripes: texture_2d<f32>;
@group(0) @binding(18) var textureStripesSampler: sampler;
@group(0) @binding(19) var textureOverlayTile: texture_2d<f32>;
@group(0) @binding(20) var textureOverlayTileSampler: sampler;

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

    return output;
}

@fragment
fn fs_main(input: VertexOutput) -> @location(0) vec4<f32> {
    return vec4<f32>(1.0, 0.0, 0.0, 1.0);
}
