#version 420

in vec2 v_texCoords;
uniform sampler2D u_textureFlag;
uniform sampler2D u_textureOverlay;
uniform sampler2D u_textureAlpha;
uniform vec2 u_flagSize;
uniform vec2 u_overlaySize;

out vec4 fragColor;

void main() {
    vec4 flagColor = texture(u_textureFlag, v_texCoords);
    vec4 overlayColor = texture(u_textureOverlay, v_texCoords);
    vec4 alphaColor = texture(u_textureAlpha, v_texCoords);

    vec4 color = mix(flagColor, overlayColor, overlayColor.a);

    fragColor = mix(alphaColor, color, alphaColor.a);
    if (fragColor.a > 0.0) {
        fragColor.a = 1.0;
        fragColor.a = step(0.0, fragColor.a);
    }
}