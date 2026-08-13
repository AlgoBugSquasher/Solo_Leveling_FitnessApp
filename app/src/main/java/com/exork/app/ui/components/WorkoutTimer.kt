package com.exork.app.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import com.exork.app.ui.theme.*
import com.exork.app.util.SoundManager
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
    val scrollState = rememberScrollState()
    
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
                .background(ObsidianVoid.copy(alpha = 0.96f))
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
                    .verticalScroll(scrollState)
            ) {
                Text(
                    text = "SET $currentSet / $totalSets",
                    color = TitaniumGray,
                    style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Black, letterSpacing = 4.sp)
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = exerciseName.uppercase(),
                    color = Color.White,
                    style = TextStyle(
                        fontSize = if (isTimerComplete) 24.sp else 32.sp, 
                        fontWeight = FontWeight.Black, 
                        letterSpacing = 2.sp,
                        shadow = Shadow(Color.Black, blurRadius = 15f)
                    ),
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(if (isTimerComplete) 20.dp else 60.dp))
                
                // Large Countdown with Ring
                val ringSize by animateDpAsState(targetValue = if (isTimerComplete) 180.dp else 300.dp, label = "ringSize")
                Box(contentAlignment = Alignment.Center) {
                    val targetProgress = timeLeft.toFloat() / (if (isResting) restDuration else initialSeconds).toFloat()
                    
                    val animatedProgress by animateFloatAsState(
                        targetValue = targetProgress,
                        animationSpec = tween(1000, easing = LinearEasing), label = "progress"
                    )

                    // Outer Recessed Track
                    Box(
                        modifier = Modifier
                            .size(ringSize)
                            .drawBehind {
                                drawCircle(
                                    color = Color.White.copy(alpha = 0.05f),
                                    radius = size.minDimension / 2,
                                    style = Stroke(width = 4.dp.toPx())
                                )
                            }
                    )
                    
                    // 3D Metallic Progress Ring
                    Box(
                        modifier = Modifier
                            .size(ringSize - 20.dp)
                            .drawBehind {
                                drawArc(
                                    brush = Brush.sweepGradient(
                                        listOf(ChromeSilver, DarkSteel, MutedSlate, ChromeSilver)
                                    ),
                                    startAngle = -90f,
                                    sweepAngle = 360f * animatedProgress,
                                    useCenter = false,
                                    style = Stroke(width = 10.dp.toPx(), cap = StrokeCap.Round)
                                )
                            }
                    )
                    
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.scale(if (isTimerComplete) pulseScale * 0.7f else pulseScale)
                    ) {
                        Text(
                            text = timeLeft.toString(),
                            color = if (timeLeft <= 5 && !isResting) Color.Red else Color.White,
                            style = TextStyle(
                                fontSize = if (isTimerComplete) 60.sp else 110.sp, 
                                fontWeight = FontWeight.Black,
                                shadow = Shadow(
                                    color = Color.Black, 
                                    blurRadius = if (timeLeft <= 5 && isRunning) 40f else 25f
                                )
                            )
                        )
                        Text(
                            text = if (isResting) "RESTING" else "SECONDS",
                            color = if (isResting) ChromeSilver else TitaniumGray,
                            style = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(if (isTimerComplete) 20.dp else 40.dp))
                
                Text(
                    text = "STATUS: ${if (isTimerComplete) "COMPLETE" else if (isRunning) "ACTIVE" else "PAUSED"}",
                    color = if (isRunning) ChromeSilver else TitaniumGray.copy(alpha = 0.6f),
                    style = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Black, letterSpacing = 2.sp)
                )

                Spacer(modifier = Modifier.height(if (isTimerComplete) 24.dp else 60.dp))
                
                if (isTimerComplete) {
                    PremiumCompletionPanel(
                        title = if (isResting) "REST OVER" else "TIME COMPLETE",
                        subtitle = if (isResting) "GET READY" else "SET $currentSet COMPLETE",
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
                            modifier = Modifier.weight(1f),
                            onClick = { 
                                soundManager.playClick()
                                isRunning = !isRunning 
                            }
                        )
                        
                        TimerButton(
                            text = "CANCEL",
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
                            Text("SKIP REST", color = ChromeSilver, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
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
    val scrollState = rememberScrollState()

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
                .background(ObsidianVoid.copy(alpha = 0.96f))
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
                    .verticalScroll(scrollState)
            ) {
                Text(
                    text = "DISTANCE SESSION",
                    color = TitaniumGray,
                    style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Black, letterSpacing = 4.sp)
                )
                
                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = exerciseName.uppercase(),
                    color = Color.White,
                    style = TextStyle(
                        fontSize = if (isFinished) 24.sp else 32.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 2.sp,
                        shadow = Shadow(Color.Black, blurRadius = 15f)
                    ),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "TARGET: $targetDistance KM",
                    color = ChromeSilver,
                    style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                )

                Spacer(modifier = Modifier.height(if (isFinished) 20.dp else 60.dp))

                if (isFinished) {
                    val m = secondsElapsed / 60
                    val s = secondsElapsed % 60
                    PremiumCompletionPanel(
                        title = "RUN COMPLETE",
                        subtitle = "DISTANCE: $targetDistance KM\nTIME: ${m}m ${s}s",
                        onAction = {
                            soundManager.playClick()
                            onFinish(secondsElapsed)
                        },
                        actionText = "COLLECT PROGRESS"
                    )
                } else {
                    // Stopwatch Display with Status Ring
                    Box(contentAlignment = Alignment.Center) {
                        val ringSize by animateDpAsState(targetValue = 300.dp, label = "ringSize")
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
                                .size(ringSize)
                                .drawBehind {
                                    drawCircle(
                                        color = Color.White.copy(alpha = 0.05f),
                                        radius = size.minDimension / 2,
                                        style = Stroke(width = 4.dp.toPx())
                                    )
                                }
                        )
                        
                        Box(
                            modifier = Modifier
                                .size(ringSize)
                                .drawBehind {
                                    if (isRunning) {
                                        drawArc(
                                            brush = Brush.sweepGradient(
                                                listOf(ChromeSilver, DarkSteel, MutedSlate, ChromeSilver)
                                            ),
                                            startAngle = animatedRotation,
                                            sweepAngle = 90f,
                                            useCenter = false,
                                            style = Stroke(width = 6.dp.toPx(), cap = StrokeCap.Round)
                                        )
                                    }
                                }
                        )

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = formatTime(secondsElapsed),
                                color = Color.White,
                                style = TextStyle(
                                    fontSize = 72.sp,
                                    fontWeight = FontWeight.Black,
                                    shadow = Shadow(Color.Black, blurRadius = 25f)
                                )
                            )
                            Text(
                                text = "ELAPSED TIME",
                                color = TitaniumGray,
                                style = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(40.dp))
                    
                    Text(
                        text = "STATUS: ${if (isRunning) "RUNNING" else "PAUSED"}",
                        color = if (isRunning) ChromeSilver else TitaniumGray.copy(alpha = 0.4f),
                        style = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Black, letterSpacing = 2.sp)
                    )

                    Spacer(modifier = Modifier.height(60.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        TimerButton(
                            text = if (isRunning) "PAUSE" else if (secondsElapsed == 0) "START" else "RESUME",
                            modifier = Modifier.weight(1f),
                            onClick = {
                                soundManager.playClick()
                                isRunning = !isRunning
                            }
                        )

                        TimerButton(
                            text = "FINISH",
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
                        Text("CANCEL SESSION", color = TitaniumGray, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun TimerButton(
    text: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier
            .height(48.dp)
            .clickable(enabled = enabled) { onClick() },
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
                text = text.uppercase(),
                color = if (enabled) Color.White else Color.White.copy(alpha = 0.3f),
                fontWeight = FontWeight.ExtraBold,
                fontSize = 14.sp,
                letterSpacing = 1.sp
            )
        }
    }
}

@Composable
fun PremiumCompletionPanel(
    title: String,
    subtitle: String,
    onAction: () -> Unit,
    actionText: String
) {
    ExorkNeumorphicCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                tint = ChromeSilver,
                modifier = Modifier.size(60.dp)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = title,
                color = Color.White,
                style = TextStyle(
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.sp,
                    shadow = Shadow(Color.Black, blurRadius = 20f)
                )
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = subtitle,
                color = TitaniumGray,
                textAlign = TextAlign.Center,
                lineHeight = 22.sp,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            ExorkChromeButton(
                text = actionText,
                onClick = onAction,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
