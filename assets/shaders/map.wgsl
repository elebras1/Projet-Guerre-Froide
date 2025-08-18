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

fn getTerrain(corner: vec2<f32>, texCoord: vec2<f32>, localTexCoord: vec2<f32>) -> vec4<f32> {
    let index: f32 = textureSample(textureTerrain, textureTerrainSampler, floor(texCoord * MAP_SIZE + vec2<f32>(0.5, 0.5)) / MAP_SIZE + 0.5 * PIX * corner).r;
    let flooredIndex: f32 = floor(index * 256.0);
    let colour: vec4<f32> = textureSample(textureTerrainsheet, textureTerrainsheetSampler, localTexCoord, i32(flooredIndex));
    return colour;
}

fn getTerrainMix(texCoord: vec2<f32>) -> vec4<f32> {
    let scaling: vec2<f32> = fract(texCoord * MAP_SIZE + vec2<f32>(0.5, 0.5));
    let localTexCoord: vec2<f32> = fract(texCoord * MAP_SIZE / 16.0);

    let colourlu: vec4<f32> = getTerrain(vec2<f32>(-1.0, -1.0), texCoord, localTexCoord);
    let colourld: vec4<f32> = getTerrain(vec2<f32>(-1.0, 1.0), texCoord, localTexCoord);
    let colourru: vec4<f32> = getTerrain(vec2<f32>(1.0, -1.0), texCoord, localTexCoord);
    let colourrd: vec4<f32> = getTerrain(vec2<f32>(1.0, 1.0), texCoord, localTexCoord);

    let colour_u: vec4<f32> = mix(colourlu, colourru, scaling.x);
    let colour_d: vec4<f32> = mix(colourld, colourrd, scaling.x);
    var terrain: vec4<f32> = mix(colour_u, colour_d, scaling.y);

    let terrainBackground: vec4<f32> = textureSample(textureColormap, textureColormapSampler, texCoord);
    terrain = vec4<f32>((terrain.rgb * 2.0 + terrainBackground.rgb) / 3.0, terrain.a);
    return terrain;
}

fn getBorder(filteredColor: vec4<f32>, offsets: array<vec2<f32>, 4>, color: vec3<f32>, uv: vec2<f32>) -> vec4<f32> {
    let thresholdSquared: f32 = THRESHOLD * THRESHOLD;
    for (var i: i32 = 0; i < 4; i++) {
        let filteredNeighbor: vec4<f32> = hqxFilter(uv + offsets[i], textureProvinces);
        let deltaColor: vec3<f32> = filteredColor.rgb - filteredNeighbor.rgb;
        if (dot(deltaColor, deltaColor) > thresholdSquared) {
            return vec4<f32>(color, 1.0);
        }
    }

    return vec4<f32>(0.0, 0.0, 0.0, 0.0);
}

fn getLandClose(colorProvince: vec4<f32>, colorMapMode: vec4<f32>, texCoord: vec2<f32>, uv: vec2<f32>) -> vec4<f32> {
    var terrain: vec4<f32> = getTerrainMix(texCoord);
    var political: vec3<f32> = colorMapMode.rgb;

    if (uniforms.showTerrain == 1) {
        political = terrain.rgb;
    }

    let grey: f32 = dot(terrain.rgb, GREYIFY);
    terrain = vec4<f32>(vec3<f32>(grey), terrain.a);

    let deltaColor: vec3<f32> = colorProvince.rgb - uniforms.colorProvinceSelected.rgb;
    if (dot(deltaColor, deltaColor) < 0.00000001) {
        political += vec3<f32>(0.20);
        political = clamp(political, vec3<f32>(0.0), vec3<f32>(1.0));
        political *= 1.3;
        political = mix(political, vec3<f32>(grey), 0.5);
    }

    let stripeColor: vec4<f32> = textureSample(textureProvincesStripes, textureProvincesStripesSampler, colorProvince.rg);
    if (stripeColor.a > 0.0) {
        let stripeCoord: vec2<f32> = fract(texCoord * vec2<f32>(512.0, 512.0 * MAP_SIZE.y / MAP_SIZE.x));
        let stripeFactor: f32 = textureSample(textureStripes, textureStripesSampler, stripeCoord).a;
        political = clamp(mix(political, stripeColor.rgb, stripeFactor), vec3<f32>(0.0), vec3<f32>(1.0));
    }

    if (colorProvince.a < 1.0) {
        var border: vec4<f32> = vec4<f32>(0.0);
        if (colorProvince.a < 0.1) {
            if (uniforms.zoom < 0.55) {
                border = getBorder(colorProvince, OFFSETS_PROVINCE, vec3<f32>(0.13, 0.16, 0.20), uv);
            }
        } else if (colorProvince.a < 0.4) {
            border = getBorder(colorProvince, OFFSETS_REGION, vec3<f32>(0.13, 0.16, 0.20), uv);
        } else if (colorProvince.a < 0.7) {
            border = getBorder(colorProvince, OFFSETS_COUNTRY, vec3<f32>(0.74, 0.26, 0.22), uv);
        }
        political = mix(political, border.rgb, step(0.01, border.a));
    }

    political = political - vec3<f32>(0.7);
    terrain = vec4<f32>(mix(terrain.rgb, political, 0.3), terrain.a);
    terrain = vec4<f32>(terrain.rgb * 1.5, colorMapMode.a);
    return terrain;
}

