package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.example.api.VtonApiService
import com.example.ui.theme.DustyRose
import com.example.ui.theme.WarmIvory

@Composable
fun TryOnScreen(
    bodyPhotoUri: String?,
    topImageUrl: String?,
    bottomImageUrl: String?,
    apiService: VtonApiService,
    onResultReady: (String) -> Unit,
    onError: () -> Unit
) {
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        if (bodyPhotoUri != null) {
            try {
                val resultUrl = apiService.generateTryOn(context, bodyPhotoUri, topImageUrl, bottomImageUrl)
                if (resultUrl != null) {
                    onResultReady(resultUrl)
                } else {
                    android.widget.Toast.makeText(context, "Failed to generate image.", android.widget.Toast.LENGTH_LONG).show()
                    onError()
                }
            } catch (e: Exception) {
                val msg = e.message ?: "An error occurred during generation."
                android.widget.Toast.makeText(context, msg, android.widget.Toast.LENGTH_LONG).show()
                onError()
            }
        } else {
            onError()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(WarmIvory),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(32.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                topImageUrl?.let {
                    Box(
                        modifier = Modifier
                            .size(90.dp, 120.dp)
                            .clip(RoundedCornerShape(16.dp))
                    ) {
                        Image(
                            painter = rememberAsyncImagePainter(it),
                            contentDescription = "Top",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
                if (topImageUrl != null && bottomImageUrl != null) {
                    Spacer(modifier = Modifier.width(16.dp))
                }
                bottomImageUrl?.let {
                    Box(
                        modifier = Modifier
                            .size(90.dp, 120.dp)
                            .clip(RoundedCornerShape(16.dp))
                    ) {
                        Image(
                            painter = rememberAsyncImagePainter(it),
                            contentDescription = "Bottom",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(48.dp))
            
            CircularProgressIndicator(
                color = DustyRose,
                modifier = Modifier.size(32.dp),
                strokeWidth = 2.dp
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = "Creating your new look...",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Light,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
    }
}
