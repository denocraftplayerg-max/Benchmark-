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

class VoxelRenderer(context: Context) : GLSurfaceView(context), GLSurfaceView.Renderer {

    private val engine = VoxelEngine()
    private var vertexCount: Int = 0
    private var shaderProgram: Int = 0
    private var mvpLocation: Int = 0

    private val viewMatrix = FloatArray(16)
    private val projMatrix = FloatArray(16)
    private val mvpMatrix = FloatArray(16)
    
    private var cameraX = 8f
    private var cameraY = 12f
    private var cameraZ = 24f
    private var pitch = -30f
    private var yaw = -45f

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

        // 1. Gerar chunk no C++ (Face culling e empacotamento 32-bit feitos aqui)
        val packedData = engine.generateChunk()
        vertexCount = packedData.size

        // 2. Upload para a GPU
        val vbo = IntArray(1)
        GLES30.glGenBuffers(1, vbo, 0)
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, vbo[0])
        
        val buffer = ByteBuffer.allocateDirect(packedData.size * 4)
            .order(ByteOrder.nativeOrder())
            .asIntBuffer()
        buffer.put(packedData)
        buffer.flip()
        
        GLES30.glBufferData(GLES30.GL_ARRAY_BUFFER, buffer.capacity() * 4, buffer, GLES30.GL_STATIC_DRAW)
        
        // 3. O SEGREDO: glVertexAttribIPointer (com 'I') para inteiros puros sem conversão
        GLES30.glVertexAttribIPointer(0, 1, GLES30.GL_UNSIGNED_INT, 4, 0)
        GLES30.glEnableVertexAttribArray(0)
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        GLES30.glViewport(0, 0, width, height)
        val ratio = width.toFloat() / height.toFloat()
        Matrix.perspectiveM(projMatrix, 0, 60f, ratio, 0.1f, 100f)
    }

    override fun onDrawFrame(gl: GL10?) {
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT or GLES30.GL_DEPTH_BUFFER_BIT)

        updateCamera()
        GLES30.glUseProgram(shaderProgram)
        GLES30.glUniformMatrix4fv(mvpLocation, 1, false, mvpMatrix, 0)
        GLES30.glDrawArrays(GLES30.GL_TRIANGLES, 0, vertexCount)

        calculateFPS()
    }

    private fun updateCamera() {
        Matrix.setLookAtM(viewMatrix, 0, cameraX, cameraY, cameraZ,
            cameraX + Math.cos(Math.toRadians(yaw.toDouble())).toFloat() * Math.cos(Math.toRadians(pitch.toDouble())).toFloat(),
            cameraY + Math.sin(Math.toRadians(pitch.toDouble())).toFloat(),
            cameraZ + Math.sin(Math.toRadians(yaw.toDouble())).toFloat() * Math.cos(Math.toRadians(pitch.toDouble())).toFloat(),
            0f, 1f, 0f)
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

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                lastTouchX = event.x
                lastTouchY = event.y
                isDragging = true
            }
            MotionEvent.ACTION_MOVE -> {
                if (isDragging) {
                    yaw += (event.x - lastTouchX) * 0.5f
                    pitch -= (event.y - lastTouchY) * 0.5f
                    pitch = pitch.coerceIn(-89f, 89f)
                    lastTouchX = event.x
                    lastTouchY = event.y
                }
            }
            MotionEvent.ACTION_UP -> isDragging = false
        }
        return true
    }

    private fun createProgram(vertexAsset: String, fragmentAsset: String): Int {
        val vs = GLES30.glCreateShader(GLES30.GL_VERTEX_SHADER)
        GLES30.glShaderSource(vs, context.assets.open(vertexAsset).bufferedReader().readText())
        GLES30.glCompileShader(vs)
        
        val fs = GLES30.glCreateShader(GLES30.GL_FRAGMENT_SHADER)
        GLES30.glShaderSource(fs, context.assets.open(fragmentAsset).bufferedReader().readText())
        GLES30.glCompileShader(fs)
        
        val program = GLES30.glCreateProgram()
        GLES30.glAttachShader(program, vs)
        GLES30.glAttachShader(program, fs)
        GLES30.glLinkProgram(program)
        return program
    }
}
