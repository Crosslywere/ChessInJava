#version 330 core
layout (location = 0) in vec3 a_Position;
layout (location = 2) in vec3 a_Normal;
uniform mat4 u_ProjView;
uniform mat4 u_Model;
out vec3 i_Normal;
void main() {
    i_Normal = mat3(transpose(inverse(u_Model))) * a_Normal;
    gl_Position = u_ProjView * u_Model * vec4(a_Position, 1.0);
}