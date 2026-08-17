#version 300 es
precision mediump float;
in vec3 outColor;
in float outAO;
out vec4 fragColor;

void main() {
    fragColor = vec4(outColor * outAO, 1.0);
}
