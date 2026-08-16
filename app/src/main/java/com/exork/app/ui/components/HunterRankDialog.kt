package com.exork.app.ui.components

import android.content.Intent
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.exork.app.model.User
import com.exork.app.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

val HexagonShape = GenericShape { size, _ ->
    val radius = size.minDimension / 2f
    val centerX = size.width / 2f
    val centerY = size.height / 2f
    for (i in 0 until 6) {
        val angle = Math.toRadians(60.0 * i - 30.0)
        val x = centerX + radius * cos(angle).toFloat()
        val y = centerY + radius * sin(angle).toFloat()
        if (i == 0) moveTo(x, y) else lineTo(x, y)
    }
    close()
}

@Composable
fun HunterRankDialog(
    user: User,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    
    var showExplosion by remember { mutableStateOf(false) }

    val ranks = listOf(
        RankInfo("E", 1, "E-Rank Hunter"),
        RankInfo("D", 20, "D-Rank Hunter"),
        RankInfo("C", 40, "C-Rank Hunter"),
        RankInfo("B", 60, "B-Rank Hunter"),
        RankInfo("A", 80, "A-Rank Hunter"),
        RankInfo("S", 90, "S-Rank Hunter")
    )

    val currentRankInfo = ranks.findLast { user.level >= it.requiredLevel } ?: ranks[0]

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .wrapContentHeight()
                    .shadow(32.dp, RoundedCornerShape(28.dp), ambientColor = Color.Black),
                shape = RoundedCornerShape(28.dp),
                color = Color(0xFB0D0D12),
                border = BorderStroke(
                    1.dp, 
                    Brush.linearGradient(listOf(ChromeSilver.copy(alpha = 0.4f), Color(0xFF1E1E2E)))
                )
            ) {
                Box {
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.align(Alignment.TopEnd).padding(8.dp)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = TitaniumGray)
                    }

                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        SystemGlitchText(
                            text = "PROVE YOUR STRENGTH",
                            style = ExorkTypography.labelLarge.copy(
                                fontWeight = FontWeight.Black,
                                letterSpacing = 3.sp,
                                color = ChromeSilver
                            )
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "SYSTEM ALERT: HUNTER POTENTIAL ASSESSMENT",
                            style = ExorkTypography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.Red.copy(alpha = 0.8f),
                                letterSpacing = 1.sp
                            )
                        )

                        Spacer(modifier = Modifier.height(36.dp))

                        ObsidianCrystalHexagon(currentRankInfo.shortName)

                        Spacer(modifier = Modifier.height(20.dp))

                        Text(
                            text = "LEVEL ${user.level} - ${currentRankInfo.fullName.uppercase()}",
                            style = ExorkTypography.labelLarge.copy(
                                fontWeight = FontWeight.Black,
                                color = Color.White,
                                letterSpacing = 1.sp
                            )
                        )
                        
                        Spacer(modifier = Modifier.height(40.dp))

                        Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            val chunks = ranks.chunked(3)
                            chunks.forEach { rowRanks ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceEvenly
                                ) {
                                    rowRanks.forEach { rank ->
                                        RankMatrixItem(rank, user.level >= rank.requiredLevel)
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(48.dp))

                        Box(contentAlignment = Alignment.Center) {
                            val infiniteTransition = rememberInfiniteTransition(label = "btnGlow")
                            val glowAlpha by infiniteTransition.animateFloat(
                                initialValue = 0.3f,
                                targetValue = 0.7f,
                                animationSpec = infiniteRepeatable(tween(500), RepeatMode.Reverse),
                                label = "glow"
                            )

                            ExorkChromeButton(
                                text = "SHARE RANK",
                                onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    showExplosion = true
                                    val shareText = "I am a ${currentRankInfo.fullName} (Level ${user.level}) in eXork! Join the system."
                                    val intent = Intent(Intent.ACTION_SEND).apply {
                                        type = "text/plain"
                                        putExtra(Intent.EXTRA_TEXT, shareText)
                                    }
                                    context.startActivity(Intent.createChooser(intent, "Share Rank"))
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .drawBehind {
                                        drawRect(
                                            Brush.radialGradient(
                                                listOf(ChromeSilver.copy(alpha = 0.15f * glowAlpha), Color.Transparent)
                                            )
                                        )
                                    }
                            )
                        }
                    }
                }
            }

            if (showExplosion) {
                XPExplosionAnimation(onFinished = { showExplosion = false })
            }
        }
    }
}

@Composable
fun ObsidianCrystalHexagon(rank: String) {
    val infiniteTransition = rememberInfiniteTransition(label = "centerGlow")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(tween(1000, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "pulse"
    )
    val glowColor = Color(0xFF00BFFF)

    Box(contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.size(160.dp)) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(glowColor.copy(alpha = 0.2f), Color.Transparent),
                    radius = size.minDimension / 1.8f * pulseScale
                )
            )
        }

        val rotation by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 360f,
            animationSpec = infiniteRepeatable(tween(5000, easing = LinearEasing)),
            label = "rotation"
        )
        Box(
            modifier = Modifier
                .size(145.dp)
                .rotate(rotation)
                .border(1.dp, glowColor.copy(alpha = 0.3f), HexagonShape)
        )

        Surface(
            modifier = Modifier
                .size(120.dp)
                .shadow(16.dp, HexagonShape, ambientColor = glowColor),
            shape = HexagonShape,
            color = Color(0xFF0D0D12),
            border = BorderStroke(2.dp, Brush.sweepGradient(listOf(ChromeSilver, glowColor, ChromeSilver)))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0xFF1E1E2E), Color(0xFF050508))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = rank,
                    style = TextStyle(
                        fontSize = 64.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        shadow = Shadow(glowColor, offset = Offset(0f, 0f), blurRadius = 15f)
                    )
                )
                
                LightningEffect(modifier = Modifier.fillMaxSize(), color = glowColor.copy(alpha = 0.4f))
            }
        }
    }
}

