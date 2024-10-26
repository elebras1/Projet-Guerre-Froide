#version 420

uniform sampler2D u_textureFont;

in vec4 v_color;
in vec2 v_texCoords;

out vec4 fragColor;

void main() {
    fragColor = v_color * texture(u_textureFont, v_texCoords);
}
