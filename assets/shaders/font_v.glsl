#version 420

in vec4 a_position;
in vec4 a_color;
in vec2 a_texCoord0;

uniform mat4 u_projTrans;
uniform float u_angle;
uniform vec2 u_center;

out vec4 v_color;
out vec2 v_texCoords;

void main() {
    float cosAngle = cos(u_angle);
    float sinAngle = sin(u_angle);

    vec2 pos = a_position.xy - u_center;

    vec2 rotatedPos;
    rotatedPos.x = pos.x * cosAngle - pos.y * sinAngle;
    rotatedPos.y = pos.x * sinAngle + pos.y * cosAngle;

    rotatedPos += u_center;

    gl_Position = u_projTrans * vec4(rotatedPos, a_position.z, 1.0);

    v_color = a_color;
    v_texCoords = a_texCoord0;
}
