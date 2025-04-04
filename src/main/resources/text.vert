#version 330 core
layout (location = 0) in vec2 aPosition;
layout (location = 1) in vec2 aTexCoord;
out vec2 iTexCoord;
uniform mat4 uOrtho;

void main() {
    iTexCoord = aTexCoord;
    gl_Position = uOrtho * vec4(aPosition, 0.0, 1.0);
}