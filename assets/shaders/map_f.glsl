/*This shader is derived from the map shader of the "Project Alice" : https://github.com/schombert/Project-Alice,
Licensed under the GNU General Public License v3.0. You can redistribute it and/or modify it under the terms of the GPL v3.0.
A copy of the GNU GPL v3.0 should have been included with this project. If not, see <https://www.gnu.org/licenses/>.*/
#version 420

in vec2 v_texCoords;

layout (binding = 0) uniform sampler2D u_textureProvinces;
layout (binding = 1) uniform sampler2D u_textureCountries;
layout (binding = 2) uniform sampler2D u_textureColorMapWater;
layout (binding = 3) uniform sampler2D u_textureWaterNormal;
layout (binding = 4) uniform sampler2D u_textureTerrain;
layout (binding = 5) uniform sampler2DArray u_textureTerrainsheet;
layout (binding = 6) uniform sampler2D u_textureColormap;
layout (binding = 7) uniform sampler2D u_textureProvincesStripes;
layout (binding = 8) uniform sampler2D u_textureStripes;
layout (binding = 9) uniform sampler2D u_textureOverlayTile;
layout (binding = 10) uniform sampler2D u_textureBorders;

uniform float u_zoom;
uniform float u_time;
uniform vec4 u_colorProvinceSelected;

out vec4 fragColor;

// hqx variables
const int ml = 0;
const float threshold = 0.03;
const float aaScale = 17;
const float mainLineThickness = 0.38197;
const float subLineThickness = 0.2;

//  terrain variables
const vec2 mapSize = vec2(5616, 2160);
const float xx = 1.0 / mapSize.x;
const float yy = 1.0 / mapSize.y;
const vec2 pix = vec2(xx, yy);

// border variables
const vec2 offsets[4] = vec2[](vec2(0.1, 0), vec2(-0.1, 0), vec2(0, 0.1), vec2(0, -0.1));

const vec3 GREYIFY = vec3( 0.212671, 0.715160, 0.072169 );

vec2 getCorrectedTexCoord() {
    return vec2(v_texCoords.x, 1.0 - v_texCoords.y);
}

bool diag(inout vec4 sum, vec2 uv, vec2 p1, vec2 p2, sampler2D texture, float lineThickness) {
    vec4 v1 = texelFetch(texture, ivec2(uv + p1), ml);
    vec4 v2 = texelFetch(texture, ivec2(uv + p2), ml);
    if (length(v1 - v2) < threshold) {
        vec2 dir = p2 - p1,
        lp = uv - (floor(uv + p1) + 0.5);
        dir = normalize(vec2(dir.y, -dir.x));
        float l = clamp((lineThickness - dot(lp, dir)) * aaScale, 0., 1.);
        sum = mix(sum, v1, l);
        return true;
    }
    return false;
}

vec4 hqxFilter(vec2 uv, sampler2D texture) {
    vec4 sum = texelFetch(texture, ivec2(uv), ml);

    if (diag(sum, uv, vec2(-1, 0), vec2(0, 1), texture, mainLineThickness)) {
        diag(sum, uv, vec2(-1, 0), vec2(1, 1), texture, subLineThickness);
        diag(sum, uv, vec2(-1, -1), vec2(0, 1), texture, subLineThickness);
    }

    if (diag(sum, uv, vec2(0, 1), vec2(1, 0), texture, mainLineThickness)) {
        diag(sum, uv, vec2(0, 1), vec2(1, -1), texture, subLineThickness);
        diag(sum, uv, vec2(-1, 1), vec2(1, 0), texture, subLineThickness);
    }

    if (diag(sum, uv, vec2(1, 0), vec2(0, -1), texture, mainLineThickness)) {
        diag(sum, uv, vec2(1, 0), vec2(-1, -1), texture, subLineThickness);
        diag(sum, uv, vec2(1, 1), vec2(0, -1), texture, subLineThickness);
    }

    if (diag(sum, uv, vec2(0, -1), vec2(-1, 0), texture, mainLineThickness)) {
        diag(sum, uv, vec2(0, -1), vec2(-1, 1), texture, subLineThickness);
        diag(sum, uv, vec2(1, -1), vec2(-1, 0), texture, subLineThickness);
    }

    return sum;
}

