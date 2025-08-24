#version 320 es

in vec2 a_position;
in vec2 a_texCoord0;

uniform mat4 u_projTrans;
uniform float u_worldWidth;

out vec2 v_texCoords;

void main() {
    v_texCoords = a_texCoord0;

    float instanceOffset = 0.0;
    if (gl_InstanceID == 1) {
        instanceOffset = -u_worldWidth;
    } else if (gl_InstanceID == 2) {
        instanceOffset = u_worldWidth;
    }

    vec4 finalPosition = vec4(a_position.x + instanceOffset, a_position.y, 0.0, 1.0);
    gl_Position = u_projTrans * finalPosition;
}
