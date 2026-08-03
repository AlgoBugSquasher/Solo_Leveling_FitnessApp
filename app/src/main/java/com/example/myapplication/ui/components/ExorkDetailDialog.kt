package com.example.myapplication.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.myapplication.ui.theme.*
import com.example.myapplication.util.SoundManager

@Composable
fun ExorkDetailDialog(
    title: String = "ARCHIVE DETAILS",
    badgeName: String,
    rarity: String,
    rarityColor: Color,
    description: String,
    imageRes: Int,
    requiredLevel: Int,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val soundManager = remember { SoundManager.getInstance(context) }
    
    var animationStarted by remember { mutableStateOf(false) }
    
    // Hologram Expansion Animation
    val scaleY by animateFloatAsState(
        targetValue = if (animationStarted) 1f else 0f,
        animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing),
        label = "ScaleY"
    )
    val alpha by animateFloatAsState(
        targetValue = if (animationStarted) 1f else 0f,
        animationSpec = tween(durationMillis = 400),
        label = "Alpha"
    )

    LaunchedEffect(Unit) {
        animationStarted = true
        soundManager.playSystemPopupSound()
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .graphicsLayer {
                    this.scaleY = scaleY
                    this.alpha = alpha
                    this.transformOrigin = TransformOrigin.Center
                }
                .shadow(
                    elevation = 24.dp,
                    shape = RoundedCornerShape(20.dp),
                    ambientColor = Color.Black
                ),
            shape = RoundedCornerShape(20.dp),
            color = Color(0xCC0D0D12), // Translucent Obsidian Glass
            border = BorderStroke(
                1.dp,
                Brush.linearGradient(
                    listOf(ChromeSilver, Color(0xFF3A3A3E))
                )
            )
        ) {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                // Header Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Archive Info Icon
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .border(BorderStroke(1.dp, ChromeSilver), RoundedCornerShape(4.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = ChromeSilver,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Text(
                        text = title,
                        color = Color.White,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Black,
                            letterSpacing = 2.sp,
                            shadow = Shadow(Color.Black, blurRadius = 4f)
                        )
                    )
                }

                // Divider
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(ChromeSilver.copy(alpha = 0.2f))
                )

                // Vertical Card Artwork Frame
                Spacer(modifier = Modifier.height(24.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    ExorkNeumorphicCard(
                        modifier = Modifier
                            .aspectRatio(2f / 3f)
                            .height(320.dp)
                    ) {
                        Image(
                            painter = painterResource(id = imageRes),
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize().padding(8.dp),
                            contentScale = ContentScale.Fit
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))

                // Metadata Section
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = badgeName.uppercase(),
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp,
                            shadow = Shadow(Color.Black, blurRadius = 8f)
                        )
                    )
                    
                    Text(
                        text = rarity.uppercase(),
                        color = rarityColor,
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 2.sp
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = description,
                        color = TitaniumGray,
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            lineHeight = 24.sp
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "REQUIRED LEVEL: $requiredLevel",
                        color = ChromeSilver.copy(alpha = 0.7f),
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.5.sp
                        )
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Action Button
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .padding(bottom = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .clickable {
                                soundManager.playClick()
                                onDismiss()
                            },
                        shape = RoundedCornerShape(24.dp),
                        color = Color.Black.copy(alpha = 0.3f),
                        border = BorderStroke(
                            1.dp,
                            Brush.linearGradient(
                                listOf(ChromeSilver, Color(0xFF4A4A50))
                            )
                        )
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "CLOSE",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 2.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
