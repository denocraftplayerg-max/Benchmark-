#version 300 es
layout(location = 0) in uint inVoxelData;
uniform mat4 u_MVP;
out vec2 outUV;
out float outAO;
out flat uint outBlockID;
out flat uint outFace;

const vec3 FACE_OFFSETS[6][4] = vec3[6][4](
    vec3[4](vec3(0,0,1),vec3(1,0,1),vec3(1,1,1),vec3(0,1,1)),
    vec3[4](vec3(1,0,0),vec3(0,0,0),vec3(0,1,0),vec3(1,1,0)),
    vec3[4](vec3(1,0,1),vec3(1,0,0),vec3(1,1,0),vec3(1,1,1)),
    vec3[4](vec3(0,0,0),vec3(0,0,1),vec3(0,1,1),vec3(0,1,0)),
    vec3[4](vec3(0,1,1),vec3(1,1,1),vec3(1,1,0),vec3(0,1,0)),
    vec3[4](vec3(0,0,0),vec3(1,0,0),vec3(1,0,1),vec3(0,0,1))
);
const vec2 UV_TABLE[6] = vec2[6](
    vec2(0,0),vec2(1,0),vec2(1,1),vec2(0,1),vec2(0,0),vec2(1,1)
);
const uint TRI_IDX[6] = uint[6](0u,1u,2u,0u,2u,3u);

void main() {
    uint x     = (inVoxelData >>  0u) & 0x3Fu;
    uint y     = (inVoxelData >>  6u) & 0x3Fu;
    uint z     = (inVoxelData >> 12u) & 0x3Fu;
    uint face  = (inVoxelData >> 18u) & 0x07u;
    uint block = (inVoxelData >> 21u) & 0xFFu;
    uint ao    = (inVoxelData >> 29u) & 0x03u;
    uint vIdx  = (inVoxelData >> 27u) & 0x07u;

    vec3 base = vec3(float(x), float(y), float(z));
    uint corner = TRI_IDX[vIdx];
    vec3 pos = base + FACE_OFFSETS[face][corner];

    outUV      = UV_TABLE[corner];
    outBlockID = block;
    outFace    = face;
    outAO      = 0.4 + (float(ao) / 3.0) * 0.6;
    gl_Position = u_MVP * vec4(pos, 1.0);
}
