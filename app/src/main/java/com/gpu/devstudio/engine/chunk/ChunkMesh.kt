package com.gpu.devstudio.engine.chunk

import android.opengl.GLES30
import java.nio.ByteBuffer
import java.nio.ByteOrder

class ChunkMesh(private val chunkSize: Int = 16) {
    private var vao = 0
    private var vbo = 0
    private var vertexCount = 0

    fun generate() {
        val vertices = mutableListOf<Float>()
        val step = 1.0f
        
        // Gerar uma grade plana (topo do chunk) para garantir compilação e renderização
        for (x in 0 until chunkSize) {
            for (z in 0 until chunkSize) {
                val x0 = x * step
                val z0 = z * step
                val x1 = (x + 1) * step
                val z1 = (z + 1) * step
                val y = 0.0f
                
                // Formato do vértice: x, y, z, nx, ny, nz, u, v (8 floats)
                
                // Triângulo 1
                vertices.add(x0); vertices.add(y); vertices.add(z0) // pos
                vertices.add(0f); vertices.add(1f); vertices.add(0f) // normal
                vertices.add(0f); vertices.add(0f) // uv
                
                vertices.add(x1); vertices.add(y); vertices.add(z0)
                vertices.add(0f); vertices.add(1f); vertices.add(0f)
                vertices.add(1f); vertices.add(0f)
                
                vertices.add(x0); vertices.add(y); vertices.add(z1)
                vertices.add(0f); vertices.add(1f); vertices.add(0f)
                vertices.add(0f); vertices.add(1f)
                
                // Triângulo 2
                vertices.add(x1); vertices.add(y); vertices.add(z0)
                vertices.add(0f); vertices.add(1f); vertices.add(0f)
                vertices.add(1f); vertices.add(0f)
                
                vertices.add(x1); vertices.add(y); vertices.add(z1)
                vertices.add(0f); vertices.add(1f); vertices.add(0f)
                vertices.add(1f); vertices.add(1f)
                
                vertices.add(x0); vertices.add(y); vertices.add(z1)
                vertices.add(0f); vertices.add(1f); vertices.add(0f)
                vertices.add(0f); vertices.add(1f)
            }
        }
        
        vertexCount = vertices.size / 8
        
        val vaoIds = IntArray(1)
        GLES30.glGenVertexArrays(1, vaoIds, 0)
        vao = vaoIds[0]
        
        val vboIds = IntArray(1)
        GLES30.glGenBuffers(1, vboIds, 0)
        vbo = vboIds[0]
        
        val buffer = ByteBuffer.allocateDirect(vertices.size * 4)
            .order(ByteOrder.nativeOrder())
        val floatBuffer = buffer.asFloatBuffer()
        floatBuffer.put(vertices.toFloatArray())
        floatBuffer.flip()
        
        GLES30.glBindVertexArray(vao)
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, vbo)
        GLES30.glBufferData(GLES30.GL_ARRAY_BUFFER, floatBuffer.capacity() * 4, floatBuffer, GLES30.GL_STATIC_DRAW)
        
        // Location 0: Position (3 floats)
        GLES30.glEnableVertexAttribArray(0)
        GLES30.glVertexAttribPointer(0, 3, GLES30.GL_FLOAT, false, 32, 0)
        
        // Location 1: Normal (3 floats)
        GLES30.glEnableVertexAttribArray(1)
        GLES30.glVertexAttribPointer(1, 3, GLES30.GL_FLOAT, false, 32, 12)
        
        // Location 2: TexCoord (2 floats)
        GLES30.glEnableVertexAttribArray(2)
        GLES30.glVertexAttribPointer(2, 2, GLES30.GL_FLOAT, false, 32, 24)
        
        GLES30.glBindVertexArray(0)
    }

    fun render() {
        if (vao == 0) return
        GLES30.glBindVertexArray(vao)
        GLES30.glDrawArrays(GLES30.GL_TRIANGLES, 0, vertexCount)
        GLES30.glBindVertexArray(0)
    }

    fun dispose() {
        if (vbo != 0) {
            GLES30.glDeleteBuffers(1, intArrayOf(vbo), 0)
            vbo = 0
        }
        if (vao != 0) {
            GLES30.glDeleteVertexArrays(1, intArrayOf(vao), 0)
            vao = 0
        }
    }
}
