package com.exork.app.ui.theme

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Optimized Glass Card for RPG UI.
 * Uses a static tint with subtle border to maintain performance.
 */
@Composable
fun ExorkGlassCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    border: BorderStroke? = BorderStroke(1.dp, ElectricCyan.copy(alpha = 0.2f)),
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = modifier
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier),
        color = TranslucentSlate.copy(alpha = 0.8f),
        shape = RoundedCornerShape(24.dp),
        border = border,
        tonalElevation = 8.dp
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            content = content
        )
    }
}

/**
 * The Primary Action Button for eXork.
 * Features a Monarch Gold radial gradient and neon pulse animation.
 */
@Composable
fun ExorkPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val infiniteTransition = rememberInfiniteTransition(label = "neonPulse")
    
    // High-intensity powered-up border pulse (Energetic Speed)
    val borderPulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "borderPulse"
    )

    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .height(64.dp)
            .border(1.5.dp, ElectricCyan.copy(alpha = borderPulseAlpha), RoundedCornerShape(16.dp))
            .shadow(
                elevation = 24.dp,
                shape = RoundedCornerShape(16.dp),
                spotColor = ElectricCyan.copy(alpha = borderPulseAlpha),
                ambientColor = ElectricCyan.copy(alpha = borderPulseAlpha * 0.5f)
            ),
  colors = ButtonDefaults.buttonColors(
            containerColor = TranslucentSlate,
            contentColor = ElectricCyan,
            disabledContainerColor = TranslucentSlate.copy(alpha = 0.3f),
            disabledContentColor = ElectricCyan.copy(alpha = 0.3f)
        ),
        shape = RoundedCornerShape(16.dp),
        contentPadding = PaddingValues(horizontal = 32.dp)
    ) {
        Text(
            text = text.uppercase(),
            style = ExorkTypography.labelLarge.copy(fontSize = 18.sp, letterSpacing = 2.sp),
            fontWeight = FontWeight.Black
        )
    }
}

/**
 * Segmented RPG Progress Bar.
 */
@Composable
fun ExorkProgressBar(
    progress: Float,
    modifier: Modifier = Modifier,
    label: String? = null,
    subLabel: String? = null,
    showSweep: Boolean = true,
    primaryColor: Color = ElectricCyan
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 250, easing = FastOutSlowInEasing),
        label = "progressAnimation"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "sweep")
    val sweepOffset by infiniteTransition.animateFloat(
        initialValue = -1f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "sweepOffset"
    )

    Column(modifier = modifier) {
        if (label != null || subLabel != null) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                if (label != null) Text(label, style = ExorkTypography.labelMedium.copy(fontSize = 11.sp), color = Color.White)
                if (subLabel != null) Text(subLabel, style = ExorkTypography.labelMedium.copy(fontSize = 11.sp), color = Color.Gray)
            }
        }
        
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp)
                .background(MonarchSlate, RoundedCornerShape(5.dp))
                .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(5.dp))
                .clip(RoundedCornerShape(5.dp))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(animatedProgress)
                    .fillMaxHeight()
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(ManaPurple, primaryColor)
                        )
                    )
                    .drawBehind {
                        // Gloss effect
                        drawLine(
                            color = Color.White.copy(alpha = 0.2f),
                            start = Offset(0f, 2f),
                            end = Offset(size.width, 2f),
                            strokeWidth = 2f
                        )

                        if (showSweep) {
                            val sweepWidth = size.width * 0.5f
                            val startX = size.width * sweepOffset
                            drawRect(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        primaryColor.copy(alpha = 0.3f),
                                        Color.Transparent
                                    ),
                                    startX = startX,
                                    endX = startX + sweepWidth
                                ),
                                size = size
                            )
                        }
                    }
            )
        }
    }
}

/**
 * Floating Glass Navigation Bar.
 */
@Composable
fun ExorkNavigationBar(
    modifier: Modifier = Modifier,
    containerColor: Color = ObsidianVoid.copy(alpha = 0.9f),
    borderColor: Color = ChromeSilver.copy(alpha = 0.4f),
    content: @Composable RowScope.() -> Unit
) {
    Surface(
        modifier = modifier
            .padding(horizontal = 32.dp, vertical = 12.dp)
            .fillMaxWidth()
            .height(64.dp),
        color = containerColor,
        shape = RoundedCornerShape(32.dp),
        border = BorderStroke(1.dp, borderColor),
        tonalElevation = 12.dp
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically,
            content = content
        )
    }
}

/**
 * specialized RPG Stat Card.
 */
@Composable
fun ExorkStatCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    icon: (@Composable () -> Unit)? = null,
    isPremium: Boolean = false
) {
    ExorkGlassCard(
        modifier = modifier.padding(vertical = 2.dp),
        border = if (isPremium) BorderStroke(1.dp, ElectricCyan.copy(alpha = 0.4f)) else BorderStroke(1.dp, ElectricCyan.copy(alpha = 0.1f))
    ) {
        Row(
            modifier = Modifier.padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (icon != null) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(if (isPremium) ElectricCyan.copy(alpha = 0.1f) else ManaPurple.copy(alpha = 0.1f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Box(modifier = Modifier.size(20.dp), contentAlignment = Alignment.Center) {
                        icon()
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
            }
            Column {
                Text(label.uppercase(), style = ExorkTypography.labelMedium.copy(fontSize = 10.sp), color = Color.Gray)
                Text(
                    value,
                    style = ExorkTypography.headlineMedium.copy(fontSize = 18.sp),
                    color = if (isPremium) ElectricCyan else Color.White
                )
            }
        }
    }
}

@Composable
fun ExorkSectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    action: (@Composable () -> Unit)? = null
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(width = 4.dp, height = 20.dp)
                    .background(ElectricCyan, RoundedCornerShape(2.dp))
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                title.uppercase(),
                style = ExorkTypography.labelLarge.copy(fontSize = 12.sp),
                color = Color.White
            )
        }
        if (action != null) action()
    }
}
