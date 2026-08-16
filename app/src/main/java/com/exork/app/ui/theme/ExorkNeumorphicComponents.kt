package com.exork.app.ui.theme

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.exork.app.model.User
import java.io.File
import android.graphics.BitmapFactory
import android.util.Base64

fun parseAvatarToBitmap(data: String?): android.graphics.Bitmap? {
    if (data.isNullOrBlank()) return null
    return try {
        if (data.startsWith("data:")) {
            val base64Data = if (data.contains(",")) data.substringAfter(",") else data
            val decodedBytes = Base64.decode(base64Data, Base64.DEFAULT)
            BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
        } else {
            BitmapFactory.decodeFile(data)
        }
    } catch (e: Exception) {
        android.util.Log.e("AvatarLoader", "Failed to decode bitmap", e)
        null
    }
}

/**
 * 3D Obsidian Neumorphic Card with Leather Texture and Metallic Bezel.
 */
@Composable
fun ExorkNeumorphicCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    cornerRadius: Dp = 20.dp,
    containerColor: Color? = null,
    borderColor: Color? = null,
    elevation: Dp = 10.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = modifier
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
            .shadow(
                elevation = elevation,
                shape = RoundedCornerShape(cornerRadius),
                ambientColor = Color.Black.copy(alpha = 0.8f),
                spotColor = Color.White.copy(alpha = 0.05f)
            ),
        shape = RoundedCornerShape(cornerRadius),
        color = Color.Transparent
    ) {
        Column(
            modifier = Modifier
                .background(
                    if (containerColor != null) Brush.linearGradient(listOf(containerColor, containerColor))
                    else Brush.verticalGradient(listOf(LeatherDark, LeatherDeep))
                )
                .border(
                    BorderStroke(
                        1.5.dp,
                        if (borderColor != null) Brush.linearGradient(listOf(borderColor, borderColor))
                        else Brush.linearGradient(listOf(ChromeSilver, DarkSteel, MutedSlate))
                    ),
                    RoundedCornerShape(cornerRadius)
                )
                .padding(16.dp),
            content = content
        )
    }
}

/**
 * Raised 3D Metallic Action Button (Silver Capsule).
 */
@Composable
fun ExorkChromeButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    height: Dp = 56.dp
) {
    Surface(
        modifier = modifier
            .height(height)
            .shadow(
                elevation = 8.dp,
                shape = CircleShape,
                ambientColor = Color.Black.copy(alpha = 0.6f),
                spotColor = Color.White.copy(alpha = 0.2f)
            )
            .clickable { onClick() },
        shape = CircleShape,
        color = Color.Transparent
    ) {
        Box(
            modifier = Modifier
                .background(
                    Brush.verticalGradient(
                        listOf(SilverLight, SilverDeep)
                    )
                )
                .padding(horizontal = 24.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text.uppercase(),
                style = ExorkTypography.labelLarge.copy(
                    fontWeight = FontWeight.Black,
                    color = ObsidianVoid,
                    letterSpacing = 1.sp
                )
            )
        }
    }
}

/**
 * Chrome-bordered Neumorphic Progress Bar.
 */
@Composable
fun ExorkNeumorphicProgressBar(
    progress: Float,
    label: String? = null,
    subLabel: String? = null,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        if (label != null || subLabel != null) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                if (label != null) Text(label, style = ExorkTypography.labelMedium, color = Color.White)
                if (subLabel != null) Text(subLabel, style = ExorkTypography.labelMedium, color = TitaniumGray)
            }
        }
        
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(12.dp)
                .background(ObsidianVoid, CircleShape)
                .border(1.dp, ChromeSilver.copy(alpha = 0.3f), CircleShape)
                .padding(2.dp)
        ) {
            val animatedProgress by animateFloatAsState(
                targetValue = progress.coerceIn(0f, 1f),
                animationSpec = tween(durationMillis = 250, easing = FastOutSlowInEasing),
                label = "ProgressAnimation"
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth(animatedProgress)
                    .fillMaxHeight()
                    .background(
                        Brush.linearGradient(
                            listOf(ChromeSilver, TitaniumGray)
                        ),
                        CircleShape
                    )
            )
        }
    }
}

/**
 * 3D Metallic Profile Header with Circular Avatar Frame.
 */
