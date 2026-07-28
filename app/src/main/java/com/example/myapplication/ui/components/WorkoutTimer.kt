package com.example.myapplication.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.myapplication.util.SoundManager
import kotlinx.coroutines.delay

@Composable
fun ExerciseTimerDialog(
    exerciseName: String,
    totalSets: Int,
    initialSeconds: Int,
    soundManager: SoundManager,
    onDismiss: () -> Unit,
    onComplete: () -> Unit
) {
    var currentSet by remember { mutableIntStateOf(1) }
    
    var timeLeft by remember { mutableIntStateOf(initialSeconds) }
    var isRunning by remember { mutableStateOf(false) }
    var isTimerComplete by remember { mutableStateOf(false) }
    var isResting by remember { mutableStateOf(false) }
    
    val restDuration = 30
    
    LaunchedEffect(isRunning, timeLeft) {
        if (isRunning && timeLeft > 0) {
            delay(1000)
            timeLeft--
            if (timeLeft == 0) {
                isRunning = false
                soundManager.playQuestComplete()
                isTimerComplete = true
            }
        }
    }

    // Pulse animation for last 5 seconds
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by if (timeLeft <= 5 && isRunning) {
        infiniteTransition.animateFloat(
            initialValue = 1f,
            targetValue = 1.1f,
            animationSpec = infiniteRepeatable(
                animation = tween(500, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ), label = "pulseScale"
        )
    } else {
        remember { mutableStateOf(1f) }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.98f))
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxSize().padding(24.dp)
            ) {
                Text(
                    text = "SET $currentSet / $totalSets",
                    color = Color.White.copy(alpha = 0.6f),
                    style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Black, letterSpacing = 4.sp)
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = exerciseName.uppercase(),
                    color = Color(0xFFBB86FC),
                    style = TextStyle(
                        fontSize = 32.sp, 
                        fontWeight = FontWeight.Black, 
                        letterSpacing = 2.sp,
                        shadow = Shadow(Color(0xFFBB86FC).copy(alpha = 0.5f), blurRadius = 15f)
                    ),
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(60.dp))
                
                // Large Countdown with Ring
                Box(contentAlignment = Alignment.Center) {
                    val themeColor = if (isResting) Color(0xFF03DAC6) else Color(0xFFBB86FC)
                    val targetProgress = timeLeft.toFloat() / (if (isResting) restDuration else initialSeconds).toFloat()
                    
                    val animatedProgress by animateFloatAsState(
                        targetValue = targetProgress,
                        animationSpec = tween(1000, easing = LinearEasing), label = "progress"
                    )

                    // Outer Glow Ring
                    Box(
                        modifier = Modifier
                            .size(300.dp)
                            .drawBehind {
                                drawCircle(
                                    color = themeColor.copy(alpha = if (timeLeft <= 5 && isRunning) 0.15f else 0.05f),
                                    radius = size.minDimension / 2,
                                    style = Stroke(width = 40.dp.toPx())
                                )
                            }
                    )
                    
                    CircularProgressIndicator(
                        progress = { animatedProgress },
                        modifier = Modifier.size(280.dp),
                        color = themeColor,
                        strokeWidth = 8.dp,
                        trackColor = Color.White.copy(alpha = 0.05f),
                        strokeCap = StrokeCap.Round
                    )
                    
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.scale(pulseScale)
                    ) {
                        Text(
                            text = timeLeft.toString(),
                            color = if (timeLeft <= 5 && !isResting) Color.Red else Color.White,
                            style = TextStyle(
                                fontSize = 110.sp, 
                                fontWeight = FontWeight.Black,
                                shadow = Shadow(
                                    color = if (timeLeft <= 5 && !isResting) Color.Red.copy(alpha = 0.6f) else themeColor.copy(alpha = 0.4f), 
                                    blurRadius = if (timeLeft <= 5 && isRunning) 40f else 25f
                                )
                            )
                        )
                        Text(
                            text = if (isResting) "RESTING" else "SECONDS",
                            color = themeColor.copy(alpha = 0.8f),
                            style = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(40.dp))
                
                Text(
                    text = "STATUS: ${if (isTimerComplete) "COMPLETE" else if (isRunning) "ACTIVE" else "PAUSED"}",
                    color = if (isRunning) Color(0xFF03DAC6) else Color.White.copy(alpha = 0.4f),
                    style = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Black, letterSpacing = 2.sp)
                )

                Spacer(modifier = Modifier.height(60.dp))
                
                if (isTimerComplete) {
                    PremiumCompletionPanel(
                        title = if (isResting) "REST OVER" else "TIME COMPLETE",
                        subtitle = if (isResting) "GET READY" else "SET $currentSet COMPLETE",
                        themeColor = Color(0xFF03DAC6),
                        onAction = {
                            soundManager.playClick()
                            if (isResting) {
                                isResting = false
                                isTimerComplete = false
                                timeLeft = initialSeconds
                                isRunning = false
                            } else {
                                if (currentSet < totalSets) {
                                    isResting = true
                                    isTimerComplete = false
                                    timeLeft = restDuration
                                    isRunning = true
                                    currentSet++
                                } else {
                                    onComplete()
                                }
                            }
                        },
                        actionText = if (isResting) "START NEXT SET" else if (currentSet < totalSets) "MARK SET COMPLETE" else "FINISH EXERCISE"
                    )
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        TimerButton(
                            text = if (isRunning) "PAUSE" else "RESUME",
                            color = if (isRunning) Color.Transparent else Color(0xFFBB86FC),
                            borderColor = if (isRunning) Color.Red.copy(alpha = 0.5f) else Color(0xFFBB86FC),
                            textColor = if (isRunning) Color.Red else Color.Black,
                            modifier = Modifier.weight(1f),
                            onClick = { 
                                soundManager.playClick()
                                isRunning = !isRunning 
                            }
                        )
                        
                        TimerButton(
                            text = "CANCEL",
                            color = Color.Transparent,
                            borderColor = Color.White.copy(alpha = 0.2f),
                            textColor = Color.White,
                            modifier = Modifier.weight(1f),
                            onClick = onDismiss
                        )
                    }
                    
                    if (isResting) {
                        Spacer(modifier = Modifier.height(16.dp))
                        TextButton(onClick = {
                            isResting = false
                            timeLeft = initialSeconds
                            isRunning = false
                            isTimerComplete = false
                        }) {
                            Text("SKIP REST", color = Color(0xFF03DAC6), fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ExerciseStopwatchDialog(
    exerciseName: String,
    targetDistance: Double,
    soundManager: SoundManager,
    onDismiss: () -> Unit,
    onFinish: (Int) -> Unit // returns time in seconds
) {
    var secondsElapsed by remember { mutableIntStateOf(0) }
    var isRunning by remember { mutableStateOf(false) }
    var isFinished by remember { mutableStateOf(false) }

    LaunchedEffect(isRunning) {
        while (isRunning) {
            delay(1000)
            secondsElapsed++
        }
    }

    fun formatTime(seconds: Int): String {
        val h = seconds / 3600
        val m = (seconds % 3600) / 60
        val s = seconds % 60
        return "%02d:%02d:%02d".format(h, m, s)
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.98f))
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxSize().padding(24.dp)
            ) {
                Text(
                    text = "DISTANCE SESSION",
                    color = Color.White.copy(alpha = 0.6f),
                    style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Black, letterSpacing = 4.sp)
                )
                
                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = exerciseName.uppercase(),
                    color = Color(0xFFFFD700),
                    style = TextStyle(
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 2.sp,
                        shadow = Shadow(Color(0xFFFFD700).copy(alpha = 0.5f), blurRadius = 15f)
                    ),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "TARGET: $targetDistance KM",
                    color = Color(0xFFFFD700).copy(alpha = 0.7f),
                    style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                )

                Spacer(modifier = Modifier.height(60.dp))

                if (isFinished) {
                    val m = secondsElapsed / 60
                    val s = secondsElapsed % 60
                    PremiumCompletionPanel(
                        title = "RUN COMPLETE",
                        subtitle = "DISTANCE: $targetDistance KM\nTIME: ${m}m ${s}s",
                        themeColor = Color(0xFF03DAC6),
                        onAction = {
                            soundManager.playClick()
                            onFinish(secondsElapsed)
                        },
                        actionText = "COLLECT PROGRESS"
                    )
                } else {
                    // Stopwatch Display with Status Ring
                    Box(contentAlignment = Alignment.Center) {
                        val animatedRotation by rememberInfiniteTransition().animateFloat(
                            initialValue = 0f,
                            targetValue = 360f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(4000, easing = LinearEasing),
                                repeatMode = RepeatMode.Restart
                            ), label = "rotation"
                        )

                        Box(
                            modifier = Modifier
                                .size(300.dp)
                                .drawBehind {
                                    if (isRunning) {
                                        drawArc(
                                            color = Color(0xFFFFD700),
                                            startAngle = animatedRotation,
                                            sweepAngle = 90f,
                                            useCenter = false,
                                            style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
                                        )
                                    }
                                    drawCircle(
                                        color = Color.White.copy(alpha = 0.05f),
                                        radius = size.minDimension / 2,
                                        style = Stroke(width = 2.dp.toPx())
                                    )
                                }
                        )

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = formatTime(secondsElapsed),
                                color = Color.White,
                                style = TextStyle(
                                    fontSize = 72.sp,
                                    fontWeight = FontWeight.Black,
                                    shadow = Shadow(Color(0xFFFFD700).copy(alpha = 0.3f), blurRadius = 25f)
                                )
                            )
                            Text(
                                text = "ELAPSED TIME",
                                color = Color(0xFFFFD700).copy(alpha = 0.8f),
                                style = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(40.dp))
                    
                    Text(
                        text = "STATUS: ${if (isRunning) "RUNNING" else "PAUSED"}",
                        color = if (isRunning) Color(0xFF03DAC6) else Color.White.copy(alpha = 0.4f),
                        style = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Black, letterSpacing = 2.sp)
                    )

                    Spacer(modifier = Modifier.height(60.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        TimerButton(
                            text = if (isRunning) "PAUSE" else if (secondsElapsed == 0) "START" else "RESUME",
                            color = if (isRunning) Color.Transparent else Color(0xFFFFD700),
                            borderColor = if (isRunning) Color.White.copy(alpha = 0.3f) else Color(0xFFFFD700),
                            textColor = if (isRunning) Color.White else Color.Black,
                            modifier = Modifier.weight(1f),
                            onClick = {
                                soundManager.playClick()
                                isRunning = !isRunning
                            }
                        )

                        TimerButton(
                            text = "FINISH",
                            color = Color(0xFF03DAC6),
                            borderColor = Color(0xFF03DAC6),
                            textColor = Color.Black,
                            modifier = Modifier.weight(1f),
                            enabled = secondsElapsed > 0,
                            onClick = {
                                soundManager.playQuestComplete()
                                isRunning = false
                                isFinished = true
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    TextButton(onClick = onDismiss) {
                        Text("CANCEL SESSION", color = Color.Gray, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun TimerButton(
    text: String,
    color: Color,
    borderColor: Color,
    textColor: Color,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(60.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = color,
            disabledContainerColor = color.copy(alpha = 0.3f)
        ),
        border = BorderStroke(1.dp, borderColor),
        shape = RoundedCornerShape(12.dp),
        enabled = enabled
    ) {
        Text(
            text = text,
            color = if (enabled) textColor else textColor.copy(alpha = 0.3f),
            fontWeight = FontWeight.ExtraBold,
            fontSize = 16.sp,
            letterSpacing = 1.sp
        )
    }
}

@Composable
fun PremiumCompletionPanel(
    title: String,
    subtitle: String,
    themeColor: Color,
    onAction: () -> Unit,
    actionText: String
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(),
        color = Color(0xFF1A1A1A),
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(2.dp, themeColor.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier.padding(vertical = 40.dp, horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                tint = themeColor,
                modifier = Modifier.size(72.dp)
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = title,
                color = themeColor,
                style = TextStyle(
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.sp,
                    shadow = Shadow(themeColor.copy(alpha = 0.4f), blurRadius = 20f)
                )
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = subtitle,
                color = Color.White.copy(alpha = 0.7f),
                textAlign = TextAlign.Center,
                lineHeight = 24.sp,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
            
            Spacer(modifier = Modifier.height(48.dp))
            
            // Clear Action Button - Replacing any hidden clickable areas
            Button(
                onClick = onAction,
                colors = ButtonDefaults.buttonColors(
                    containerColor = themeColor,
                    contentColor = Color.Black
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp)
            ) {
                Text(
                    text = actionText.uppercase(),
                    style = TextStyle(
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                )
            }
        }
    }
}
