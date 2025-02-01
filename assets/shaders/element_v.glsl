#version 420

in vec2 a_position;
in vec2 a_texCoord0;
in vec2 a_center;

uniform float u_zoom;
uniform mat4 u_projTrans;

out vec2 v_texCoords;

void main() {
    v_texCoords = a_texCoord0;

    vec2 offset = a_position - a_center;
    vec2 scaledPosition = a_center + (offset * u_zoom);

    gl_Position = u_projTrans * vec4(scaledPosition, 0.0, 1.0);
}
