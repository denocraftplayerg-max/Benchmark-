package com.gpu.devstudio.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.gpu.devstudio.engine.HardwareQuerier
import com.gpu.devstudio.ui.theme.*

@Composable
fun GLESScreen() {
    var glesData by remember { mutableStateOf("Consultando hardware...") }
    var isLoading by remember { mutableStateOf(true) }

    // Executar a query nativa ao montar a tela
    LaunchedEffect(Unit) {
        try {
            val querier = HardwareQuerier()
            glesData = querier.getGLESInfoNative()
            isLoading = false
        } catch (e: Exception) {
            glesData = "ERRO ao consultar hardware: ${e.message}\n\nNota: Esta aba requer um contexto GL ativo. Em versões futuras, isso será injetado via GLSurfaceView."
            isLoading = false
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
                if (isLoading) {
                    CircularProgressIndicator(color = AccentGLES, modifier = Modifier.align(androidx.compose.ui.Alignment.CenterHorizontally))
                } else {
                    Text("INFORMAÇÕES DO HARDWARE", color = AccentGLES, style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(glesData, color = TextPrimary, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}
