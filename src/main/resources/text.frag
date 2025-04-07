#version 330 core
in vec2 iTexCoord;
layout (location = 0) out vec4 oFragColor;
layout (location = 1) out int oID;
uniform vec3 uColor;
uniform int uID;
uniform sampler2D uTextTexture;

void main() {
    oID = uID;
    float alpha = texture(uTextTexture, iTexCoord).r;
    oFragColor = vec4(uColor, alpha);
}