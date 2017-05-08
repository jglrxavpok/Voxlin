//! Fragment
#version 330

in vec2 v_texcoords;
in vec4 v_color;

uniform sampler2D u_texture;

out vec4 color;

void main() {
    vec4 finalColor = texture(u_texture, v_texcoords) * v_color;
    if(finalColor.a < 0.1)
        discard;
    color = finalColor;
}