vec4 getWaterClose(vec2 texCoord)
{
    const float WRAP = 0.8;
    const float WaveModOne = 3.0;
    const float WaveModTwo = 4.0;
    const float SpecValueOne = 20.0;
    const float SpecValueTwo = 5.0;
    const float vWaterTransparens = 0.6;
    const float vColorMapFactor = 0.5;

    vec4 waterColor = vec4(0.22, 0.3, 0.45, 1.0);
    vec3 WorldColorColor = texture(u_textureColorMapWater, texCoord).rgb;
    texCoord *= 100.;
    texCoord = texCoord * 0.25 + u_time * 0.002;

    const vec3 eyeDirection = vec3(0.0, 1.0, 1.0);
    const vec3 lightDirection = vec3(0.0, 1.0, 1.0);

    vec2 coordA = texCoord * 3 + vec2(0.10, 0.10);
    vec2 coordB = texCoord * 1 + vec2(0.00, 0.10);
    vec2 coordC = texCoord * 2 + vec2(0.00, 0.15);
    vec2 coordD = texCoord * 5 + vec2(0.00, 0.30);

    vec4 vBumpA = texture(u_textureWaterNormal, coordA);
    coordB += vec2(0.03, 0.05) * u_time;
    vec4 vBumpB = texture(u_textureWaterNormal, coordB);
    coordC += vec2(0.02, 0.07) * u_time;
    vec4 vBumpC = texture(u_textureWaterNormal, coordC);
    coordD += vec2(0.06, 0.02) * u_time;
    vec4 vBumpD = texture(u_textureWaterNormal, coordD);

    vec3 vBumpTex = normalize(WaveModOne * (vBumpA.xyz + vBumpB.xyz +
    vBumpC.xyz + vBumpD.xyz) - WaveModTwo);

    vec3 eyeDir = normalize(eyeDirection);
    float NdotL = max(dot(eyeDir, (vBumpTex / 2)), 0);

    NdotL = clamp((NdotL + WRAP) / (1 + WRAP), 0.f, 1.f);
    NdotL = mix(NdotL, 1.0, 0.0);

    vec3 color = NdotL * (WorldColorColor * vColorMapFactor);

    vec3 reflVector = -reflect(lightDirection, vBumpTex);
    float specular = dot(normalize(reflVector), eyeDir);
    specular = clamp(specular, 0.0, 1.0);

    specular = pow(specular, SpecValueOne);
    color += (specular / SpecValueTwo);

    waterColor = mix(waterColor, vec4(color, 1.0), vWaterTransparens);

    return waterColor;
}

vec4 getWaterFar(vec2 texCoord) {
    const vec3 waterColor = texture(u_textureColorMapWater, texCoord).rgb;
    vec4 overlayColor = texture(u_textureOverlayTile, v_texCoords * vec2(11., 11. * mapSize.y / mapSize.x));
    if (overlayColor.r < 0.5) {
        overlayColor.rgb = 2.0 * overlayColor.rgb * waterColor;
    } else {
        overlayColor.rgb = 1.0 - 2.0 * (1.0 - overlayColor.rgb) * (1.0 - waterColor);
    }

    return overlayColor;
}

// The terrain color from the current texture coordinate offset with one pixel in the "corner" direction
vec4 getTerrain(vec2 corner, vec2 texCoord, vec2 localTexCoord) {
    float index = texture(u_textureTerrain, floor(texCoord * mapSize + vec2(0.5, 0.5)) / mapSize + 0.5 * pix * corner).r;
    index = floor(index * 256);
    vec4 colour = texture(u_textureTerrainsheet, vec3(localTexCoord, index));
    return colour;
}

vec4 getTerrainMix(vec2 texCoord) {
    // Pixel size on map texture
    vec2 scaling = fract(texCoord * mapSize + vec2(0.5, 0.5));
    vec2 localTexCoord = fract(texCoord * mapSize / 16.0);

    vec4 colourlu = getTerrain(vec2(-1, -1), texCoord, localTexCoord);
    vec4 colourld = getTerrain(vec2(-1, 1), texCoord, localTexCoord);
    vec4 colourru = getTerrain(vec2(1, -1), texCoord, localTexCoord);
    vec4 colourrd = getTerrain(vec2(1, 1), texCoord, localTexCoord);

    // Mix together the terrains based on how close they are to the current texture coordinate
    vec4 colour_u = mix(colourlu, colourru, scaling.x);
    vec4 colour_d = mix(colourld, colourrd, scaling.x);
    vec4 terrain = mix(colour_u, colour_d, scaling.y);

    // Mixes the terrains from "texturesheet.tga" with the "colormap.dds" background color.
    vec4 terrain_background = texture(u_textureColormap, texCoord);
    terrain.rgb = (terrain.rgb * 2. + terrain_background.rgb) / 3.;
    return terrain;
}

