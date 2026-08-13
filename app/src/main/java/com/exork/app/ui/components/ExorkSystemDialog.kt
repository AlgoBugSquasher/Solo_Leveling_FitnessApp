package com.exork.app.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.exork.app.ui.theme.*
import com.exork.app.util.SoundManager

@Composable
fun ExorkSystemDialog(
    title: String = "NOTIFICATION",
    content: String,
    primaryButtonText: String = "ACCEPT",
    secondaryButtonText: String? = "DECLINE",
    iconText: String? = null,
    imageRes: Int? = null,
    isBadgeLayout: Boolean = false,
    onPrimaryClick: () -> Unit,
    onSecondaryClick: (() -> Unit)? = null
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
        onDismissRequest = { /* Controlled by buttons */ },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(if (isBadgeLayout) 0.94f else 0.9f)
                .graphicsLayer {
                    this.scaleY = scaleY
                    this.alpha = alpha
                    this.transformOrigin = TransformOrigin.Center
                }
                .shadow(
                    elevation = 24.dp,
                    shape = RoundedCornerShape(16.dp),
                    ambientColor = Color.Black
                ),
            shape = RoundedCornerShape(16.dp),
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
                    // [ ! ] Silver Icon
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .border(BorderStroke(1.dp, ChromeSilver), RoundedCornerShape(4.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "!",
                            color = ChromeSilver,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }

                    // Optional Achievement Icon (Only for non-badge layout)
                    if (!isBadgeLayout && (iconText != null || imageRes != null)) {
                        Spacer(modifier = Modifier.width(12.dp))
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .border(BorderStroke(1.dp, TitaniumGray), RoundedCornerShape(4.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            if (iconText != null) {
                                Text(text = iconText, fontSize = 18.sp)
                            } else if (imageRes != null) {
                                Image(
                                    painter = painterResource(id = imageRes),
                                    contentDescription = null,
                                    modifier = Modifier.size(24.dp),
                                    contentScale = ContentScale.Fit
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Text(
                        text = title.uppercase(),
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

                if (isBadgeLayout && imageRes != null) {
                    // Special Badge Artwork Frame (MAXIMIZED)
                    Spacer(modifier = Modifier.height(20.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        ExorkNeumorphicCard(
                            modifier = Modifier
                                .aspectRatio(1f)
                                .fillMaxWidth()
                        ) {
                            Image(
                                painter = painterResource(id = imageRes),
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize().padding(16.dp),
                                contentScale = ContentScale.Fit
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                } else {
                    Spacer(modifier = Modifier.height(32.dp))
                }

                // Content
                Text(
                    text = content,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    style = MaterialTheme.typography.bodyLarge.copy(
                        lineHeight = 26.sp,
                        fontWeight = FontWeight.Medium,
                        fontSize = 16.sp
                    )
                )

                Spacer(modifier = Modifier.height(40.dp))

                // Action Buttons
                if (secondaryButtonText != null && onSecondaryClick != null) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp)
                            .padding(bottom = 24.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // DECLINE: Transparent Metallic Outlined
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .clickable { 
                                    soundManager.playSystemDeclineSound()
                                    onSecondaryClick()
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
                                    text = secondaryButtonText.uppercase(),
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp,
                                    fontSize = 14.sp
                                )
                            }
                        }
                        
                        // ACCEPT: Transparent Metallic Outlined
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .clickable { 
                                    soundManager.playSystemAcceptSound()
                                    onPrimaryClick()
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
                                    text = primaryButtonText.uppercase(),
                                    color = Color.White,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 1.sp,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                } else {
                    // Single Button Layout (Centered)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 48.dp)
                            .padding(bottom = 24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .clickable {
                                    soundManager.playSystemAcceptSound()
                                    onPrimaryClick()
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
                                    text = primaryButtonText.uppercase(),
                                    color = Color.White,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 1.sp,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
