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
fun HoldButton(label: String, onPress: () -> Unit, onRelease: () -> Unit) {
    Button(
        onClick = {},
        modifier = Modifier.pointerInput(Unit) {
            awaitPointerEventScope {
                while (true) {
                    val down = awaitPointerEvent()
                    if (down.changes.any { it.pressed }) {
                        onPress()
                        while (true) {
                            val up = awaitPointerEvent()
                            if (up.changes.all { !it.pressed }) { onRelease(); break }
                        }
                    }
                }
            }
        }
    ) { Text(label) }
}

@Composable
fun GameScreen(onExit: () -> Unit) {
    var fps by remember { mutableStateOf(0f) }
    var renderer by remember { mutableStateOf<VoxelRenderer?>(null) }

    Box(modifier = Modifier.fillMaxSize().background(BackgroundPrimary)) {

        AndroidView(
            factory = { context ->
                VoxelRenderer(context).also {
                    it.onFPSUpdate = { f -> fps = f }
                    renderer = it
                }
            },
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectDragGestures { _, drag ->
                        renderer?.onCameraDrag(drag.x, drag.y)
                    }
                }
        )

        // HUD FPS
        Card(
            modifier = Modifier.align(Alignment.TopCenter).padding(12.dp),
            colors = CardDefaults.cardColors(containerColor = BackgroundCards.copy(alpha = 0.85f))
        ) {
            Row(Modifier.padding(8.dp, 4.dp)) {
                Text("FPS: ${"%.0f".format(fps)}", color = AccentGLES)
                Spacer(Modifier.width(16.dp))
                Text("QUANEGGAES4D", color = AccentVulkan)
            }
        }

        // Controles
        Column(
            modifier = Modifier.align(Alignment.BottomStart).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Spacer(Modifier.width(56.dp))
                HoldButton("▲",
                    onPress   = { renderer?.moveForward  = true },
                    onRelease = { renderer?.moveForward  = false })
            }
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                HoldButton("◀",
                    onPress   = { renderer?.moveLeft     = true },
                    onRelease = { renderer?.moveLeft     = false })
                HoldButton("▼",
                    onPress   = { renderer?.moveBackward = true },
                    onRelease = { renderer?.moveBackward = false })
                HoldButton("▶",
                    onPress   = { renderer?.moveRight    = true },
                    onRelease = { renderer?.moveRight    = false })
            }
        }

        Button(
            onClick = onExit,
            modifier = Modifier.align(Alignment.TopEnd).padding(12.dp)
        ) { Text("✕") }
    }
}
