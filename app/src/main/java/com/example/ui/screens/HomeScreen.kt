package com.example.ui.screens

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.data.AppDatabase
import com.example.data.GeneratedLook
import com.example.ui.theme.DustyRose
import com.example.ui.theme.LavenderMist
import com.example.ui.theme.SoftCream
import com.example.ui.theme.SoftCreamDark
import com.example.ui.theme.FrostedGlassWhite

@Composable
fun HomeScreen(
    bodyPhotoUri: String?,
    onNavigateToTryOnSetup: () -> Unit,
    onNavigateToWardrobe: () -> Unit,
    onNavigateToFavorites: () -> Unit,
    onNavigateToLetter: () -> Unit
) {
    val context = LocalContext.current
    val database = remember { AppDatabase.getDatabase(context) }
    val dao = remember { database.wardrobeDao() }
    val recentLooks by dao.getHistory().collectAsState(initial = emptyList())

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.statusBars),
            contentPadding = PaddingValues(24.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "GOOD MORNING",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                        )
                        Text(
                            text = "Anavadya 🌸",
                            style = MaterialTheme.typography.headlineLarge,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                    
                    if (bodyPhotoUri != null) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .border(1.dp, DustyRose.copy(alpha = 0.5f), CircleShape)
                                .padding(4.dp)
                        ) {
                            Image(
                                painter = rememberAsyncImagePainter(Uri.parse(bodyPhotoUri)),
                                contentDescription = "Profile Photo",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape)
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(32.dp))
            }
            
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                        .shadow(
                            elevation = 20.dp, 
                            shape = RoundedCornerShape(40.dp), 
                            ambientColor = DustyRose.copy(alpha = 0.3f), 
                            spotColor = DustyRose.copy(alpha = 0.3f)
                        )
                        .clip(RoundedCornerShape(40.dp))
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(SoftCream, SoftCreamDark)
                            )
                        )
                        .clickable(onClick = onNavigateToTryOnSetup)
                ) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .fillMaxWidth()
                            .padding(24.dp)
                            .clip(RoundedCornerShape(24.dp))
                            .background(FrostedGlassWhite)
                            .border(1.dp, androidx.compose.ui.graphics.Color.White.copy(alpha = 0.5f), RoundedCornerShape(24.dp))
                            .padding(24.dp)
                    ) {
                        Column {
                            Text(
                                text = "THE SIGNATURE EXPERIENCE",
                                style = MaterialTheme.typography.labelSmall,
                                color = DustyRose
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Try New Outfit",
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Let AI weave your stored silhouette with your personal wardrobe.",
                                style = MaterialTheme.typography.bodyLarge.copy(fontSize = 12.sp),
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.weight(1f).height(1.dp).background(DustyRose.copy(alpha = 0.3f)))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "BEGIN MAGIC",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            item {
                Row(modifier = Modifier.fillMaxWidth()) {
                    SecondaryActionCard(
                        emoji = "👗",
                        title = "WARDROBE",
                        subtitle = "My pieces",
                        onClick = onNavigateToWardrobe,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    SecondaryActionCard(
                        emoji = "❤️",
                        title = "FAVORITES",
                        subtitle = "Saved looks",
                        onClick = onNavigateToFavorites,
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                
                Card(
                    shape = RoundedCornerShape(32.dp),
                    colors = CardDefaults.cardColors(containerColor = androidx.compose.ui.graphics.Color.Transparent),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = onNavigateToLetter)
                ) {
                    Box(
                        modifier = Modifier
                            .background(Brush.horizontalGradient(
                                colors = listOf(DustyRose.copy(alpha = 0.1f), LavenderMist.copy(alpha = 0.1f))
                            ))
                            .border(1.dp, DustyRose.copy(alpha = 0.2f), RoundedCornerShape(32.dp))
                            .padding(20.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(androidx.compose.ui.graphics.Color.White.copy(alpha = 0.6f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("🎁", fontSize = 20.sp)
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Personal Letter",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontFamily = androidx.compose.ui.text.font.FontFamily.Serif,
                                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                                Text(
                                    text = "CREATED FOR ANAVADYA",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .border(1.dp, DustyRose.copy(alpha = 0.3f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("→", color = DustyRose)
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(48.dp))
            }
            
            if (recentLooks.isNotEmpty()) {
                item {
                    Text(
                        text = "Recent Outfits",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Light
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(recentLooks) { look ->
                            Card(
                                shape = RoundedCornerShape(24.dp),
                                modifier = Modifier
                                    .size(140.dp, 200.dp)
                            ) {
                                Image(
                                    painter = rememberAsyncImagePainter(Uri.parse(look.resultImageUri)),
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}

@Composable
fun SecondaryActionCard(
    emoji: String,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = SoftCream),
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier
                .border(1.dp, androidx.compose.ui.graphics.Color.White, RoundedCornerShape(32.dp))
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(androidx.compose.ui.graphics.Color.White)
                    .shadow(1.dp, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(text = emoji, fontSize = 18.sp)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontSize = 9.sp, 
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                ),
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f)
            )
        }
    }
}
