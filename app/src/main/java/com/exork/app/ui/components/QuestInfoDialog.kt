package com.exork.app.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.exork.app.model.DailyQuest
import com.exork.app.ui.theme.*
import com.exork.app.util.SoundManager

@Composable
fun QuestInfoDialog(
    quests: List<DailyQuest>,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val soundManager = remember { SoundManager.getInstance(context) }
    
    var animationStarted by remember { mutableStateOf(false) }
    
    val scaleY by animateFloatAsState(
        targetValue = if (animationStarted) 1f else 0f,
        animationSpec = tween(durationMillis = 250, easing = FastOutSlowInEasing),
        label = "ScaleY"
    )
    val alpha by animateFloatAsState(
        targetValue = if (animationStarted) 1f else 0f,
        animationSpec = tween(durationMillis = 200),
        label = "Alpha"
    )

    LaunchedEffect(Unit) {
        animationStarted = true
        soundManager.playAlert()
    }

    Dialog(
        onDismissRequest = { /* Force action */ },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .graphicsLayer {
                    this.scaleY = scaleY
                    this.alpha = alpha
                    this.transformOrigin = TransformOrigin.Center
                }
                .shadow(elevation = 24.dp, shape = RoundedCornerShape(16.dp), ambientColor = Color.Black),
            shape = RoundedCornerShape(16.dp),
            color = Color(0xCC0D0D12),
            border = BorderStroke(
                1.5.dp,
                Brush.linearGradient(listOf(ChromeSilver, Color(0xFF3A3A3E)))
            )
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .border(BorderStroke(1.dp, ChromeSilver), RoundedCornerShape(4.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("!", color = ChromeSilver, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        "QUEST INFO",
                        color = Color.White,
                        style = TextStyle(
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 2.sp,
                            shadow = Shadow(Color.Black, blurRadius = 8f)
                        )
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    "Daily Quest: Preparation to Become Strong",
                    color = ChromeSilver,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Exercise List
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    quests.forEach { quest ->
                        QuestDialogItem(quest)
                    }
                }
                
                Spacer(modifier = Modifier.height(32.dp))
                
                // Warning Section
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.Red.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Warning, null, tint = Color.Red, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        "WARNING - Failure to complete the quest within the allotted time will incur an appropriate penalty.",
                        color = Color.Red,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 14.sp
                    )
                }
                
                Spacer(modifier = Modifier.height(32.dp))
                
                // Action Button
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .clickable {
                            soundManager.playSystemAcceptSound()
                            onDismiss()
                        },
                    shape = RoundedCornerShape(24.dp),
                    color = Color.Black.copy(alpha = 0.3f),
                    border = BorderStroke(
                        1.dp,
                        Brush.linearGradient(listOf(ChromeSilver, Color(0xFF4A4A50)))
                    )
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            "ACCEPT / START QUEST",
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun QuestDialogItem(quest: DailyQuest) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            quest.title.uppercase(),
            color = Color.White,
            fontWeight = FontWeight.Black,
            fontSize = 14.sp,
            letterSpacing = 1.sp
        )
        Text(
            "[ ${quest.targetValue} ]",
            color = ChromeSilver,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp
        )
    }
}
