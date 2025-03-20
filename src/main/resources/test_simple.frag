#version 330 core
layout (location = 0) out vec4 oColorAttachment;
layout (location = 1) out int oID;
in vec2 iTexCoord;
in vec3 iNormal;
uniform int uID;
void main() {
    oID = uID;
    oColorAttachment = vec4(iNormal, 1.0);
}