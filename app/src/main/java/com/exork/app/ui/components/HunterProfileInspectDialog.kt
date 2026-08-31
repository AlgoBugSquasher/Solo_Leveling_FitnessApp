package com.exork.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.exork.app.model.HunterProfile
import com.exork.app.ui.components.AvatarImage
import com.exork.app.ui.theme.*

@Composable
fun HunterProfileInspectDialog(
    profile: HunterProfile,
    onDismiss: () -> Unit,
    actionButton: @Composable () -> Unit = {}
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.90f)
                .wrapContentHeight()
                .shadow(24.dp, RoundedCornerShape(24.dp)),
            shape = RoundedCornerShape(24.dp),
            color = Color(0xCC0D0D12),
            border = BorderStroke(
                1.dp, 
                Brush.linearGradient(listOf(ChromeSilver.copy(alpha = 0.5f), Color(0xFF2A2A2E)))
            )
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Close, null, tint = ChromeSilver, modifier = Modifier.size(20.dp))
                    }
                    Text(
                        "HUNTER DOSSIER",
                        style = ExorkTypography.labelMedium.copy(fontWeight = FontWeight.Black),
                        color = ChromeSilver,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.width(32.dp))
                }

                // Avatar in Metallic Frame (Reduced to 84dp)
                Box(
                    modifier = Modifier
                        .size(84.dp)
                        .drawBehind {
                            drawCircle(
                                Brush.sweepGradient(listOf(ChromeSilver, DarkSteel, ChromeSilver)),
                                radius = size.minDimension / 2
                            )
                        }
                        .padding(3.dp)
                        .clip(CircleShape)
                        .background(Color.Black),
                    contentAlignment = Alignment.Center
                ) {
                    AvatarImage(
                        avatarData = profile.photoUrl,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                // Identity
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = (profile.username ?: profile.displayName).uppercase(),
                        style = ExorkTypography.titleLarge.copy(fontWeight = FontWeight.Black, fontSize = 18.sp),
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )
                    if (profile.activeTitle != null) {
                        Text(
                            text = profile.activeTitle.uppercase(),
                            style = ExorkTypography.labelSmall,
                            color = ElectricCyan,
                            letterSpacing = 0.5.sp
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .background(ElectricCyan, RoundedCornerShape(4.dp))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(profile.hunterRank, style = ExorkTypography.labelSmall, color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("LVL ${profile.hunterLevel}", style = ExorkTypography.labelLarge, color = ChromeSilver)
                }

                // Combat Vitals
                ExorkNeumorphicCard(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp), 
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        StatItem("STREAK", "${profile.currentStreak}D")
                        StatItem("TOTAL XP", profile.totalXp.toString())
                        StatItem("WORKOUTS", profile.totalWorkouts.toString())
                    }
                }

                // PR Section
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        "PERSONAL RECORDS",
                        style = ExorkTypography.labelSmall.copy(fontWeight = FontWeight.Black, fontSize = 10.sp),
                        color = TitaniumGray
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    ExorkNeumorphicCard(modifier = Modifier.fillMaxWidth()) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            PRRow("PUSHUPS", "${profile.maxPushupsSingleWorkout}")
                            PRRow("PULLUPS", "${profile.maxPullupsSingleWorkout}")
                            PRRow("PLANK", "${profile.maxPlankSingleWorkout}s")
                        }
                    }
                }

                // Action Button (Will only show if not an ally)
                Box(modifier = Modifier.padding(top = 4.dp)) {
                    actionButton()
                }
            }
        }
    }
}

@Composable
private fun StatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, style = ExorkTypography.labelSmall.copy(fontSize = 9.sp), color = TitaniumGray)
        Text(value, style = ExorkTypography.labelLarge.copy(fontWeight = FontWeight.Black), color = Color.White)
    }
}

@Composable
private fun PRRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = ExorkTypography.labelSmall.copy(fontSize = 11.sp), color = TitaniumGray)
        Text(value, style = ExorkTypography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 11.sp), color = ChromeSilver)
    }
}
