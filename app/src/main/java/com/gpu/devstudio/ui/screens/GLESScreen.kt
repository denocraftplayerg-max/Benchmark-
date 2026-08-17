package com.gpu.devstudio.ui.screens

import android.opengl.GLSurfaceView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.gpu.devstudio.ui.theme.*
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

@Composable
fun GLESScreen() {
    var glesInfo by remember { mutableStateOf("Inicializando...") }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundPrimary)
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text("OpenGL ES", color = AccentGLES, style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(16.dp))
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = BackgroundCards)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Informações do Contexto", color = TextPrimary)
                Spacer(modifier = Modifier.height(8.dp))
                Text(glesInfo, color = TextSecondary)
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        AndroidView(
            factory = { context ->
                GLSurfaceView(context).apply {
                    setEGLContextClientVersion(3)
                    setRenderer(object : GLSurfaceView.Renderer {
                        override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
                            val vendor = gl?.glGetString(GL10.GL_VENDOR) ?: "N/A"
                            val renderer = gl?.glGetString(GL10.GL_RENDERER) ?: "N/A"
                            val version = gl?.glGetString(GL10.GL_VERSION) ?: "N/A"
                            val extensions = gl?.glGetString(GL10.GL_EXTENSIONS) ?: "N/A"
                            
                            glesInfo = """
Vendor: $vendor
Renderer: $renderer
Version: $version
Extensions: ${extensions.split(" ").size} disponíveis
                            """.trimIndent()
                        }
                        override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {}
                        override fun onDrawFrame(gl: GL10?) {}
                    })
                }
            },
            modifier = Modifier.size(1.dp)
        )
    }
}
