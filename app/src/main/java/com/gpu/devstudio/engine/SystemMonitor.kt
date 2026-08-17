package com.gpu.devstudio.engine

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

object SystemMonitor {
    private val _fps = MutableStateFlow(60)
    val fps: StateFlow<Int> = _fps
    
    private val _gpuTemp = MutableStateFlow(45)
    val gpuTemp: StateFlow<Int> = _gpuTemp
    
    private val _vram = MutableStateFlow(512)
    val vram: StateFlow<Int> = _vram
    
    fun updateMetrics(fps: Int, temp: Int, vram: Int) {
        _fps.value = fps
        _gpuTemp.value = temp
        _vram.value = vram
    }
}
