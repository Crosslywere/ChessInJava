#version 330 core
layout (location = 0) out vec4 o_Color;
layout (location = 1) out int o_Id;
in vec3 i_Normal;
uniform int u_Id;
uniform vec3 u_LightDir;
uniform vec3 u_Color;
void main() {
    vec3 lightDir = reflect(-u_LightDir, i_Normal);
    float diff = max(dot(i_Normal, u_LightDir), 0.0);
    vec3 color = (0.25 + diff) * u_Color;
    o_Id = u_Id;
    o_Color = vec4(color, 1.0);
}