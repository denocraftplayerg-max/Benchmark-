package com.gpu.devstudio.engine.chunk

import android.opengl.GLES20
import android.opengl.GLES30

class ShaderProgram {
    var programId = 0
        private set
    
    private var vertexShader = 0
    private var fragmentShader = 0
    
    companion object {
        const val VERTEX_SHADER = """
            #version 300 es
            precision highp float;
            
            layout(location = 0) in vec3 aPosition;
            layout(location = 1) in vec3 aNormal;
            layout(location = 2) in vec2 aTexCoord;
            
            uniform mat4 uMVP;
            uniform mat4 uModel;
            
            out vec3 vNormal;
            out vec2 vTexCoord;
            out vec3 vWorldPos;
            
            void main() {
                gl_Position = uMVP * vec4(aPosition, 1.0);
                vNormal = mat3(uModel) * aNormal;
                vTexCoord = aTexCoord;
                vWorldPos = (uModel * vec4(aPosition, 1.0)).xyz;
            }
        """
        
        const val FRAGMENT_SHADER = """
            #version 300 es
            precision highp float;
            
            in vec3 vNormal;
            in vec2 vTexCoord;
            in vec3 vWorldPos;
            
            uniform sampler2D uTexture;
            uniform vec3 uLightDir;
            uniform vec3 uCameraPos;
            
            out vec4 fragColor;
            
            void main() {
                vec3 normal = normalize(vNormal);
                vec3 lightDir = normalize(uLightDir);
                
                // Diffuse lighting
                float diff = max(dot(normal, lightDir), 0.0);
                
                // Texture sampling
                vec4 texColor = texture(uTexture, vTexCoord);
                
                // Simple fog based on distance
                float dist = length(vWorldPos - uCameraPos);
                float fog = 1.0 - smoothstep(20.0, 60.0, dist);
                
                vec3 ambient = vec3(0.3);
                vec3 lighting = ambient + vec3(0.7) * diff;
                
                fragColor = vec4(texColor.rgb * lighting, texColor.a);
            }
        """
    }
    
    fun compile(): Boolean {
        vertexShader = compileShader(GLES30.GL_VERTEX_SHADER, VERTEX_SHADER)
        if (vertexShader == 0) return false
        
        fragmentShader = compileShader(GLES30.GL_FRAGMENT_SHADER, FRAGMENT_SHADER)
        if (fragmentShader == 0) return false
        
        programId = GLES30.glCreateProgram()
        GLES30.glAttachShader(programId, vertexShader)
        GLES30.glAttachShader(programId, fragmentShader)
        GLES30.glLinkProgram(programId)
        
        val linkStatus = IntArray(1)
        GLES30.glGetProgramiv(programId, GLES30.GL_LINK_STATUS, linkStatus, 0)
        
        if (linkStatus[0] == 0) {
            val log = GLES30.glGetProgramInfoLog(programId)
            GLES30.glDeleteProgram(programId)
            programId = 0
            return false
        }
        
        return true
    }
    
    private fun compileShader(type: Int, source: String): Int {
        val shader = GLES30.glCreateShader(type)
        GLES30.glShaderSource(shader, source)
        GLES30.glCompileShader(shader)
        
        val compileStatus = IntArray(1)
        GLES30.glGetShaderiv(shader, GLES30.GL_COMPILE_STATUS, compileStatus, 0)
        
        if (compileStatus[0] == 0) {
            val log = GLES30.glGetShaderInfoLog(shader)
            GLES30.glDeleteShader(shader)
            return 0
        }
        
        return shader
    }
    
    fun use() {
        GLES30.glUseProgram(programId)
    }
    
    fun dispose() {
        if (programId != 0) {
            GLES30.glDeleteProgram(programId)
            programId = 0
        }
        if (vertexShader != 0) {
            GLES30.glDeleteShader(vertexShader)
            vertexShader = 0
        }
        if (fragmentShader != 0) {
            GLES30.glDeleteShader(fragmentShader)
            fragmentShader = 0
        }
    }
}
