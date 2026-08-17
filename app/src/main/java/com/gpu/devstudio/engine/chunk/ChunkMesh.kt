package com.gpu.devstudio.engine.chunk

import android.opengl.GLES30
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer

class ChunkMesh(private val chunkSize: Int = 16) {
    private var vao = 0
    private var vbo = 0
    private var vertexCount = 0
    
    data class Vertex(
        val x: Float, val y: Float, val z: Float,
        val nx: Float, val ny: Float, val nz: Float,
        val u: Float, val v: Float
    )
    
    fun generate() {
        val vertices = mutableListOf<Float>()
        
        // Gerar chunk de blocos 16x16x16
        for (x in 0 until chunkSize) {
            for (y in 0 until chunkSize) {
                for (z in 0 until chunkSize) {
                    // Simplificação: renderizar apenas blocos na superfície
                    if (x == 0 || x == chunkSize - 1 || 
                        y == 0 || y == chunkSize - 1 || 
                        z == 0 || z == chunkSize - 1) {
                        addBlock(vertices, x.toFloat(), y.toFloat(), z.toFloat(), 1.0f)
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
            .asFloatBuffer()
        buffer.put(vertices.toFloatArray())
        buffer.flip()
        
        GLES30.glBindVertexArray(vao)
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, vbo)
        GLES30.glBufferData(GLES30.GL_ARRAY_BUFFER, buffer.capacity() * 4, buffer, GLES30.GL_STATIC_DRAW)
        
        // Position (location = 0)
        GLES30.glEnableVertexAttribArray(0)
        GLES30.glVertexAttribPointer(0, 3, GLES30.GL_FLOAT, false, 32, 0)
        
        // Normal (location = 1)
        GLES30.glEnableVertexAttribArray(1)
        GLES30.glVertexAttribPointer(1, 3, GLES30.GL_FLOAT, false, 32, 12)
        
        // TexCoord (location = 2)
        GLES30.glEnableVertexAttribArray(2)
        GLES30.glVertexAttribPointer(2, 2, GLES30.GL_FLOAT, false, 32, 24)
        
        GLES30.glBindVertexArray(0)
    }
    
    private fun addBlock(vertices: MutableList<Float>, x: Float, y: Float, z: Float, size: Float) {
        val s = size
        val faces = listOf(
            // Front face (z+)
            Triple(floatArrayOf(x, y, z + s), floatArrayOf(0f, 0f, 1f)),
            // Back face (z-)
            Triple(floatArrayOf(x + s, y, z), floatArrayOf(0f, 0f, -1f)),
            // Top face (y+)
            Triple(floatArrayOf(x, y + s, z), floatArrayOf(0f, 1f, 0f)),
            // Bottom face (y-)
            Triple(floatArrayOf(x, y, z + s), floatArrayOf(0f, -1f, 0f)),
            // Right face (x+)
            Triple(floatArrayOf(x + s, y, z), floatArrayOf(1f, 0f, 0f)),
            // Left face (x-)
            Triple(floatArrayOf(x, y, z), floatArrayOf(-1f, 0f, 0f))
        )
        
        for ((corner, normal) in faces) {
            // Two triangles per face
            val quad = floatArrayOf(
                corner[0], corner[1], corner[2], normal[0], normal[1], normal[2], 0f, 0f,
                corner[0] + if (normal[2] != 0f) s else if (normal[0] != 0f) 0f else s,
                corner[1] + if (normal[1] != 0f) s else 0f,
                corner[2] + if (normal[2] != 0f) 0f else if (normal[0] != 0f) s else 0f,
                normal[0], normal[1], normal[2], 1f, 0f,
                corner[0] + if (normal[2] != 0f) s else if (normal[0] != 0f) 0f else s,
                corner[1] + if (normal[1] != 0f) s else 0f,
                corner[2] + if (normal[2] != 0f) 0f else if (normal[0] != 0f) s else 0f,
                normal[0], normal[1], normal[2], 1f, 0f,
                corner[0] + if (normal[2] != 0f) s else if (normal[0] != 0f) s else 0f,
                corner[1] + if (normal[1] != 0f) s else 0f,
                corner[2] + if (normal[2] != 0f) s else if (normal[0] != 0f) 0f else s,
                normal[0], normal[1], normal[2], 1f, 1f,
                corner[0] + if (normal[2] != 0f) s else if (normal[0] != 0f) s else 0f,
                corner[1] + if (normal[1] != 0f) s else 0f,
                corner[2] + if (normal[2] != 0f) s else if (normal[0] != 0f) 0f else s,
                normal[0], normal[1], normal[2], 1f, 1f,
                corner[0], corner[1] + if (normal[1] != 0f) s else 0f, corner[2],
                normal[0], normal[1], normal[2], 0f, 1f,
                corner[0], corner[1], corner[2], normal[0], normal[1], normal[2], 0f, 0f
            )
            vertices.addAll(quad.toList())
        }
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
