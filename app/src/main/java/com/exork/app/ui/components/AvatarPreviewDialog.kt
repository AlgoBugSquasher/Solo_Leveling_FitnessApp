package com.exork.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.exork.app.ui.theme.*
import com.exork.app.util.SoundManager
import java.io.File

@Composable
fun AvatarPreviewDialog(
    avatarUri: String?,
    updateKey: Long = 0L,
    onDismiss: () -> Unit,
    onEdit: () -> Unit,
    onRemove: () -> Unit
) {
    val context = LocalContext.current
    val soundManager = remember { SoundManager.getInstance(context) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .fillMaxHeight(0.7f)
                .shadow(elevation = 24.dp, shape = RoundedCornerShape(20.dp), ambientColor = Color.Black),
            shape = RoundedCornerShape(20.dp),
            color = Color(0xCC0D0D12),
            border = BorderStroke(
                1.dp,
                Brush.linearGradient(
                    listOf(ChromeSilver, Color(0xFF3A3A3E))
                )
            )
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxSize()
            ) {
                // Top Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = ChromeSilver)
                    }
                    Text(
                        "PROFILE PREVIEW",
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 2.sp,
                        style = MaterialTheme.typography.titleMedium.copy(
                            shadow = Shadow(Color.Black, blurRadius = 4f)
                        )
                    )
                    // Empty space for balance
                    Spacer(modifier = Modifier.width(48.dp))
                }

                Spacer(modifier = Modifier.weight(1f))

                // Expanded Avatar with 3D Metallic Ring
                Box(
                    modifier = Modifier
                        .size(240.dp)
                        .drawBehind {
                            drawCircle(
                                Brush.sweepGradient(
                                    listOf(ChromeSilver, DarkSteel, MutedSlate, ChromeSilver)
                                ),
                                radius = size.minDimension / 2
                            )
                        }
                        .padding(4.dp)
                        .clip(CircleShape)
                        .background(ObsidianVoid),
                    contentAlignment = Alignment.Center
                ) {
                    val bitmap = remember(avatarUri, updateKey) { parseAvatarToBitmap(avatarUri) }
                    
                    if (bitmap != null) {
                        Image(
                            bitmap = bitmap.asImageBitmap(),
                            contentDescription = "Full Preview",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else if (avatarUri != null && avatarUri.startsWith("http")) {
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(avatarUri)
                                .crossfade(true)
                                .memoryCacheKey("${avatarUri}_$updateKey")
                                .build(),
                            contentDescription = "Full Preview",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.fillMaxSize(0.5f)
                        )
                    }
                }

                Spacer(modifier = Modifier.weight(1.2f))

                // Bottom Actions
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .padding(bottom = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // EDIT BUTTON: Transparent Metallic Outlined
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .clickable {
                                soundManager.playClick()
                                onEdit()
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
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Edit, null, tint = Color.White, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "EDIT",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                        }
                    }

                    if (avatarUri != null) {
                        // REMOVE BUTTON: Subtle Red Tint
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .clickable {
                                    soundManager.playClick()
                                    onRemove()
                                },
                            shape = RoundedCornerShape(24.dp),
                            color = Color.Red.copy(alpha = 0.1f),
                            border = BorderStroke(1.dp, Color.Red.copy(alpha = 0.3f))
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = "REMOVE",
                                    color = Color.Red.copy(alpha = 0.8f),
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

