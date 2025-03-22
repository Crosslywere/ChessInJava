#version 330 core
layout (location = 0) in vec3 aPos;
layout (location = 1) in vec2 aTexCoord;
layout (location = 2) in vec3 aNormal;
out vec2 iTexCoord;
out vec3 iNormal;
uniform mat4 uProjView;
uniform mat4 uModel;
void main() {
    iTexCoord = aTexCoord;
    iNormal = aNormal;
    gl_Position = uProjView * uModel * vec4(aPos, 1.0);
}