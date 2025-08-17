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

// Constants terrain
const MAP_SIZE: vec2<f32> = vec2<f32>(5616.0, 2160.0);
const XX: f32 = 1.0 / 5616.0;
const YY: f32 = 1.0 / 2160.0;
const PIX: vec2<f32> = vec2<f32>(XX, YY);

// Constants hqx
const ML: i32 = 0;
const THRESHOLD: f32 = 0.02;
const AA_SCALE: f32 = 18.0;
const MAIN_LINE_THICKNESS: f32 = 0.38;
const SUB_LINE_THICKNESS: f32 = 0.22;

// Constants border
const OFFSETS_PROVINCE: array<vec2<f32>, 4> = array<vec2<f32>, 4>(
    vec2<f32>(0.1, 0.0),
    vec2<f32>(-0.1, 0.0),
    vec2<f32>(0.0, 0.1),
    vec2<f32>(0.0, -0.1)
);

const OFFSETS_REGION: array<vec2<f32>, 4> = array<vec2<f32>, 4>(
    vec2<f32>(0.2, 0.0),
    vec2<f32>(-0.25, 0.0),
    vec2<f32>(0.0, 0.25),
    vec2<f32>(0.0, -0.25)
);

const OFFSETS_COUNTRY: array<vec2<f32>, 4> = array<vec2<f32>, 4>(
    vec2<f32>(0.35, 0.0),
    vec2<f32>(-0.35, 0.0),
    vec2<f32>(0.0, 0.35),
    vec2<f32>(0.0, -0.35)
);

const GREYIFY: vec3<f32> = vec3<f32>(0.212671, 0.715160, 0.072169);

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

fn getCorrectedTexCoord(texCoord: vec2<f32>) -> vec2<f32> {
    return vec2<f32>(texCoord.x, 1.0 - texCoord.y);
}

fn diag(sum: ptr<function, vec4<f32>>, uv: vec2<f32>, p1: vec2<f32>, p2: vec2<f32>, texture: texture_2d<f32>, lineThickness: f32) -> bool {
    let v1: vec4<f32> = textureLoad(texture, vec2<i32>(uv + p1), ML);
    let v2: vec4<f32> = textureLoad(texture, vec2<i32>(uv + p2), ML);

    if (length(v1 - v2) < THRESHOLD) {
        let dir: vec2<f32> = normalize(vec2<f32>((p2 - p1).y, -(p2 - p1).x));
        let lp: vec2<f32> = uv - (floor(uv + p1) + 0.5);
        let l: f32 = clamp((lineThickness - dot(lp, dir)) * AA_SCALE, 0.0, 1.0);

        if (l > 0.5) {
            *sum = v1;
        }

        return true;
    }
    return false;
}

fn hqxFilter(uv: vec2<f32>, texture: texture_2d<f32>) -> vec4<f32> {
    var sum: vec4<f32> = textureLoad(texture, vec2<i32>(uv), ML);

    if (diag(&sum, uv, vec2<f32>(-1.0, 0.0), vec2<f32>(0.0, 1.0), texture, MAIN_LINE_THICKNESS)) {
        diag(&sum, uv, vec2<f32>(-1.0, 0.0), vec2<f32>(1.0, 1.0), texture, SUB_LINE_THICKNESS);
        diag(&sum, uv, vec2<f32>(-1.0, -1.0), vec2<f32>(0.0, 1.0), texture, SUB_LINE_THICKNESS);
    }

    if (diag(&sum, uv, vec2<f32>(0.0, 1.0), vec2<f32>(1.0, 0.0), texture, MAIN_LINE_THICKNESS)) {
        diag(&sum, uv, vec2<f32>(0.0, 1.0), vec2<f32>(1.0, -1.0), texture, SUB_LINE_THICKNESS);
        diag(&sum, uv, vec2<f32>(-1.0, 1.0), vec2<f32>(1.0, 0.0), texture, SUB_LINE_THICKNESS);
    }

    if (diag(&sum, uv, vec2<f32>(1.0, 0.0), vec2<f32>(0.0, -1.0), texture, MAIN_LINE_THICKNESS)) {
        diag(&sum, uv, vec2<f32>(1.0, 0.0), vec2<f32>(-1.0, -1.0), texture, SUB_LINE_THICKNESS);
        diag(&sum, uv, vec2<f32>(1.0, 1.0), vec2<f32>(0.0, -1.0), texture, SUB_LINE_THICKNESS);
    }

    if (diag(&sum, uv, vec2<f32>(0.0, -1.0), vec2<f32>(-1.0, 0.0), texture, MAIN_LINE_THICKNESS)) {
        diag(&sum, uv, vec2<f32>(0.0, -1.0), vec2<f32>(-1.0, 1.0), texture, SUB_LINE_THICKNESS);
        diag(&sum, uv, vec2<f32>(1.0, -1.0), vec2<f32>(-1.0, 0.0), texture, SUB_LINE_THICKNESS);
    }

    return sum;
}

