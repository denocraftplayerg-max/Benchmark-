package com.gpu.devstudio.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.gpu.devstudio.engine.VoxelRenderer
import com.gpu.devstudio.ui.theme.*

@Composable
fun ChunkRenderScreen() {
    var fps by remember { mutableStateOf(0f) }
    var renderer: VoxelRenderer? by remember { mutableStateOf(null) }
    
    Column(modifier = Modifier.fillMaxSize().background(BackgroundPrimary)) {
        Card(
            modifier = Modifier.fillMaxWidth().padding(8.dp),
            colors = CardDefaults.cardColors(containerColor = BackgroundCards)
        ) {
            Row(modifier = Modifier.padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("FPS: ${"%.1f".format(fps)}", color = AccentGLES)
                Text("Voxel Engine: ATIVO", color = AccentVulkan)
            }
        }
        
        AndroidView(
            factory = { context ->
                VoxelRenderer(context).apply {
                    onFPSUpdate = { newFps -> fps = newFps }
                    renderer = this
                }
            },
            modifier = Modifier.fillMaxWidth().weight(1f)
        )
        
        Text("Arraste para olhar ao redor", color = TextSecondary, modifier = Modifier.padding(8.dp))
    }
}
