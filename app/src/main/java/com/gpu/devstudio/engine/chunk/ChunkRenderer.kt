package com.gpu.devstudio.engine.chunk

import android.content.Context
import android.opengl.GLES30
import android.opengl.GLSurfaceView
import android.view.MotionEvent
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class ChunkRenderer(context: Context) : GLSurfaceView(context), GLSurfaceView.Renderer {
    
    private val shader = ShaderProgram()
    private val chunkMesh = ChunkMesh(16)
    private val camera = CameraController()
    private val fpsCalculator = FPSCalculator()
    
    private var textureId = 0
    private val modelMatrix = FloatArray(16)
    
    // Controles touch
    private var lastTouchX = 0f
    private var lastTouchY = 0f
    private var isDragging = false
    
    // Callback para UI
    var onFPSUpdate: ((Float, Float) -> Unit)? = null
    
    init {
        setEGLContextClientVersion(3)
        setRenderer(this)
        renderMode = RENDERMODE_CONTINUOUSLY
    }
    
    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        GLES30.glClearColor(0.5f, 0.7f, 1.0f, 1.0f)
        GLES30.glEnable(GLES30.GL_DEPTH_TEST)
        GLES30.glEnable(GLES30.GL_CULL_FACE)
        
        // Compilar shaders
        if (!shader.compile()) {
            throw RuntimeException("Falha ao compilar shaders")
        }
        
        // Gerar chunk
        chunkMesh.generate()
        
        // Criar textura procedural (checkerboard)
        createProceduralTexture()
    }
    
    private fun createProceduralTexture() {
        val textureIds = IntArray(1)
        GLES30.glGenTextures(1, textureIds, 0)
        textureId = textureIds[0]
        
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, textureId)
        
        // Criar textura 64x64 com padrão checkerboard
        val pixels = IntArray(64 * 64)
        for (y in 0 until 64) {
            for (x in 0 until 64) {
                val color = if ((x / 8 + y / 8) % 2 == 0) 0xFF4CAF50.toInt() else 0xFF8B4513.toInt()
                pixels[y * 64 + x] = color
            }
        }
        
        val buffer = java.nio.ByteBuffer.allocateDirect(pixels.size * 4)
        buffer.order(java.nio.ByteOrder.nativeOrder())
        for (pixel in pixels) {
            buffer.putInt(pixel)
        }
        buffer.flip()
        
        GLES30.glTexImage2D(GLES30.GL_TEXTURE_2D, 0, GLES30.GL_RGBA, 64, 64, 0,
            GLES30.GL_RGBA, GLES30.GL_UNSIGNED_BYTE, buffer)
        
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MIN_FILTER, GLES30.GL_LINEAR)
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MAG_FILTER, GLES30.GL_LINEAR)
    }
    
    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        GLES30.glViewport(0, 0, width, height)
        camera.updateProjection(width, height)
    }
    
    override fun onDrawFrame(gl: GL10?) {
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT or GLES30.GL_DEPTH_BUFFER_BIT)
        
        shader.use()
        
        // Setar uniforms
        val mvp = camera.getMVP(modelMatrix)
        val mvpLoc = GLES30.glGetUniformLocation(shader.programId, "uMVP")
        GLES30.glUniformMatrix4fv(mvpLoc, 1, false, mvp, 0)
        
        val modelLoc = GLES30.glGetUniformLocation(shader.programId, "uModel")
        GLES30.glUniformMatrix4fv(modelLoc, 1, false, modelMatrix, 0)
        
        val lightDirLoc = GLES30.glGetUniformLocation(shader.programId, "uLightDir")
        GLES30.glUniform3f(lightDirLoc, 0.5f, 1.0f, 0.3f)
        
        val cameraPosLoc = GLES30.glGetUniformLocation(shader.programId, "uCameraPos")
        GLES30.glUniform3fv(cameraPosLoc, 1, camera.position, 0)
        
        // Bind texture
        GLES30.glActiveTexture(GLES30.GL_TEXTURE0)
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, textureId)
        val textureLoc = GLES30.glGetUniformLocation(shader.programId, "uTexture")
        GLES30.glUniform1i(textureLoc, 0)
        
        // Renderizar chunk
        chunkMesh.render()
        
        // Calcular FPS
        val fps = fpsCalculator.update()
        val frameTime = fpsCalculator.getFrameTime()
        onFPSUpdate?.invoke(fps, frameTime)
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
                    val dx = event.x - lastTouchX
                    val dy = event.y - lastTouchY
                    camera.rotate(dx, dy)
                    lastTouchX = event.x
                    lastTouchY = event.y
                }
            }
            MotionEvent.ACTION_UP -> {
                isDragging = false
            }
        }
        return true
    }
    
    fun moveForward() = camera.moveForward(0.5f)
    fun moveBackward() = camera.moveBackward(0.5f)
    fun moveLeft() = camera.moveLeft(0.5f)
    fun moveRight() = camera.moveRight(0.5f)
    
    override fun onPause() {
        super.onPause()
        shader.dispose()
        chunkMesh.dispose()
    }
}
