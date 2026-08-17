package com.gpu.devstudio.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.gpu.devstudio.engine.GLContextManager
import com.gpu.devstudio.ui.theme.*

@Composable
fun GLESScreen() {
    val glManager = remember { GLContextManager() }
    
    val glesInfo by glManager.glesInfo.collectAsState()
    val extensions by glManager.extensions.collectAsState()
    val limits by glManager.limits.collectAsState()
    
    LaunchedEffect(Unit) {
        glManager.initialize()
    }
    
    DisposableEffect(Unit) {
        onDispose {
            glManager.destroy()
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundPrimary)
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text("OpenGL ES (Dados Reais)", color = AccentGLES, style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(16.dp))
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = BackgroundCards)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("INFORMAÇÕES DO HARDWARE", color = AccentGLES, style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
                if (glesInfo.isEmpty()) {
                    CircularProgressIndicator(color = AccentGLES, modifier = Modifier.align(androidx.compose.ui.Alignment.CenterHorizontally))
                } else {
                    Text(glesInfo, color = TextPrimary, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = BackgroundCards)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("LIMITES DO HARDWARE", color = AccentGLES, style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
                if (limits.isEmpty()) {
                    Text("Carregando...", color = TextSecondary)
                } else {
                    limits.forEach { (key, value) ->
                        Text("$key: $value", color = TextPrimary, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = BackgroundCards)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("EXTENSÕES DISPONÍVEIS (${extensions.size})", color = AccentGLES, style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
                if (extensions.isEmpty()) {
                    Text("Carregando...", color = TextSecondary)
                } else {
                    extensions.take(20).forEach { ext ->
                        Text("• $ext", color = TextPrimary, style = MaterialTheme.typography.bodySmall)
                    }
                    if (extensions.size > 20) {
                        Text("... e mais ${extensions.size - 20} extensões", color = TextSecondary, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}
