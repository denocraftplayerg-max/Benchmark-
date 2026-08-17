#version 300 es
precision mediump float;
in vec2 outUV;
in float outAO;
in flat uint outBlockID;
in flat uint outFace;
uniform sampler2D u_Atlas;
out vec4 fragColor;

const float ATLAS_SIZE = 16.0;

const float FACE_LIGHT[6] = float[6](0.8,0.8,0.6,0.6,1.0,0.5);

// blockID -> coluna/linha no atlas 16x16
// 1=dirt, 2=grass_top/side, 3=bedrock
vec2 getAtlasTile(uint blockID, uint face) {
    // grass: topo=relva(0,0), lados=grass_side(1,0), baixo=dirt(2,0)
    if (blockID == 2u) {
        if (face == 4u) return vec2(0.0, 0.0); // grass_block_top
        if (face == 5u) return vec2(2.0, 0.0); // dirt
        return vec2(1.0, 0.0);                  // grass_block_side
    }
    if (blockID == 1u) return vec2(2.0, 0.0);  // dirt
    if (blockID == 3u) return vec2(3.0, 0.0);  // bedrock
    return vec2(15.0, 15.0);                    // fallback magenta
}

void main() {
    vec2 tile = getAtlasTile(outBlockID, outFace);
    vec2 uv = (tile + clamp(outUV, 0.001, 0.999)) / ATLAS_SIZE;
    vec4 tex = texture(u_Atlas, uv);
    if (tex.a < 0.5) discard;
    float light = FACE_LIGHT[outFace] * outAO;
    fragColor = vec4(tex.rgb * light, tex.a);
}
