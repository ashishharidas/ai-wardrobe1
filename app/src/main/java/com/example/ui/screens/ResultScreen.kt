package com.example.ui.screens

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.example.data.GeneratedLook
import com.example.ui.theme.DustyRose
import com.example.ui.theme.FrostedGlassWhite
import com.example.ui.theme.WarmIvory

import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.ui.draw.rotate

@Composable
fun ResultScreen(
    bodyPhotoUri: String?,
    look: GeneratedLook?,
    onBack: () -> Unit,
    onSaveFavorite: (Boolean) -> Unit,
    onGenerateAgain: () -> Unit
) {
    if (look == null) return
    var isFavorite by remember { mutableStateOf(look.isFavorite) }
    var sliderPosition by remember { mutableStateOf(0.5f) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(WarmIvory)
    ) {
        // Base image (After)
        Image(
            painter = rememberAsyncImagePainter(Uri.parse(look.resultImageUri)),
            contentDescription = "Generated Look",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        
        // Overlay image (Before)
        if (bodyPhotoUri != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(androidx.compose.foundation.shape.GenericShape { size, _ ->
                        moveTo(0f, 0f)
                        lineTo(size.width * sliderPosition, 0f)
                        lineTo(size.width * sliderPosition, size.height)
                        lineTo(0f, size.height)
                        close()
                    })
            ) {
                Image(
                    painter = rememberAsyncImagePainter(Uri.parse(bodyPhotoUri)),
                    contentDescription = "Original Photo",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
            
            // Slider divider
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectHorizontalDragGestures { change, dragAmount ->
                            change.consume()
                            val fractionDelta = dragAmount / size.width
                            sliderPosition = (sliderPosition + fractionDelta).coerceIn(0f, 1f)
                        }
                    }
            ) {
                val boxWidth = maxWidth
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(4.dp)
                        .align(Alignment.CenterStart)
                        .offset(x = (boxWidth * sliderPosition) - 2.dp)
                        .background(androidx.compose.ui.graphics.Color.White)
                        .shadow(4.dp)
                ) {
                    // Custom handle thumb
                    Box(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .size(36.dp)
                            .offset(x = (-16).dp)
                            .clip(CircleShape)
                            .background(androidx.compose.ui.graphics.Color.White)
                            .border(1.dp, DustyRose, CircleShape)
                            .shadow(2.dp, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(horizontalArrangement = Arrangement.Center) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = DustyRose, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(2.dp))
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = DustyRose, modifier = Modifier.size(12.dp).rotate(180f))
                        }
                    }
                }
            }
        }
        
        // Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.statusBars)
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .background(FrostedGlassWhite, CircleShape)
                    .border(1.dp, androidx.compose.ui.graphics.Color.White.copy(alpha = 0.5f), CircleShape)
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onBackground)
            }
            
            IconButton(
                onClick = { 
                    isFavorite = !isFavorite
                    onSaveFavorite(isFavorite) 
                },
                modifier = Modifier
                    .background(FrostedGlassWhite, CircleShape)
                    .border(1.dp, androidx.compose.ui.graphics.Color.White.copy(alpha = 0.5f), CircleShape)
            ) {
                Icon(
                    if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder, 
                    contentDescription = "Favorite", 
                    tint = if (isFavorite) DustyRose else MaterialTheme.colorScheme.onBackground
                )
            }
        }
        
        // Bottom Controls
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.navigationBars)
                .padding(24.dp)
                .clip(RoundedCornerShape(32.dp))
                .background(FrostedGlassWhite)
                .border(1.dp, androidx.compose.ui.graphics.Color.White.copy(alpha = 0.5f), RoundedCornerShape(32.dp))
                .padding(24.dp)
        ) {
            Column {
                Text(
                    text = "A New Silhouette",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(24.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Button(
                        onClick = onGenerateAgain,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        shape = CircleShape,
                        modifier = Modifier.weight(1f).height(56.dp)
                    ) {
                        Text("Generate Again")
                    }
                    
                    Spacer(modifier = Modifier.width(16.dp))
                    
                    IconButton(
                        onClick = { /* Save to gallery */ },
                        modifier = Modifier
                            .size(56.dp)
                            .background(androidx.compose.ui.graphics.Color.White, CircleShape)
                            .shadow(2.dp, CircleShape)
                    ) {
                        Icon(Icons.Default.Download, contentDescription = "Save", tint = MaterialTheme.colorScheme.onBackground)
                    }
                    
                    Spacer(modifier = Modifier.width(16.dp))
                    
                    IconButton(
                        onClick = { /* Share */ },
                        modifier = Modifier
                            .size(56.dp)
                            .background(androidx.compose.ui.graphics.Color.White, CircleShape)
                            .shadow(2.dp, CircleShape)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = "Share", tint = MaterialTheme.colorScheme.onBackground)
                    }
                }
            }
        }
    }
}
