package com.gpu.devstudio.engine

import android.content.Context
import android.opengl.GLSurfaceView
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class GLContextManager(context: Context) {
    
    private val _glesInfo = MutableStateFlow("")
    val glesInfo: StateFlow<String> = _glesInfo
    
    private val _extensions = MutableStateFlow<List<String>>(emptyList())
    val extensions: StateFlow<List<String>> = _extensions
    
    private val _limits = MutableStateFlow<Map<String, Int>>(emptyMap())
    val limits: StateFlow<Map<String, Int>> = _limits
    
    private var glSurfaceView: GLSurfaceView? = null
    
    fun initialize() {
        glSurfaceView = GLSurfaceView(context).apply {
            setEGLContextClientVersion(3)
            setRenderer(object : GLSurfaceView.Renderer {
                override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
                    queryGLESInfo(gl)
                    queryExtensions(gl)
                    queryLimits(gl)
                }
                
                override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {}
                
                override fun onDrawFrame(gl: GL10?) {
                    // Não precisa renderizar nada
                }
            })
            isVisible = false
            layoutParams = android.view.ViewGroup.LayoutParams(1, 1)
        }
    }
    
    private fun queryGLESInfo(gl: GL10?) {
        val vendor = gl?.glGetString(GL10.GL_VENDOR) ?: "Unknown"
        val renderer = gl?.glGetString(GL10.GL_RENDERER) ?: "Unknown"
        val version = gl?.glGetString(GL10.GL_VERSION) ?: "Unknown"
        val glslVersion = gl?.glGetString(GL10.GL_EXTENSIONS) ?: "N/A"
        
        _glesInfo.value = """
VENDOR: $vendor
RENDERER: $renderer
VERSION: $version
GLSL VERSION: OpenGL ES 3.0+
        """.trimIndent()
    }
    
    private fun queryExtensions(gl: GL10?) {
        val extString = gl?.glGetString(GL10.GL_EXTENSIONS) ?: ""
        _extensions.value = extString.split(" ").filter { it.isNotEmpty() }
    }
    
    private fun queryLimits(gl: GL10?) {
        if (gl == null) return
        
        val maxTextureSize = IntArray(1)
        gl.glGetIntegerv(GL10.GL_MAX_TEXTURE_SIZE, maxTextureSize, 0)
        
        val maxViewportDims = IntArray(2)
        gl.glGetIntegerv(GL10.GL_MAX_VIEWPORT_DIMS, maxViewportDims, 0)
        
        val maxVertexAttribs = IntArray(1)
        gl.glGetIntegerv(GL10.GL_MAX_VERTEX_ATTRIBS, maxVertexAttribs, 0)
        
        val maxTextureImageUnits = IntArray(1)
        gl.glGetIntegerv(GL10.GL_MAX_TEXTURE_IMAGE_UNITS, maxTextureImageUnits, 0)
        
        _limits.value = mapOf(
            "GL_MAX_TEXTURE_SIZE" to maxTextureSize[0],
            "GL_MAX_VIEWPORT_WIDTH" to maxViewportDims[0],
            "GL_MAX_VIEWPORT_HEIGHT" to maxViewportDims[1],
            "GL_MAX_VERTEX_ATTRIBS" to maxVertexAttribs[0],
            "GL_MAX_TEXTURE_IMAGE_UNITS" to maxTextureImageUnits[0]
        )
    }
    
    fun destroy() {
        glSurfaceView?.onPause()
        glSurfaceView?.onDestroy()
    }
}
