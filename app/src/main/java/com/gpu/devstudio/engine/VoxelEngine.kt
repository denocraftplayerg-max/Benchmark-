package com.gpu.devstudio.engine

class VoxelEngine {
    companion object {
        init {
            System.loadLibrary("voxel_engine")
        }
    }
    // Retorna o array de ints compactados diretamente (mais seguro que ponteiros no Android)
    external fun generateChunk(): IntArray
}
