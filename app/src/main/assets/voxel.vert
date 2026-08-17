#version 300 es
layout(location = 0) in uint inVoxelData;

uniform mat4 u_MVP;

out vec2 outUV;
out float outAO;
out flat uint outBlockID;

void main() {
    uint x = (inVoxelData >> 0) & 0x3Fu;
    uint y = (inVoxelData >> 6) & 0x3Fu;
    uint z = (inVoxelData >> 12) & 0x3Fu;
    uint face = (inVoxelData >> 18) & 0x07u;
    uint block = (inVoxelData >> 21) & 0xFFu;
    uint ao = (inVoxelData >> 29) & 0x03u;
    uint vIdx = (inVoxelData >> 31) & 0x01u;

    vec3 localPos = vec3(float(x), float(y), float(z));
    
    // Lookup tables inline para evitar consumo de banda
    vec2 uvs[4] = vec2[](vec2(0,0), vec2(1,0), vec2(1,1), vec2(0,1));
    outUV = uvs[vIdx * 2u]; 
    
    outBlockID = block;
    outAO = 0.4 + (float(ao) / 3.0) * 0.6; 

    gl_Position = u_MVP * vec4(localPos, 1.0);
}
