package com.gpu.devstudio.engine

import android.content.Context
import android.graphics.BitmapFactory
import android.opengl.GLES30
import android.opengl.GLSurfaceView
import android.opengl.GLUtils
import android.opengl.Matrix
import java.nio.ByteBuffer
import java.nio.ByteOrder
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10
import kotlin.math.cos
import kotlin.math.sin

class VoxelRenderer(private val ctx: Context) : GLSurfaceView(ctx), GLSurfaceView.Renderer {

    private val engine = VoxelEngine()
    private var vertexCount = 0
    private var program = 0
    private var mvpLoc = 0
    private var atlasLoc = 0
    private var textureId = 0

    private val view = FloatArray(16)
    private val proj = FloatArray(16)
    private val mvp  = FloatArray(16)

    // Câmera começa na frente do chunk, olhando para ele
    private var camX = 8f
    private var camY = 10f
    private var camZ = -4f
    private var pitch = -10f
    private var yaw   = 90f

    private var velY = 0f
    private val gravity = -20f

    var moveForward  = false
    var moveBackward = false
    var moveLeft     = false
    var moveRight    = false

    private var frameCount = 0
    private var lastTime   = System.nanoTime()
    var onFPSUpdate: ((Float) -> Unit)? = null

    init {
        setEGLContextClientVersion(3)
        setRenderer(this)
        renderMode = RENDERMODE_CONTINUOUSLY
    }

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        GLES30.glClearColor(0.4f, 0.6f, 1.0f, 1.0f)
        GLES30.glEnable(GLES30.GL_DEPTH_TEST)
        GLES30.glEnable(GLES30.GL_CULL_FACE)

        program = buildProgram(
            ctx.assets.open("voxel.vert").bufferedReader().readText(),
            ctx.assets.open("voxel.frag").bufferedReader().readText()
        )
        mvpLoc  = GLES30.glGetUniformLocation(program, "u_MVP")
        atlasLoc = GLES30.glGetUniformLocation(program, "u_Atlas")

        textureId = loadAtlas()

        val data = engine.generateChunk()
        vertexCount = data.size

        val vbo = IntArray(1)
        GLES30.glGenBuffers(1, vbo, 0)
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, vbo[0])
        val buf = ByteBuffer.allocateDirect(data.size * 4)
            .order(ByteOrder.nativeOrder()).asIntBuffer()
        buf.put(data).flip()
        GLES30.glBufferData(GLES30.GL_ARRAY_BUFFER, buf.capacity() * 4, buf, GLES30.GL_STATIC_DRAW)
        GLES30.glVertexAttribIPointer(0, 1, GLES30.GL_UNSIGNED_INT, 4, 0)
        GLES30.glEnableVertexAttribArray(0)
    }

    override fun onSurfaceChanged(gl: GL10?, w: Int, h: Int) {
        GLES30.glViewport(0, 0, w, h)
        Matrix.perspectiveM(proj, 0, 70f, w.toFloat() / h.toFloat(), 0.1f, 200f)
    }

    override fun onDrawFrame(gl: GL10?) {
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT or GLES30.GL_DEPTH_BUFFER_BIT)
        updatePhysics()
        updateCamera()

        GLES30.glUseProgram(program)
        GLES30.glActiveTexture(GLES30.GL_TEXTURE0)
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, textureId)
        GLES30.glUniform1i(atlasLoc, 0)
        GLES30.glUniformMatrix4fv(mvpLoc, 1, false, mvp, 0)
        GLES30.glDrawArrays(GLES30.GL_TRIANGLES, 0, vertexCount)

        frameCount++
        val now = System.nanoTime()
        if (now - lastTime >= 1_000_000_000L) {
            onFPSUpdate?.invoke(frameCount.toFloat())
            frameCount = 0
            lastTime = now
        }
    }

    private fun updatePhysics() {
        val dt = 0.016f
        velY += gravity * dt
        camY += velY * dt
        if (camY < 12f) { camY = 12f; velY = 0f }

        val speed = 0.12f
        val yr = Math.toRadians(yaw.toDouble())
        if (moveForward)  { camX += (cos(yr)*speed).toFloat(); camZ += (sin(yr)*speed).toFloat() }
        if (moveBackward) { camX -= (cos(yr)*speed).toFloat(); camZ -= (sin(yr)*speed).toFloat() }
        if (moveLeft)     { camX += (cos(yr+Math.PI/2)*speed).toFloat(); camZ += (sin(yr+Math.PI/2)*speed).toFloat() }
        if (moveRight)    { camX -= (cos(yr+Math.PI/2)*speed).toFloat(); camZ -= (sin(yr+Math.PI/2)*speed).toFloat() }
    }

    private fun updateCamera() {
        val yr = Math.toRadians(yaw.toDouble())
        val pr = Math.toRadians(pitch.toDouble())
        val tx = (camX + cos(yr)*cos(pr)).toFloat()
        val ty = (camY + sin(pr)).toFloat()
        val tz = (camZ + sin(yr)*cos(pr)).toFloat()
        Matrix.setLookAtM(view, 0, camX, camY, camZ, tx, ty, tz, 0f, 1f, 0f)
        Matrix.multiplyMM(mvp, 0, proj, 0, view, 0)
    }

    fun onCameraDrag(dx: Float, dy: Float) {
        yaw   += dx * 0.3f
        pitch  = (pitch - dy * 0.3f).coerceIn(-89f, 89f)
    }

    private fun loadAtlas(): Int {
        val ids = IntArray(1)
        GLES30.glGenTextures(1, ids, 0)
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, ids[0])
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MIN_FILTER, GLES30.GL_NEAREST)
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MAG_FILTER, GLES30.GL_NEAREST)
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_S, GLES30.GL_CLAMP_TO_EDGE)
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_T, GLES30.GL_CLAMP_TO_EDGE)
        val bmp = BitmapFactory.decodeStream(ctx.assets.open("atlas.png"))
        GLUtils.texImage2D(GLES30.GL_TEXTURE_2D, 0, bmp, 0)
        bmp.recycle()
        return ids[0]
    }

    private fun buildProgram(vs: String, fs: String): Int {
        fun compile(type: Int, src: String): Int {
            val s = GLES30.glCreateShader(type)
            GLES30.glShaderSource(s, src)
            GLES30.glCompileShader(s)
            val ok = IntArray(1)
            GLES30.glGetShaderiv(s, GLES30.GL_COMPILE_STATUS, ok, 0)
            if (ok[0] == 0) android.util.Log.e("VoxelRenderer",
                "Shader error: ${GLES30.glGetShaderInfoLog(s)}")
            return s
        }
        val p = GLES30.glCreateProgram()
        GLES30.glAttachShader(p, compile(GLES30.GL_VERTEX_SHADER, vs))
        GLES30.glAttachShader(p, compile(GLES30.GL_FRAGMENT_SHADER, fs))
        GLES30.glLinkProgram(p)
        return p
    }
}
