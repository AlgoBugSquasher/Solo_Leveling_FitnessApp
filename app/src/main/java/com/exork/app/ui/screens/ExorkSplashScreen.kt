package com.exork.app.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.*
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.exork.app.R
import com.exork.app.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun ExorkSplashScreen(
    onAnimationComplete: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "splashGlow")
    
    // Scale & Alpha Animations
    val entranceAnimation = remember { Animatable(0.8f) }
    val alphaAnimation = remember { Animatable(0f) }
    
    // Pulsing Glow Animation
    val glowRadius by infiniteTransition.animateFloat(
        initialValue = 100f,
        targetValue = 180f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowRadius"
    )

    // Status Text Alpha Pulse
    val textAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "textAlpha"
    )

    LaunchedEffect(Unit) {
        // Run entrance animations in parallel
        launch {
            entranceAnimation.animateTo(
                targetValue = 1f,
                animationSpec = tween(1500, easing = FastOutSlowInEasing)
            )
        }
        launch {
            alphaAnimation.animateTo(
                targetValue = 1f,
                animationSpec = tween(1000)
            )
        }
        
        delay(2500)
        onAnimationComplete()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ObsidianVoid),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // 3D Metallic Circular Logo Frame
            Box(
                modifier = Modifier
                    .size(160.dp)
                    .scale(entranceAnimation.value)
                    .drawBehind {
                        // Ambient Radial Glow
                        drawCircle(
                            Brush.radialGradient(
                                colors = listOf(ChromeSilver.copy(alpha = 0.15f), Color.Transparent),
                                radius = glowRadius
                            ),
                            radius = glowRadius
                        )
                    }
                    .background(Color.Transparent)
                    .padding(4.dp)
                    .drawBehind {
                        // 3D Chrome Bezel
                        drawCircle(
                            Brush.sweepGradient(
                                listOf(ChromeSilver, DarkSteel, MutedSlate, ChromeSilver)
                            ),
                            radius = size.minDimension / 2
                        )
                    }
                    .padding(4.dp)
                    .clip(CircleShape)
                    .background(ObsidianVoid)
                    .padding(12.dp),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_launcher_monarch),
                    contentDescription = "App Logo",
                    modifier = Modifier
                        .fillMaxSize()
                        .scale(entranceAnimation.value)
                        .graphicsLayer(alpha = alphaAnimation.value)
                )
            }

            Spacer(modifier = Modifier.height(48.dp))

            // Initializing Text
            Text(
                text = "SYSTEM INITIALIZING...",
                color = ChromeSilver,
                fontWeight = FontWeight.Black,
                letterSpacing = 4.sp,
                fontSize = 12.sp,
                style = MaterialTheme.typography.labelLarge.copy(
                    shadow = Shadow(Color.Black, blurRadius = 8f)
                ),
                modifier = Modifier.graphicsLayer(alpha = textAlpha * alphaAnimation.value)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "eXork",
                color = Color.White,
                fontWeight = FontWeight.Black,
                letterSpacing = 8.sp,
                fontSize = 24.sp,
                modifier = Modifier.graphicsLayer(alpha = alphaAnimation.value)
            )
        }
    }
}
