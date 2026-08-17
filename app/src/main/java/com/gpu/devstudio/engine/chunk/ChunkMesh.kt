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
        
        // Gerar faces dos blocos na superfície do chunk
        for (x in 0 until chunkSize) {
            for (y in 0 until chunkSize) {
                for (z in 0 until chunkSize) {
                    // Renderizar apenas blocos na superfície
                    if (x == 0 || x == chunkSize - 1 || 
                        y == 0 || y == chunkSize - 1 || 
                        z == 0 || z == chunkSize - 1) {
                        addBlockFaces(vertices, x.toFloat(), y.toFloat(), z.toFloat(), 1.0f)
                    }
                }
            }
        }
        
        vertexCount = vertices.size / 8
        
        // Criar VAO e VBO
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
        
        // Position (location = 0) - 3 floats
        GLES30.glEnableVertexAttribArray(0)
        GLES30.glVertexAttribPointer(0, 3, GLES30.GL_FLOAT, false, 32, 0)
        
        // Normal (location = 1) - 3 floats
        GLES30.glEnableVertexAttribArray(1)
        GLES30.glVertexAttribPointer(1, 3, GLES30.GL_FLOAT, false, 32, 12)
        
        // TexCoord (location = 2) - 2 floats
        GLES30.glEnableVertexAttribArray(2)
        GLES30.glVertexAttribPointer(2, 2, GLES30.GL_FLOAT, false, 32, 24)
        
        GLES30.glBindVertexArray(0)
    }
    
    private fun addBlockFaces(vertices: MutableList<Float>, x: Float, y: Float, z: Float, size: Float) {
        // Cada face: 6 vértices (2 triângulos), cada vértice: 8 floats (x,y,z, nx,ny,nz, u,v)
        
        // Front face (z+)
        addFace(vertices, x, y, z + size, 0f, 0f, 1f, size, 0f, 0f, size)
        // Back face (z-)
        addFace(vertices, x + size, y, z, 0f, 0f, -1f, size, 0f, 0f, size)
        // Top face (y+)
        addFace(vertices, x, y + size, z, 0f, 1f, 0f, size, 0f, 0f, size)
        // Bottom face (y-)
        addFace(vertices, x, y, z + size, 0f, -1f, 0f, size, 0f, 0f, size)
        // Right face (x+)
        addFace(vertices, x + size, y, z, 1f, 0f, 0f, size, 0f, 0f, size)
        // Left face (x-)
        addFace(vertices, x, y, z, -1f, 0f, 0f, size, 0f, 0f, size)
    }
    
    private fun addFace(
        vertices: MutableList<Float>,
        startX: Float, startY: Float, startZ: Float,
        normalX: Float, normalY: Float, normalZ: Float,
        width: Float, height: Float, depth: Float
    ) {
        // Determinar os 4 cantos da face baseado na normal
        val p1x = startX
        val p1y = startY
        val p1z = startZ
        
        val p2x = startX + if (normalX != 0f) 0f else width
        val p2y = startY + if (normalY != 0f) 0f else height
        val p2z = startZ + if (normalZ != 0f) 0f else depth
        
        val p3x = startX + if (normalX != 0f) 0f else width
        val p3y = startY + if (normalY != 0f) height else 0f
        val p3z = startZ + if (normalZ != 0f) depth else 0f
        
        val p4x = startX + if (normalX != 0f) 0f else width
        val p4y = startY + if (normalY != 0f) height else 0f
        val p4z = startZ + if (normalZ != 0f) depth else 0f
        
        // Triângulo 1
        addVertex(vertices, p1x, p1y, p1z, normalX, normalY, normalZ, 0f, 0f)
        addVertex(vertices, p2x, p2y, p2z, normalX, normalY, normalZ, 1f, 0f)
        addVertex(vertices, p3x, p3y, p3z, normalX, normalY, normalZ, 1f, 1f)
        
        // Triângulo 2
        addVertex(vertices, p1x, p1y, p1z, normalX, normalY, normalZ, 0f, 0f)
        addVertex(vertices, p3x, p3y, p3z, normalX, normalY, normalZ, 1f, 1f)
        addVertex(vertices, p4x, p4y, p4z, normalX, normalY, normalZ, 0f, 1f)
    }
    
    private fun addVertex(
        vertices: MutableList<Float>,
        x: Float, y: Float, z: Float,
        nx: Float, ny: Float, nz: Float,
        u: Float, v: Float
    ) {
        vertices.add(x)
        vertices.add(y)
        vertices.add(z)
        vertices.add(nx)
        vertices.add(ny)
        vertices.add(nz)
        vertices.add(u)
        vertices.add(v)
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
