package com.gpu.devstudio.engine.chunk

class FPSCalculator {
    private var frameCount = 0
    private var lastTime = System.nanoTime()
    private var currentFPS = 0f
    private var frameTime = 0f
    
    fun update(): Float {
        frameCount++
        val currentTime = System.nanoTime()
        val elapsed = (currentTime - lastTime) / 1_000_000_000f
        
        if (elapsed >= 1.0f) {
            currentFPS = frameCount / elapsed
            frameTime = (elapsed * 1000f) / frameCount
            frameCount = 0
            lastTime = currentTime
        }
        
        return currentFPS
    }
    
    fun getFPS(): Float = currentFPS
    fun getFrameTime(): Float = frameTime
}
