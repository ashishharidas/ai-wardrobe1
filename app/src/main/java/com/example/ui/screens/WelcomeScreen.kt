package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import androidx.compose.foundation.layout.size
import com.example.ui.theme.SoftCream
import com.example.ui.theme.WarmIvory

@Composable
fun WelcomeScreen(
    onNavigateNext: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                androidx.compose.ui.graphics.Brush.verticalGradient(
                    colors = listOf(WarmIvory, SoftCream)
                )
            )
    ) {
        // Decorative background glow
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 60.dp, end = 20.dp)
                .size(240.dp)
                .background(
                    androidx.compose.ui.graphics.Brush.radialGradient(
                        colors = listOf(com.example.ui.theme.LavenderMist.copy(alpha = 0.4f), androidx.compose.ui.graphics.Color.Transparent)
                    ),
                    shape = CircleShape
                )
        )
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(40.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.weight(1f))
            
            Text(
                text = "Happy Birthday,\nAnavadya ❤️",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Light,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onBackground,
                lineHeight = 40.sp
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Text(
                text = "This isn't simply an application.\nIt's something created especially for you.\n\nA small gift that lets you imagine new styles, celebrate your uniqueness, and have a little fun.\n\nThank you for inspiring this idea.",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
                lineHeight = 28.sp,
                fontWeight = FontWeight.Light
            )
            
            Spacer(modifier = Modifier.weight(1.5f))
            
            Button(
                onClick = onNavigateNext,
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text("Open My Wardrobe", fontSize = 16.sp, fontWeight = FontWeight.Normal, letterSpacing = 1.sp)
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
