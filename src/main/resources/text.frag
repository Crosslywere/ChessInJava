#version 330 core
in vec2 iTexCoord;
layout (location = 0) out vec4 oFragColor;
uniform vec3 uColor;
uniform sampler2D uTextTexture;

void main() {
    float alpha = texture(uTextTexture, iTexCoord).r;
    oFragColor = vec4(uColor, alpha);
}