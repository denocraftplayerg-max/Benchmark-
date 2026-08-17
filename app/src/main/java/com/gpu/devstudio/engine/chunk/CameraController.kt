package com.gpu.devstudio.engine.chunk

import android.opengl.Matrix

class CameraController {
    var position = floatArrayOf(8f, 20f, 30f)
    var yaw = -45f // Rotação horizontal
    var pitch = -30f // Rotação vertical
    
    private val viewMatrix = FloatArray(16)
    private val projectionMatrix = FloatArray(16)
    private val mvpMatrix = FloatArray(16)
    private val tempMatrix = FloatArray(16)
    
    fun updateProjection(width: Int, height: Int) {
        val aspect = width.toFloat() / height.toFloat()
        Matrix.perspectiveM(projectionMatrix, 0, 60f, aspect, 0.1f, 1000f)
    }
    
    fun updateView() {
        // Calcular direção da câmera
        val yawRad = Math.toRadians(yaw.toDouble()).toFloat()
        val pitchRad = Math.toRadians(pitch.toDouble()).toFloat()
        
        val frontX = Math.cos(pitchRad.toDouble()).toFloat() * Math.cos(yawRad.toDouble()).toFloat()
        val frontY = Math.sin(pitchRad.toDouble()).toFloat()
        val frontZ = Math.cos(pitchRad.toDouble()).toFloat() * Math.sin(yawRad.toDouble()).toFloat()
        
        val targetX = position[0] + frontX
        val targetY = position[1] + frontY
        val targetZ = position[2] + frontZ
        
        Matrix.setLookAtM(viewMatrix, 0,
            position[0], position[1], position[2],
            targetX, targetY, targetZ,
            0f, 1f, 0f
        )
    }
    
    fun getMVP(modelMatrix: FloatArray): FloatArray {
        updateView()
        Matrix.multiplyMM(tempMatrix, 0, projectionMatrix, 0, viewMatrix, 0)
        Matrix.multiplyMM(mvpMatrix, 0, tempMatrix, 0, modelMatrix, 0)
        return mvpMatrix
    }
    
    fun moveForward(speed: Float) {
        val yawRad = Math.toRadians(yaw.toDouble()).toFloat()
        position[0] += Math.cos(yawRad.toDouble()).toFloat() * speed
        position[2] += Math.sin(yawRad.toDouble()).toFloat() * speed
    }
    
    fun moveBackward(speed: Float) {
        val yawRad = Math.toRadians(yaw.toDouble()).toFloat()
        position[0] -= Math.cos(yawRad.toDouble()).toFloat() * speed
        position[2] -= Math.sin(yawRad.toDouble()).toFloat() * speed
    }
    
    fun moveLeft(speed: Float) {
        val yawRad = Math.toRadians(yaw.toDouble()).toFloat()
        position[0] -= Math.cos(yawRad.toDouble() + Math.PI / 2).toFloat() * speed
        position[2] -= Math.sin(yawRad.toDouble() + Math.PI / 2).toFloat() * speed
    }
    
    fun moveRight(speed: Float) {
        val yawRad = Math.toRadians(yaw.toDouble()).toFloat()
        position[0] += Math.cos(yawRad.toDouble() + Math.PI / 2).toFloat() * speed
        position[2] += Math.sin(yawRad.toDouble() + Math.PI / 2).toFloat() * speed
    }
    
    fun rotate(dx: Float, dy: Float) {
        yaw += dx * 0.5f
        pitch -= dy * 0.5f
        pitch = pitch.coerceIn(-89f, 89f)
    }
}
