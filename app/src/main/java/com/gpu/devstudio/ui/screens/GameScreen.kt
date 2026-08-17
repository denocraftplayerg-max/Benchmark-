package com.gpu.devstudio.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.gpu.devstudio.engine.VoxelRenderer
import com.gpu.devstudio.ui.theme.*

@Composable
fun GameScreen(onExit: () -> Unit) {
    var fps by remember { mutableStateOf(0f) }
    var renderer: VoxelRenderer? by remember { mutableStateOf(null) }
    
    Box(modifier = Modifier.fillMaxSize().background(BackgroundPrimary)) {
        AndroidView(
            factory = { context ->
                VoxelRenderer(context).apply {
                    onFPSUpdate = { newFps -> fps = newFps }
                    renderer = this
                }
            },
            modifier = Modifier.fillMaxSize()
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        renderer?.onCameraDrag(dragAmount.x, dragAmount.y)
                    }
                }
        )
        
        Card(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = BackgroundCards.copy(alpha = 0.8f))
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("FPS: ${"%.0f".format(fps)}", color = AccentGLES)
                Spacer(modifier = Modifier.width(16.dp))
                Text("QUANEGGAES4D", color = AccentVulkan)
            }
        }
        
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(16.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { renderer?.moveLeft() },
                    modifier = Modifier.pointerInput(Unit) {
                        detectDragGestures { _, _ -> }
                    }
                ) { Text("◀") }
                Button(
                    onClick = { renderer?.moveRight() },
                    modifier = Modifier.pointerInput(Unit) {
                        detectDragGestures { _, _ -> }
                    }
                ) { Text("▶") }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { renderer?.moveBackward() },
                    modifier = Modifier.pointerInput(Unit) {
                        detectDragGestures { _, _ -> }
                    }
                ) { Text("▼") }
                Button(
                    onClick = { renderer?.moveForward() },
                    modifier = Modifier.pointerInput(Unit) {
                        detectDragGestures { _, _ -> }
                    }
                ) { Text("▲") }
            }
        }
        
        Button(
            onClick = onExit,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
        ) {
            Text("✕")
        }
    }
}
