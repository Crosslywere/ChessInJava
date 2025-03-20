#version 330 core
layout (location = 0) out vec4 oColorAttachment;
layout (location = 1) out int oID;
in vec2 iTexCoord;
in vec3 iNormal;
uniform int uID;
uniform float uTime;
void main() {
    oID = uID;
    float t = cos(uTime) * 0.5 + 0.5;
    oColorAttachment = vec4(iNormal * t, 1.0);
}