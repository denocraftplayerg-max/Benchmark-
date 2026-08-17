#version 300 es
precision mediump float;
in vec2 inUV;
in float inAO;
in flat uint inBlockID;
out vec4 fragColor;

vec3 getBlockColor(uint id) {
    if (id == 1u) return vec3(0.5, 0.3, 0.1); // Terra
    if (id == 2u) return vec3(0.2, 0.8, 0.3); // Relva
    if (id == 3u) return vec3(0.3, 0.3, 0.3); // Bedrock
    return vec3(1.0, 0.0, 1.0); // Fallback
}

void main() {
    vec3 baseColor = getBlockColor(inBlockID);
    fragColor = vec4(baseColor * inAO, 1.0);
}
