package com.gpu.devstudio.engine

class VoxelEngine {
    companion object {
        init {
            System.loadLibrary("voxel_engine")
        }
    }
    // O C++ gera os dados e retorna um IntArray seguro para o Kotlin
    external fun generateChunk(): IntArray
}