fn getLandFar(colorProvince: vec4<f32>, colorMapMode: vec4<f32>, texCoord: vec2<f32>, uv: vec2<f32>) -> vec4<f32> {
    var political: vec4<f32> = colorMapMode;

    if (uniforms.showTerrain == 1) {
        political = getTerrainMix(texCoord);
    }

    let deltaColor: vec3<f32> = colorProvince.rgb - uniforms.colorProvinceSelected.rgb;
    if (dot(deltaColor, deltaColor) < 0.00000001) {
        political = vec4<f32>(political.rgb + vec3<f32>(0.20), political.a);
        political = vec4<f32>(clamp(political.rgb, vec3<f32>(0.0), vec3<f32>(1.0)), political.a);
        political = vec4<f32>(political.rgb * 1.3, political.a);
        let grey: f32 = dot(political.rgb, GREYIFY);
        political = vec4<f32>(mix(political.rgb, vec3<f32>(grey), 0.5), political.a);
    }

    let stripeColor: vec4<f32> = textureSample(textureProvincesStripes, textureProvincesStripesSampler, colorProvince.rg);
    if (stripeColor.a > 0.0) {
        let stripeCoord: vec2<f32> = fract(texCoord * vec2<f32>(512.0, 512.0 * MAP_SIZE.y / MAP_SIZE.x));
        let stripeFactor: f32 = textureSample(textureStripes, textureStripesSampler, stripeCoord).a;
        political = vec4<f32>(clamp(mix(political.rgb, stripeColor.rgb, stripeFactor), vec3<f32>(0.0), vec3<f32>(1.0)), political.a);
    }

    let overlayColor: vec4<f32> = textureSample(textureOverlayTile, textureOverlayTileSampler, texCoord * vec2<f32>(11.0, 11.0 * MAP_SIZE.y / MAP_SIZE.x));
    var finalPolitical: vec3<f32>;
    if (overlayColor.r < 0.5) {
        finalPolitical = 2.0 * overlayColor.rgb * political.rgb;
    } else {
        finalPolitical = vec3<f32>(1.0) - 2.0 * (vec3<f32>(1.0) - overlayColor.rgb) * (vec3<f32>(1.0) - political.rgb);
    }

    let grey: f32 = dot(colorMapMode.rgb, GREYIFY);
    let finalColor: vec3<f32> = mix(finalPolitical, vec3<f32>(grey), 0.3);

    return vec4<f32>(finalColor, colorMapMode.a);
}

@fragment
fn fs_main(input: VertexOutput) -> @location(0) vec4<f32> {
    let uv: vec2<f32> = input.texCoord * MAP_SIZE;
    let colorProvince: vec4<f32> = hqxFilter(uv, textureProvinces);

    let dims = vec2<f32>(textureDimensions(textureMapMode, 0u));
    let coord = clamp(colorProvince.rg * (dims - 1.0) + 0.5, vec2<f32>(0.0), dims - 1.0);
    let texel = vec2<i32>(floor(coord));

    let colorMapMode: vec4<f32> = textureLoad(textureMapMode, texel, 0);

    let isLand: bool = colorMapMode.a > 0.0;

    var terrain: vec4<f32> = vec4<f32>(0.0, 0.0, 0.0, 0.0);
    var water: vec4<f32> = vec4<f32>(0.0, 0.0, 0.0, 0.0);

    if (uniforms.zoom > 0.8) {
        if(isLand) {
            terrain = getLandFar(colorProvince, colorMapMode, input.texCoord, uv);
        } else {
            water = getWaterFar(input.texCoord);
        }
    } else {
        if(isLand) {
            terrain = getLandClose(colorProvince, colorMapMode, input.texCoord, uv);
        } else {
            water = getWaterClose(input.texCoord);
        }
    }

    let fragColor: vec4<f32> = vec4<f32>(mix(water.rgb, terrain.rgb, terrain.a), 1.0);

    return fragColor;
}
