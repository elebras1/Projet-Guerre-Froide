#version 320 es

in vec4 a_position;
in vec4 a_color;
in vec2 a_texCoord0;

uniform mat4 u_projTrans;

out vec2 v_texCoords;

void main() {
    v_texCoords = a_texCoord0;
    gl_Position = u_projTrans * a_position;
}