fn getWaterClose(texCoord: vec2<f32>) -> vec4<f32> {
    let wrap: f32 = 0.8;
    let waveModOne: f32 = 3.0;
    let waveModTwo: f32 = 4.0;
    let specValueOne: f32 = 20.0;
    let specValueTwo: f32 = 5.0;
    let vWaterTransparens: f32 = 0.6;
    let vColorMapFactor: f32 = 0.5;

    var waterColor: vec4<f32> = vec4<f32>(0.22, 0.3, 0.45, 1.0);
    let worldColorColor: vec3<f32> = textureSample(textureColorMapWater, textureColorMapWaterSampler, texCoord).rgb;

    var modifiedTexCoord: vec2<f32> = texCoord * 100.0;
    modifiedTexCoord = modifiedTexCoord * 0.25 + uniforms.time * 0.002;

    let eyeDirection: vec3<f32> = vec3<f32>(0.0, 1.0, 1.0);
    let lightDirection: vec3<f32> = vec3<f32>(0.0, 1.0, 1.0);

    let coordA: vec2<f32> = modifiedTexCoord * 3.0 + vec2<f32>(0.10, 0.10);
    var coordB: vec2<f32> = modifiedTexCoord * 1.0 + vec2<f32>(0.00, 0.10);
    var coordC: vec2<f32> = modifiedTexCoord * 2.0 + vec2<f32>(0.00, 0.15);
    var coordD: vec2<f32> = modifiedTexCoord * 5.0 + vec2<f32>(0.00, 0.30);

    let vBumpA: vec4<f32> = textureSample(textureWaterNormal, textureWaterNormalSampler, coordA);
    coordB += vec2<f32>(0.03, 0.05) * uniforms.time;
    let vBumpB: vec4<f32> = textureSample(textureWaterNormal, textureWaterNormalSampler, coordB);
    coordC += vec2<f32>(0.02, 0.07) * uniforms.time;
    let vBumpC: vec4<f32> = textureSample(textureWaterNormal, textureWaterNormalSampler, coordC);
    coordD += vec2<f32>(0.06, 0.02) * uniforms.time;
    let vBumpD: vec4<f32> = textureSample(textureWaterNormal, textureWaterNormalSampler, coordD);

    let vBumpTex: vec3<f32> = normalize(waveModOne * (vBumpA.xyz + vBumpB.xyz + vBumpC.xyz + vBumpD.xyz) - waveModTwo);

    let eyeDir: vec3<f32> = normalize(eyeDirection);
    var ndotL: f32 = max(dot(eyeDir, (vBumpTex / 2.0)), 0.0);

    ndotL = clamp((ndotL + wrap) / (1.0 + wrap), 0.0, 1.0);
    ndotL = mix(ndotL, 1.0, 0.0);

    var color: vec3<f32> = ndotL * (worldColorColor * vColorMapFactor);

    let reflVector: vec3<f32> = -reflect(lightDirection, vBumpTex);
    var specular: f32 = dot(normalize(reflVector), eyeDir);
    specular = clamp(specular, 0.0, 1.0);

    specular = pow(specular, specValueOne);
    color += (specular / specValueTwo);

    waterColor = mix(waterColor, vec4<f32>(color, 1.0), vWaterTransparens);

    return waterColor;
}

fn getWaterFar(texCoord: vec2<f32>) -> vec4<f32> {
    let waterColor: vec3<f32> = textureSample(textureColorMapWater, textureColorMapWaterSampler, texCoord).rgb;
    var overlayColor: vec4<f32> = textureSample(textureOverlayTile, textureOverlayTileSampler, texCoord * vec2(11.0, 11.0 * MAP_SIZE.y / MAP_SIZE.x));
    if (overlayColor.r < 0.5) {
        overlayColor = vec4(2.0 * overlayColor.r * waterColor.r,2.0 * overlayColor.g * waterColor.g,2.0 * overlayColor.b * waterColor.b,overlayColor.a);
    } else {
        overlayColor = vec4(1.0 - 2.0 * (1.0 - overlayColor.r) * (1.0 - waterColor.r),1.0 - 2.0 * (1.0 - overlayColor.g) * (1.0 - waterColor.g),1.0 - 2.0 * (1.0 - overlayColor.b) * (1.0 - waterColor.b),overlayColor.a);
    }

    return overlayColor;
}

@fragment
fn fs_main(input: VertexOutput) -> @location(0) vec4<f32> {
    let texCoord: vec2<f32> = getCorrectedTexCoord(input.texCoord);
    let uv: vec2<f32> = texCoord * MAP_SIZE;
    let colorProvince: vec4<f32> = hqxFilter(uv, textureProvinces);
    /*if (uniforms.zoom > 0.8) {
        return getWaterFar(input.texCoord);
    } else {
        return getWaterClose(input.texCoord);
    }*/
    return colorProvince;
}