@Composable
fun ExorkProfileHeader(
    user: User,
    avatarUri: String?,
    updateKey: Long,
    username: String? = null,
    onAvatarClick: () -> Unit,
    onRankClick: () -> Unit
) {
    val animatedProgress by animateFloatAsState(
        targetValue = user.getProgressPercentage(),
        animationSpec = tween(250, easing = FastOutSlowInEasing),
        label = "LevelProgress"
    )
    
    val animatedXp by animateIntAsState(
        targetValue = user.xp,
        animationSpec = tween(250, easing = FastOutSlowInEasing),
        label = "XpProgress"
    )

    ExorkNeumorphicCard(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            // Metallic 3D Circular Avatar Frame with Camera Badge
            Box(contentAlignment = Alignment.BottomEnd) {
                Box(
                    modifier = Modifier
                        .size(86.dp)
                        .drawBehind {
                            drawCircle(
                                Brush.sweepGradient(
                                    listOf(ChromeSilver, DarkSteel, ChromeSilver)
                                ),
                                radius = size.minDimension / 2
                            )
                        }
                        .padding(4.dp)
                        .clip(CircleShape)
                        .background(ObsidianVoid)
                        .clickable { onAvatarClick() },
                    contentAlignment = Alignment.Center
                ) {
                    val avatarData = user.photoUrl ?: avatarUri
                    val bitmap = remember(avatarData, updateKey) { parseAvatarToBitmap(avatarData) }
                    
                    if (bitmap != null) {
                        Image(
                            bitmap = bitmap.asImageBitmap(),
                            contentDescription = "Avatar",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else if (avatarData != null && avatarData.startsWith("http")) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(avatarData)
                                .crossfade(true)
                                .memoryCacheKey("${avatarData}_$updateKey")
                                .build(),
                            contentDescription = "Avatar",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = ChromeSilver,
                            modifier = Modifier.size(40.dp)
                        )
                    }
                }

                // 3D Neumorphic Camera Icon Badge
                Surface(
                    modifier = Modifier
                        .size(28.dp)
                        .offset(x = (4).dp, y = (4).dp)
                        .shadow(
                            elevation = 4.dp,
                            shape = CircleShape,
                            ambientColor = Color.Black
                        )
                        .clickable { onAvatarClick() },
                    shape = CircleShape,
                    color = Color(0xFF1A1A1E),
                    border = BorderStroke(1.dp, ChromeSilver)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.PhotoCamera,
                            contentDescription = "Change Avatar",
                            tint = ChromeSilver,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.width(24.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                if (username != null) {
                    Text(
                        text = username.uppercase(),
                        style = ExorkTypography.labelLarge.copy(fontWeight = FontWeight.Black),
                        color = ElectricCyan,
                        letterSpacing = 1.sp
                    )
                }
                Text(
                    text = user.rank.uppercase(),
                    style = ExorkTypography.labelMedium,
                    color = TitaniumGray
                )
                Text(
                    text = "HUNTER LEVEL ${user.level}",
                    style = ExorkTypography.headlineMedium.copy(
                        color = Color.White,
                        shadow = Shadow(Color.Black, blurRadius = 4f)
                    )
                )
            }

            // Interactive Rank Entry Button
            Surface(
                modifier = Modifier
                    .wrapContentSize()
                    .clickable { onRankClick() },
                shape = CircleShape,
                color = ObsidianVoid,
                border = BorderStroke(1.dp, ChromeSilver.copy(alpha = 0.3f))
            ) {
                Box(modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.MilitaryTech,
                        contentDescription = null,
                        tint = ChromeSilver,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(20.dp))
        
        ExorkNeumorphicProgressBar(
            progress = animatedProgress,
            label = "$animatedXp XP",
            subLabel = "${user.xpToNextLevel()} XP TO LEVEL UP"
        )
    }
}

@Composable
fun ExorkNeumorphicSectionHeader(title: String) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 8.dp)) {
        Box(
            modifier = Modifier
                .width(4.dp)
                .height(18.dp)
                .background(ChromeSilver, RoundedCornerShape(2.dp))
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = title.uppercase(),
            style = ExorkTypography.labelLarge.copy(
                fontWeight = FontWeight.Bold,
                color = TitaniumGray,
                letterSpacing = 1.5.sp
            )
        )
    }
}

@Composable
fun ExorkNeumorphicHubCard(
    title: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    ExorkNeumorphicCard(
        modifier = modifier,
        onClick = onClick,
        cornerRadius = 24.dp
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = ChromeSilver,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = title.uppercase(),
                style = ExorkTypography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = Color.White,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun ExorkNeumorphicStatCard(
    label: String,
    value: String,
    icon: ImageVector? = null,
    modifier: Modifier = Modifier
) {
    ExorkNeumorphicCard(modifier = modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (icon != null) {
                Icon(icon, null, tint = ChromeSilver, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(12.dp))
            }
            Column {
                Text(label.uppercase(), style = ExorkTypography.labelSmall, color = TitaniumGray)
                Text(value, style = ExorkTypography.headlineSmall, color = Color.White)
            }
        }
    }
}