@Composable
fun RankMatrixItem(rank: RankInfo, isUnlocked: Boolean) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(contentAlignment = Alignment.Center) {
            Surface(
                modifier = Modifier
                    .size(68.dp)
                    .then(if (!isUnlocked) Modifier.alpha(0.6f) else Modifier),
                shape = HexagonShape,
                color = if (isUnlocked) Color(0xFF1E1E2E) else Color(0xFF222226),
                border = BorderStroke(
                    if (isUnlocked) 1.5.dp else 1.dp,
                    if (isUnlocked) 
                        Brush.linearGradient(listOf(ChromeSilver, Color(0xFF00BFFF))) 
                    else 
                        SolidColor(TitaniumGray.copy(alpha = 0.2f))
                )
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    if (isUnlocked) {
                        Text(
                            text = rank.shortName,
                            style = TextStyle(
                                fontSize = 26.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White,
                                shadow = Shadow(Color.Black, blurRadius = 4f)
                            )
                        )
                        LightningEffect(Modifier.fillMaxSize(), count = 1, color = Color(0xFF00BFFF).copy(alpha = 0.2f))
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.radialGradient(
                                        listOf(Color(0xFF333338), Color(0xFF161619))
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "Locked",
                                tint = TitaniumGray.copy(alpha = 0.5f),
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }
            }
            
            if (!isUnlocked) {
                Canvas(modifier = Modifier.size(68.dp)) {
                    val color = Color.Black.copy(alpha = 0.3f)
                    drawLine(color, Offset(0f, 0f), Offset(size.width, size.height), strokeWidth = 2.dp.toPx())
                    drawLine(color, Offset(size.width, 0f), Offset(0f, size.height), strokeWidth = 2.dp.toPx())
                }
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "${rank.shortName} RANK",
            style = ExorkTypography.labelSmall.copy(
                fontWeight = FontWeight.Black,
                color = if (isUnlocked) Color.White else TitaniumGray.copy(alpha = 0.4f),
                letterSpacing = 1.sp
            )
        )
    }
}

@Composable
fun LightningEffect(modifier: Modifier = Modifier, count: Int = 2, color: Color = Color.Cyan) {
    val infiniteTransition = rememberInfiniteTransition(label = "lightning")
    val frame by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(75, easing = LinearEasing)),
        label = "frame"
    )

    Canvas(modifier = modifier) {
        val f = frame
        if (f >= 0f) {
            repeat(count) {
                val startX = Random.nextFloat() * size.width
                val startY = Random.nextFloat() * size.height
                
                var currX = startX
                var varY = startY
                
                val path = Path()
                path.moveTo(currX, varY)
                
                repeat(4) {
                    currX += (Random.nextFloat() - 0.5f) * 40f
                    varY += (Random.nextFloat() - 0.5f) * 40f
                    path.lineTo(currX, varY)
                }
                
                drawPath(path, color, style = Stroke(width = 1.dp.toPx()))
            }
        }
    }
}

@Composable
fun SystemGlitchText(text: String, style: TextStyle) {
    val infiniteTransition = rememberInfiniteTransition(label = "glitch")
    val offset by infiniteTransition.animateValue(
        initialValue = Offset(0f, 0f),
        targetValue = Offset(0f, 0f),
        typeConverter = Offset.VectorConverter,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 1000
                Offset(0f, 0f) at 0
                Offset(1.5f, -0.5f) at 900
                Offset(-1.5f, 0.5f) at 925
                Offset(0f, 0f) at 950
            }
        ),
        label = "offset"
    )

    Box {
        Text(
            text = text,
            style = style.copy(color = Color.Red.copy(alpha = 0.3f)),
            modifier = Modifier.offset(offset.x.dp, offset.y.dp)
        )
        Text(
            text = text,
            style = style.copy(color = Color.Cyan.copy(alpha = 0.3f)),
            modifier = Modifier.offset(-offset.x.dp, -offset.y.dp)
        )
        Text(
            text = text,
            style = style
        )
    }
}

@Composable
fun XPExplosionAnimation(onFinished: () -> Unit) {
    val particles = remember { List(30) { ParticleData() } }
    val animatable = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        animatable.animateTo(1f, tween(600, easing = LinearOutSlowInEasing))
        onFinished()
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        particles.forEach { p ->
            val progress = animatable.value
            val x = center.x + p.vx * progress * 500f
            val y = center.y + p.vy * progress * 500f - (progress * 200f)
            
            drawCircle(
                color = ChromeSilver.copy(alpha = 1f - progress),
                radius = p.radius * (1f - progress),
                center = Offset(x, y)
            )
        }
    }
}

data class ParticleData(
    val vx: Float = (Random.nextFloat() - 0.5f) * 2f,
    val vy: Float = (Random.nextFloat() - 0.5f) * 2f,
    val radius: Float = Random.nextFloat() * 10f + 5f
)

data class RankInfo(
    val shortName: String,
    val requiredLevel: Int,
    val fullName: String
)
