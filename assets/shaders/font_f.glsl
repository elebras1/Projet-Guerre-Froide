#version 420

in vec2 v_texCoords;
in vec4 v_color;
uniform sampler2D u_texture;
uniform float u_zoom;

const float smoothing = 0.02;

out vec4 fragColor;

void main() {
    vec4 texColor = texture(u_texture, v_texCoords);

    float visibility = smoothstep(0.10, 0.25, u_zoom);

    float distance = texColor.a;
    float alpha = smoothstep(0.50 - smoothing, 0.50 + smoothing, distance);

    vec4 text = vec4(texColor.rgb, v_color.a * alpha);
    fragColor = text * visibility;
}
