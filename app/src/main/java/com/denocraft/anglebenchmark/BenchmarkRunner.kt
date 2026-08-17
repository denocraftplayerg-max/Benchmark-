package com.denocraft.anglebenchmark

class BenchmarkRunner {
    init {
        // Força o carregamento das nossas libs ANGLE antes de qualquer coisa
        System.loadLibrary("EGL_angle")
        System.loadLibrary("GLESv2_angle")
        System.loadLibrary("benchmark_engine")
    }

    external fun runNativeBenchmark(): String
}
