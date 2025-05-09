#version 420

in vec2 v_texCoords;
in vec2 v_worldCoords;
in float v_width;

uniform sampler2D u_texture;
uniform sampler2D u_textureColorMapWater;
uniform float u_time;
uniform float u_zoom;

out vec4 fragColor;

void main() {
    vec2 scrolledCoords = vec2(v_texCoords.x, v_texCoords.y - u_time);
    fragColor = texture(u_texture, scrolledCoords);
    fragColor *= texture(u_textureColorMapWater, v_worldCoords);
    fragColor.rgb = vec3(1.0) - vec3(0.8) * (1.0 - fragColor.rgb);

    float zoomBlend = clamp((u_zoom - 0.8) * 0.37037, 0.0, 1.0);

    float widthThreshold = mix(0.25, 0.9, zoomBlend);
    float visibilityMask = (u_zoom > 0.8) ? step(widthThreshold, v_width) : 1.0;

    fragColor.a *= visibilityMask;
}
