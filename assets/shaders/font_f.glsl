#version 320 es
precision mediump float;

in vec2 v_texCoords;
in vec4 v_color;
in float opacity;
uniform sampler2D u_texture;

const float smoothing = 0.02;

out vec4 fragColor;

void main() {
    vec4 texColor = texture(u_texture, v_texCoords);

    float distance = texColor.a;
    float alpha = smoothstep(0.50 - smoothing, 0.50 + smoothing, distance);

    vec4 text = vec4(texColor.rgb, v_color.a * alpha);
    fragColor = text * opacity;
}
