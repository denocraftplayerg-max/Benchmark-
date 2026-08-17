package com.gpu.devstudio

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.gpu.devstudio.ui.theme.GPUDevStudioTheme
import com.gpu.devstudio.ui.theme.BackgroundCards
import com.gpu.devstudio.ui.theme.TextPrimary
import com.gpu.devstudio.ui.theme.AccentGLES
import com.gpu.devstudio.ui.screens.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            GPUDevStudioTheme {
                MainScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    var selectedTab by remember { mutableIntStateOf(0) }
    
    val tabs = listOf(
        "GLES", "EGL", "VULKAN", "OPENCL",
        "TESTS", "COMPILER", "LINKER", "PROJECTS",
        "ANGLE", "API SWITCH", "BENCH", "MC-BENCH",
        "AI", "SHELL"
    )
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("GPU DEV STUDIO v2.0") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BackgroundCards,
                    titleContentColor = TextPrimary
                )
            )
        },
        bottomBar = {
            StatusBar()
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            ScrollableTabRow(
                selectedTabIndex = selectedTab,
                containerColor = BackgroundCards,
                contentColor = AccentGLES,
                edgePadding = 8.dp
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title) }
                    )
                }
            }
            
            when (selectedTab) {
                0 -> GLESScreen()
                1 -> EGLScreen()
                2 -> VulkanScreen()
                3 -> OpenCLScreen()
                4 -> TestsScreen()
                5 -> CompilerScreen()
                6 -> LinkerScreen()
                7 -> ProjectsScreen()
                8 -> ANGLESysScreen()
                9 -> APISwitchScreen()
                10 -> BenchmarkScreen()
                11 -> MCBenchScreen()
                12 -> AICopilotScreen()
                13 -> ShellScreen()
            }
        }
    }
}
