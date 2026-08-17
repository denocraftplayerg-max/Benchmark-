package com.gpu.devstudio.engine

import android.opengl.EGL14
import android.opengl.EGLConfig
import android.opengl.EGLContext
import android.opengl.EGLDisplay
import android.opengl.EGLSurface
import android.opengl.GLES20
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class GLContextManager {
    
    private val _glesInfo = MutableStateFlow("")
    val glesInfo: StateFlow<String> = _glesInfo
    
    private val _extensions = MutableStateFlow<List<String>>(emptyList())
    val extensions: StateFlow<List<String>> = _extensions
    
    private val _limits = MutableStateFlow<Map<String, Int>>(emptyMap())
    val limits: StateFlow<Map<String, Int>> = _limits
    
    private var eglDisplay: EGLDisplay = EGL14.EGL_NO_DISPLAY
    private var eglContext: EGLContext = EGL14.EGL_NO_CONTEXT
    private var eglSurface: EGLSurface = EGL14.EGL_NO_SURFACE

    fun initialize() {
        try {
            eglDisplay = EGL14.eglGetDisplay(EGL14.EGL_DEFAULT_DISPLAY)
            if (eglDisplay == EGL14.EGL_NO_DISPLAY) {
                _glesInfo.value = "Falha ao obter EGLDisplay"
                return
            }

            val version = IntArray(2)
            EGL14.eglInitialize(eglDisplay, version, 0, version, 1)

            val attribList = intArrayOf(
                EGL14.EGL_RED_SIZE, 8,
                EGL14.EGL_GREEN_SIZE, 8,
                EGL14.EGL_BLUE_SIZE, 8,
                EGL14.EGL_ALPHA_SIZE, 8,
                EGL14.EGL_RENDERABLE_TYPE, EGL14.EGL_OPENGL_ES2_BIT,
                EGL14.EGL_NONE
            )
            val configs = arrayOfNulls<EGLConfig>(1)
            val numConfigs = IntArray(1)
            EGL14.eglChooseConfig(eglDisplay, attribList, 0, configs, 0, 1, numConfigs, 0)

            val ctxAttribs = intArrayOf(
                EGL14.EGL_CONTEXT_CLIENT_VERSION, 3, // Solicita GLES 3
                EGL14.EGL_NONE
            )
            eglContext = EGL14.eglCreateContext(eglDisplay, configs[0], EGL14.EGL_NO_CONTEXT, ctxAttribs, 0)
            
            val surfaceAttribs = intArrayOf(EGL14.EGL_NONE)
            eglSurface = EGL14.eglCreatePbufferSurface(eglDisplay, configs[0], surfaceAttribs, 0)
            
            EGL14.eglMakeCurrent(eglDisplay, eglSurface, eglSurface, eglContext)

            // Agora o contexto está ativo, podemos chamar GLES20/GLES30 com segurança
            queryGLESInfo()
            queryExtensions()
            queryLimits()

        } catch (e: Exception) {
            _glesInfo.value = "Erro ao inicializar EGL: ${e.message}"
        }
    }
    
    private fun queryGLESInfo() {
        val vendor = GLES20.glGetString(GLES20.GL_VENDOR) ?: "Unknown"
        val renderer = GLES20.glGetString(GLES20.GL_RENDERER) ?: "Unknown"
        val version = GLES20.glGetString(GLES20.GL_VERSION) ?: "Unknown"
        val glslVersion = GLES20.glGetString(GLES20.GL_SHADING_LANGUAGE_VERSION) ?: "Unknown"
        
        _glesInfo.value = """
VENDOR: $vendor
RENDERER: $renderer
VERSION: $version
GLSL VERSION: $glslVersion
        """.trimIndent()
    }
    
    private fun queryExtensions() {
        val extString = GLES20.glGetString(GLES20.GL_EXTENSIONS) ?: ""
        _extensions.value = extString.split(" ").filter { it.isNotEmpty() }
    }
    
    private fun queryLimits() {
        val maxTextureSize = IntArray(1)
        GLES20.glGetIntegerv(GLES20.GL_MAX_TEXTURE_SIZE, maxTextureSize, 0)
        
        val maxViewportDims = IntArray(2)
        GLES20.glGetIntegerv(GLES20.GL_MAX_VIEWPORT_DIMS, maxViewportDims, 0)
        
        val maxVertexAttribs = IntArray(1)
        GLES20.glGetIntegerv(GLES20.GL_MAX_VERTEX_ATTRIBS, maxVertexAttribs, 0)
        
        val maxTextureImageUnits = IntArray(1)
        GLES20.glGetIntegerv(GLES20.GL_MAX_TEXTURE_IMAGE_UNITS, maxTextureImageUnits, 0)
        
        _limits.value = mapOf(
            "GL_MAX_TEXTURE_SIZE" to maxTextureSize[0],
            "GL_MAX_VIEWPORT_WIDTH" to maxViewportDims[0],
            "GL_MAX_VIEWPORT_HEIGHT" to maxViewportDims[1],
            "GL_MAX_VERTEX_ATTRIBS" to maxVertexAttribs[0],
            "GL_MAX_TEXTURE_IMAGE_UNITS" to maxTextureImageUnits[0]
        )
    }
    
    fun destroy() {
        if (eglDisplay != EGL14.EGL_NO_DISPLAY) {
            EGL14.eglMakeCurrent(eglDisplay, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_CONTEXT)
            if (eglSurface != EGL14.EGL_NO_SURFACE) {
                EGL14.eglDestroySurface(eglDisplay, eglSurface)
            }
            if (eglContext != EGL14.EGL_NO_CONTEXT) {
                EGL14.eglDestroyContext(eglDisplay, eglContext)
            }
            EGL14.eglTerminate(eglDisplay)
        }
        eglDisplay = EGL14.EGL_NO_DISPLAY
        eglContext = EGL14.EGL_NO_CONTEXT
        eglSurface = EGL14.EGL_NO_SURFACE
    }
}
