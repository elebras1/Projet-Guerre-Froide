#version 420

in vec2 v_texCoords;
uniform sampler2D u_texture;

out vec4 fragColor;

void main() {
    fragColor = texture(u_texture, v_texCoords);
}
