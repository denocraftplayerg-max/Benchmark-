package com.gpu.devstudio.engine

import android.content.Context
import android.opengl.GLES30
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import android.view.MotionEvent
import java.nio.ByteBuffer
import java.nio.ByteOrder
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10
import kotlin.math.cos
import kotlin.math.sin

class VoxelRenderer(context: Context) : GLSurfaceView(context), GLSurfaceView.Renderer {
    
    private val engine = VoxelEngine()
    private var vertexCount: Int = 0
    private var shaderProgram: Int = 0
    private var mvpLocation: Int = 0
    
    private val viewMatrix = FloatArray(16)
    private val projMatrix = FloatArray(16)
    private val mvpMatrix = FloatArray(16)
    
    private var cameraX = 8f
    private var cameraY = 15f
    private var cameraZ = 24f
    private var pitch = -20f
    private var yaw = -45f
    
    private var velocityY = 0f
    private val gravity = -20f
    
    private var moveForward = false
    private var moveBackward = false
    private var moveLeft = false
    private var moveRight = false
    
    private var lastTouchX = 0f
    private var lastTouchY = 0f
    private var isDragging = false
    
    private var fps = 0f
    private var frameCount = 0
    private var lastTime = System.nanoTime()
    
    var onFPSUpdate: ((Float) -> Unit)? = null
    
    init {
        setEGLContextClientVersion(3)
        setRenderer(this)
        renderMode = RENDERMODE_CONTINUOUSLY
    }
    
    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        GLES30.glClearColor(0.5f, 0.7f, 1.0f, 1.0f)
        GLES30.glEnable(GLES30.GL_DEPTH_TEST)
        GLES30.glEnable(GLES30.GL_CULL_FACE)
        
        shaderProgram = createProgram("voxel.vert", "voxel.frag")
        mvpLocation = GLES30.glGetUniformLocation(shaderProgram, "u_MVP")
        
        val packedData = engine.generateChunk()
        vertexCount = packedData.size
        
        val vbo = IntArray(1)
        GLES30.glGenBuffers(1, vbo, 0)
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, vbo[0])
        
        val buffer = ByteBuffer.allocateDirect(packedData.size * 4)
            .order(ByteOrder.nativeOrder())
            .asIntBuffer()
        buffer.put(packedData)
        buffer.flip()
        
        GLES30.glBufferData(GLES30.GL_ARRAY_BUFFER, buffer.capacity() * 4, buffer, GLES30.GL_STATIC_DRAW)
        GLES30.glVertexAttribIPointer(0, 1, GLES30.GL_UNSIGNED_INT, 4, 0)
        GLES30.glEnableVertexAttribArray(0)
    }
    
    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        GLES30.glViewport(0, 0, width, height)
        val ratio = width.toFloat() / height.toFloat()
        Matrix.perspectiveM(projMatrix, 0, 70f, ratio, 0.1f, 200f)
    }
    
    override fun onDrawFrame(gl: GL10?) {
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT or GLES30.GL_DEPTH_BUFFER_BIT)
        
        updatePhysics()
        updateCamera()
        
        GLES30.glUseProgram(shaderProgram)
        GLES30.glUniformMatrix4fv(mvpLocation, 1, false, mvpMatrix, 0)
        GLES30.glDrawArrays(GLES30.GL_TRIANGLES, 0, vertexCount)
        
        calculateFPS()
    }
    
    private fun updatePhysics() {
        val deltaTime = 0.016f
        velocityY += gravity * deltaTime
        cameraY += velocityY * deltaTime
        
        if (cameraY < 14f) {
            cameraY = 14f
            velocityY = 0f
        }
        
        val speed = 0.15f
        val yawRad = Math.toRadians(yaw.toDouble())
        
        if (moveForward) {
            cameraX += (cos(yawRad) * speed).toFloat()
            cameraZ += (sin(yawRad) * speed).toFloat()
        }
        if (moveBackward) {
            cameraX -= (cos(yawRad) * speed).toFloat()
            cameraZ -= (sin(yawRad) * speed).toFloat()
        }
        if (moveLeft) {
            cameraX -= (cos(yawRad + Math.PI / 2.0) * speed).toFloat()
            cameraZ -= (sin(yawRad + Math.PI / 2.0) * speed).toFloat()
        }
        if (moveRight) {
            cameraX += (cos(yawRad + Math.PI / 2.0) * speed).toFloat()
            cameraZ += (sin(yawRad + Math.PI / 2.0) * speed).toFloat()
        }
    }
    
    private fun updateCamera() {
        val yawRad = Math.toRadians(yaw.toDouble())
        val pitchRad = Math.toRadians(pitch.toDouble())
        
        val targetX = (cameraX + cos(yawRad) * cos(pitchRad)).toFloat()
        val targetY = (cameraY + sin(pitchRad)).toFloat()
        val targetZ = (cameraZ + sin(yawRad) * cos(pitchRad)).toFloat()
        
        Matrix.setLookAtM(viewMatrix, 0,
            cameraX, cameraY, cameraZ,
            targetX, targetY, targetZ,
            0f, 1f, 0f
        )
        Matrix.multiplyMM(mvpMatrix, 0, projMatrix, 0, viewMatrix, 0)
    }
    
    private fun calculateFPS() {
        frameCount++
        val currentTime = System.nanoTime()
        if (currentTime - lastTime >= 1_000_000_000L) {
            fps = frameCount.toFloat()
            frameCount = 0
            lastTime = currentTime
            onFPSUpdate?.invoke(fps)
        }
    }
    
    fun onCameraDrag(dx: Float, dy: Float) {
        yaw += dx * 0.3f
        pitch -= dy * 0.3f
        pitch = pitch.coerceIn(-89f, 89f)
    }
    
    fun moveForward() { moveForward = true }
    fun moveBackward() { moveBackward = true }
    fun moveLeft() { moveLeft = true }
    fun moveRight() { moveRight = true }
    
    fun stopMoveForward() { moveForward = false }
    fun stopMoveBackward() { moveBackward = false }
    fun stopMoveLeft() { moveLeft = false }
    fun stopMoveRight() { moveRight = false }
    
    private fun createProgram(v: String, f: String): Int {
        val vs = GLES30.glCreateShader(GLES30.GL_VERTEX_SHADER)
        GLES30.glShaderSource(vs, context.assets.open(v).bufferedReader().readText())
        GLES30.glCompileShader(vs)
        val fs = GLES30.glCreateShader(GLES30.GL_FRAGMENT_SHADER)
        GLES30.glShaderSource(fs, context.assets.open(f).bufferedReader().readText())
        GLES30.glCompileShader(fs)
        val p = GLES30.glCreateProgram()
        GLES30.glAttachShader(p, vs)
        GLES30.glAttachShader(p, fs)
        GLES30.glLinkProgram(p)
        return p
    }
}
