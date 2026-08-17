package com.gpu.devstudio

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import com.gpu.devstudio.ui.theme.GPUDevStudioTheme
import com.gpu.devstudio.ui.screens.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            GPUDevStudioTheme {
                var inGame by remember { mutableStateOf(false) }
                
                if (inGame) {
                    GameScreen(onExit = { inGame = false })
                } else {
                    MenuScreen(onStartGame = { inGame = true })
                }
            }
        }
    }
}
