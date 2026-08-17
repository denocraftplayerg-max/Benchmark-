package com.gpu.devstudio.engine

class HardwareQuerier {
    init {
        System.loadLibrary("gpu_engine")
    }
    
    external fun getGLESInfoNative(): String
}
