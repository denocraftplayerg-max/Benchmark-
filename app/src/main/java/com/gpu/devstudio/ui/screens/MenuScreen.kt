package com.gpu.devstudio.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gpu.devstudio.ui.theme.*

@Composable
fun MenuScreen(onStartGame: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundPrimary),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            "QUANEGGAES4D",
            color = AccentGLES,
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            "Quantum Next Generation Graphics Engine",
            color = TextSecondary,
            fontSize = 14.sp
        )
        
        Spacer(modifier = Modifier.height(64.dp))
        
        Button(
            onClick = onStartGame,
            modifier = Modifier
                .width(240.dp)
                .height(64.dp)
                .shadow(8.dp, RoundedCornerShape(12.dp)),
            colors = ButtonDefaults.buttonColors(containerColor = AccentVulkan)
        ) {
            Text(
                "▶ INICIAR",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Text(
            "Motor Voxel 3D • ANGLE → Vulkan",
            color = TextSecondary,
            fontSize = 12.sp
        )
    }
}
