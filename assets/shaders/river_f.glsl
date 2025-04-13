#version 420

in vec2 v_texCoords;
in vec2 v_worldCoords;

uniform sampler2D u_texture;
uniform sampler2D u_textureColorMapWater;
uniform float u_time;

out vec4 fragColor;

void main() {
    fragColor = texture(u_texture, vec2(v_texCoords.x, mod(v_texCoords.y - u_time, 1.f)));
    fragColor *= texture(u_textureColorMapWater, vec2(v_worldCoords.x, v_worldCoords.y));
}
