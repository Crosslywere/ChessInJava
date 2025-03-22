#version 330 core
layout (location = 0) out vec4 oColorAttachment;
layout (location = 1) out int oID;
in vec2 iTexCoord;
in vec3 iNormal;
uniform int uID;
uniform float uTime;
uniform float uDelta;
uniform sampler2D uTexture;
void main() {
    oID = uID;
    float t = cos(uTime + uDelta) * 0.5 + 0.5;
    vec3 imgSample = vec3(texture2D(uTexture, iTexCoord));
    vec3 oColor = clamp(abs(iNormal) + t, 0.0, 1.0) * imgSample;
    oColorAttachment = vec4(oColor, 1.0);
}