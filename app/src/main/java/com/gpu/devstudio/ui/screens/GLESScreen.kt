package com.gpu.devstudio.ui.screens

import android.opengl.GLES30
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.gpu.devstudio.ui.theme.*

@Composable
fun GLESScreen() {
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
                Text("Vendor: PowerVR / Adreno / Mali", color = TextSecondary)
                Text("Renderer: PowerVR Rogue GE8320", color = TextSecondary)
                Text("Version: OpenGL ES 3.2", color = TextSecondary)
                Text("Extensions: 142 disponíveis", color = TextSecondary)
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = BackgroundCards)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Limites GLES", color = TextPrimary)
                Spacer(modifier = Modifier.height(8.dp))
                Text("GL_MAX_TEXTURE_SIZE: 16384", color = TextSecondary)
                Text("GL_MAX_VIEWPORT_DIMS: 16384x16384", color = TextSecondary)
                Text("GL_MAX_VERTEX_ATTRIBS: 16", color = TextSecondary)
                Text("GL_MAX_TEXTURE_IMAGE_UNITS: 16", color = TextSecondary)
            }
        }
    }
}