vec4 getBorder(vec4 filteredColorProvince, vec4 filteredColorCountry, vec2 uv) {
    for (int i = 0; i < 4; i++) {
        vec4 filteredNeighbor = hqxFilter(uv + offsets[i], u_textureProvinces);
        if (distance(filteredColorProvince.rgb, filteredNeighbor.rgb) > threshold) {
            return vec4(0.0, 0.0, 0.0, 1.0);
        }
    }

    return vec4(0.0, 0.0, 0.0, 0.0);
}

vec4 getLandClose(vec4 colorCountry, vec2 texCoord, vec2 uv) {
    vec4 colorProvince = hqxFilter(uv, u_textureProvinces);
    vec4 terrain = getTerrainMix(texCoord);
    vec3 political = colorCountry.rgb;

    float grey = dot(terrain.rgb, GREYIFY);
    terrain.rgb = vec3(grey);

    if(colorProvince.rgb == u_colorProvinceSelected.rgb) {
        political.rgb += 0.20;
        political.rgb = clamp(political.rgb, 0.0, 1.0);
        political.rgb *= 1.3;
        political.rgb = mix(political.rgb, vec3(grey), 0.5);
    }

    vec4 stripeColor = texture(u_textureProvincesStripes, texCoord);
    if(stripeColor.a > 0.0) {
        vec2 stripeCoord = fract(texCoord * vec2(512.0, 512.0 * mapSize.y / mapSize.x));
        float stripeFactor = texture(u_textureStripes, stripeCoord).a;
        political.rgb = clamp(mix(political.rgb, stripeColor.rgb, stripeFactor), 0.0, 1.0).rgb;
    }

    float colorBorder = texture(u_textureBorders, texCoord).a;
    if(colorBorder > 0.0) {
        vec4 border = getBorder(colorProvince, colorCountry, uv);
        if(border.a > 0.0) {
            political.rgb = border.rgb;
        }
    }

    political = political - 0.7;
    terrain.rgb = mix(terrain.rgb, political, 0.3);
    terrain.rgb *= 1.5;
    terrain.a = colorCountry.a;
    return terrain;
}

vec4 getLandFar(vec4 colorCountry, vec2 texCoord, vec2 uv) {
    vec4 colorProvince = hqxFilter(uv, u_textureProvinces);
    vec4 political = colorCountry;

    if(colorProvince.rgb == u_colorProvinceSelected.rgb) {
        political.rgb += 0.20;
        political.rgb = clamp(political.rgb, 0.0, 1.0);
        political.rgb *= 1.3;
        float grey = dot(political.rgb, GREYIFY);
        political.rgb = mix(political.rgb, vec3(grey), 0.5);
    }

    vec4 stripeColor = texture(u_textureProvincesStripes, texCoord);
    if(stripeColor.a > 0.0) {
        vec2 stripeCoord = fract(texCoord * vec2(512.0, 512.0 * mapSize.y / mapSize.x));
        float stripeFactor = texture(u_textureStripes, stripeCoord).a;
        political.rgb = clamp(mix(political.rgb, stripeColor.rgb, stripeFactor), 0.0, 1.0).rgb;
    }

    vec4 overlayColor = texture(u_textureOverlayTile, v_texCoords * vec2(11., 11. * mapSize.y / mapSize.x));
    if (overlayColor.r < 0.5) {
        political.rgb = 2.0 * overlayColor.rgb * political.rgb;
    } else {
        political.rgb = 1.0 - 2.0 * (1.0 - overlayColor.rgb) * (1.0 - political.rgb);
    }

    float grey = dot(colorCountry.rgb, GREYIFY);
    colorCountry.rgb = mix(political.rgb, vec3(grey), 0.3);

    return colorCountry;
}

void main()
{
    vec2 texCoord = getCorrectedTexCoord();
    vec2 uv = texCoord * mapSize;
    vec4 colorCountry = hqxFilter(uv, u_textureCountries);
    vec4 terrain;
    vec4 water;
    int redColorWater = 0;

    if(u_zoom > .8) {
        if(colorCountry.r > redColorWater) {
            terrain = getLandFar(colorCountry, texCoord, uv);
        } else {
            water = getWaterFar(texCoord);
        }
    } else {
        if(colorCountry.r > redColorWater) {
            terrain = getLandClose(colorCountry, texCoord, uv);
        } else {
            water = getWaterClose(texCoord);
        }
    }

    fragColor = terrain;

    fragColor.rgb = mix(water.rgb, terrain.rgb, terrain.a);
    fragColor.a = 1.f;
}
