package com.gpu.devstudio

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import com.gpu.devstudio.ui.screens.GameScreen
import com.gpu.devstudio.ui.screens.MenuScreen
import com.gpu.devstudio.ui.theme.GPUDevStudioTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Manter a tela ligada durante o jogo
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        
        setContent {
            GPUDevStudioTheme {
                val inGame = remember { mutableStateOf(false) }
                
                if (inGame.value) {
                    GameScreen(onExit = { inGame.value = false })
                } else {
                    MenuScreen(onStartGame = { inGame.value = true })
                }
            }
        }
    }
}
