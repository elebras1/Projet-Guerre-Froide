#version 320 es

in vec4 a_position;
in vec4 a_color;
in vec2 a_texCoord0;

uniform mat4 u_projTrans;
uniform float u_zoom;

out vec4 v_color;
out vec2 v_texCoords;
out float opacity;

const float thickness = 0.04;

void main() {
    v_color = a_color;
    v_texCoords = a_texCoord0;
    opacity = 1.f;
    if(u_zoom < 0.5f) {
        opacity = exp(-(u_zoom * 50.f - 1.f/thickness) * (u_zoom * 50.f - 1.f/thickness) * 0.01f);
    }
    gl_Position = u_projTrans * a_position;
}
