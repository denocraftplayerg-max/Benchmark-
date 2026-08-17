#version 300 es
layout(location = 0) in uint inVoxelData;
uniform mat4 u_MVP;
out vec3 outColor;
out float outAO;

const vec3 COLORS[4] = vec3[4](
    vec3(0.0, 0.0, 0.0),
    vec3(0.6, 0.4, 0.2),
    vec3(0.2, 0.8, 0.2),
    vec3(0.4, 0.4, 0.4)
);

const float FACE_LIGHT[6] = float[6](0.8,0.8,0.6,0.6,1.0,0.5);

// Posições dos 4 cantos de cada face
const vec3 QUAD[6][4] = vec3[6][4](
    vec3[4](vec3(0,0,1),vec3(1,0,1),vec3(1,1,1),vec3(0,1,1)), // +Z
    vec3[4](vec3(1,0,0),vec3(0,0,0),vec3(0,1,0),vec3(1,1,0)), // -Z
    vec3[4](vec3(1,0,1),vec3(1,0,0),vec3(1,1,0),vec3(1,1,1)), // +X
    vec3[4](vec3(0,0,0),vec3(0,0,1),vec3(0,1,1),vec3(0,1,0)), // -X
    vec3[4](vec3(0,1,1),vec3(1,1,1),vec3(1,1,0),vec3(0,1,0)), // +Y
    vec3[4](vec3(0,0,0),vec3(1,0,0),vec3(1,0,1),vec3(0,0,1))  // -Y
);

// 6 vértices = 2 triângulos: 0,1,2,0,2,3
const int CORNER[6] = int[6](0,1,2,0,2,3);

void main() {
    uint x     = (inVoxelData >>  0u) & 0x3Fu;
    uint y     = (inVoxelData >>  6u) & 0x3Fu;
    uint z     = (inVoxelData >> 12u) & 0x3Fu;
    uint face  = (inVoxelData >> 18u) & 0x07u;
    uint ao    = (inVoxelData >> 21u) & 0x03u;
    uint vidx  = (inVoxelData >> 23u) & 0x07u;
    uint block = (inVoxelData >> 26u) & 0x3Fu;

    int corner = CORNER[vidx];
    vec3 pos = vec3(float(x), float(y), float(z)) + QUAD[face][corner];

    outColor = COLORS[min(block, 3u)];
    outAO = FACE_LIGHT[face] * (0.5 + (float(ao) / 3.0) * 0.5);

    gl_Position = u_MVP * vec4(pos, 1.0);
}
