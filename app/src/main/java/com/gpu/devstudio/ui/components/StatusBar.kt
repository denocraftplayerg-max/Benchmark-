package com.gpu.devstudio.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gpu.devstudio.ui.theme.*
import com.gpu.devstudio.engine.SystemMonitor

@Composable
fun StatusBar() {
    val fps by SystemMonitor.fps.collectAsState()
    val gpuTemp by SystemMonitor.gpuTemp.collectAsState()
    val vram by SystemMonitor.vram.collectAsState()
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(BackgroundCards)
            .padding(8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            "●GLES3.2 ●EGL1.5 ●VK1.3 ●CL3.0",
            color = AccentGLES,
            fontSize = 10.sp
        )
        Text(
            "FPS:$fps │ GPU:${gpuTemp}°C │ VRAM:${vram}M",
            color = TextPrimary,
            fontSize = 10.sp
        )
    }
}
