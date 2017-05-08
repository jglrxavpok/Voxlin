//! Vertex
#version 330

layout(location = 0) in vec3 a_pos;
layout(location = 1) in vec2 a_texcoords;
layout(location = 2) in vec3 a_normal;
layout(location = 3) in vec4 a_color;

out vec2 v_texcoords;
out vec4 v_color;

uniform mat4 u_viewMatrix;
uniform mat4 u_modelview;

void main() {
    v_texcoords = a_texcoords;
    v_color = a_color;
	gl_Position = u_viewMatrix * u_modelview * vec4(a_pos, 1.0);
}
