#version 420

in vec2 v_texCoords;
uniform sampler2D u_textureFlag;
uniform sampler2D u_textureOverlay;
uniform sampler2D u_textureAlpha;

uniform vec4 u_uvFlag;
uniform vec4 u_uvOverlay;
uniform vec4 u_uvAlpha;

out vec4 fragColor;

void main() {
    vec2 uvFlag = mix(u_uvFlag.xy, u_uvFlag.zw, v_texCoords);
    vec2 uvOverlay = mix(u_uvOverlay.xy, u_uvOverlay.zw, v_texCoords);
    vec2 uvAlpha = mix(u_uvAlpha.xy, u_uvAlpha.zw, v_texCoords);

    vec4 flagColor = texture(u_textureFlag, uvFlag);
    vec4 overlayColor = texture(u_textureOverlay, uvOverlay);
    vec4 alphaColor = texture(u_textureAlpha, uvAlpha);

    vec4 color = mix(flagColor, overlayColor, overlayColor.a);
    if(alphaColor.a < 1) {
        fragColor = overlayColor;
    } else {
        fragColor = mix(alphaColor, color, alphaColor.a);
        if (fragColor.a > 0.0) {
            fragColor.a = 1.0;
        }
    }
}
