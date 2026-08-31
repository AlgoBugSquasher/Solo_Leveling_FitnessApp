package com.exork.app.ui.components

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.graphics.Paint
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.zIndex
import coil.compose.AsyncImage
import com.exork.app.ui.theme.*
import java.io.File
import java.io.FileOutputStream

@Composable
fun NativeAvatarCropper(
    imageUri: Uri,
    onCropSaved: (Uri) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var scale by remember { mutableStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .background(ObsidianVoid),
            contentAlignment = Alignment.Center
        ) {
            val viewportWidth = constraints.maxWidth.toFloat()
            val viewportHeight = constraints.maxHeight.toFloat()

            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Fixed Top Bar with Z-Index and Status Bar Safety
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .zIndex(10f),
                    color = ObsidianVoid
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .statusBarsPadding()
                            .height(64.dp)
                            .padding(horizontal = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier.align(Alignment.CenterStart)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Cancel", tint = ChromeSilver)
                        }
                        
                        Text(
                            "ADJUST AVATAR",
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 2.sp,
                            style = MaterialTheme.typography.labelLarge.copy(
                                shadow = Shadow(Color.Black, blurRadius = 4f)
                            )
                        )

                        // Raised 3D Chrome Save Button
                        Surface(
                            modifier = Modifier
                                .align(Alignment.CenterEnd)
                                .padding(end = 8.dp)
                                .size(44.dp)
                                .shadow(8.dp, CircleShape)
                                .clickable {
                                    try {
                                        val inputStream = context.contentResolver.openInputStream(imageUri)
                                        val originalRaw = BitmapFactory.decodeStream(inputStream)
                                        
                                        // Handle EXIF orientation
                                        val orientation = context.contentResolver.openInputStream(imageUri)?.use { stream ->
                                            android.media.ExifInterface(stream).getAttributeInt(
                                                android.media.ExifInterface.TAG_ORIENTATION,
                                                android.media.ExifInterface.ORIENTATION_UNDEFINED
                                            )
                                        } ?: android.media.ExifInterface.ORIENTATION_UNDEFINED
                                        
                                        val original = com.exork.app.ui.theme.rotateBitmap(originalRaw, orientation)
                                        
                                        val targetDim = 512
                                        val result = Bitmap.createBitmap(targetDim, targetDim, Bitmap.Config.ARGB_8888)
                                        val canvas = android.graphics.Canvas(result)
                                        
                                        val matrix = Matrix()
                                        val viewScaleX = viewportWidth / original.width
                                        val viewScaleY = viewportHeight / original.height
                                        val baseScale = minOf(viewScaleX, viewScaleY)
                                        
                                        val finalScale = scale * baseScale * (targetDim / (minOf(viewportWidth, viewportHeight) * 0.8f))
                                        val centerX = targetDim / 2f
                                        val centerY = targetDim / 2f
                                        
                                        matrix.postTranslate(-original.width / 2f, -original.height / 2f)
                                        matrix.postScale(finalScale, finalScale)
                                        
                                        val offsetFactor = targetDim / (minOf(viewportWidth, viewportHeight) * 0.8f)
                                        matrix.postTranslate(centerX + offset.x * offsetFactor, centerY + offset.y * offsetFactor)
                                        
                                        canvas.drawBitmap(original, matrix, Paint(Paint.ANTI_ALIAS_FLAG).apply { isFilterBitmap = true })
                                        
                                        val file = File(context.filesDir, "custom_avatar.jpg")
                                        if (file.exists()) file.delete()
                                        
                                        FileOutputStream(file).use { out ->
                                            result.compress(Bitmap.CompressFormat.JPEG, 90, out)
                                        }
                                        onCropSaved(Uri.fromFile(file))
                                    } catch (e: Exception) {
                                        android.util.Log.e("Cropper", "Crop failed", e)
                                    }
                                },
                            shape = CircleShape,
                            color = Color.Transparent,
                            border = BorderStroke(1.dp, ChromeSilver)
                        ) {
                            Box(
                                modifier = Modifier.background(
                                    Brush.verticalGradient(listOf(SilverLight, SilverDeep))
                                ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Check, contentDescription = "Save", tint = ObsidianVoid, modifier = Modifier.size(24.dp))
                            }
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .clipToBounds() // Strict clipping fix
                        .pointerInput(Unit) {
                            detectTransformGestures { _, pan, zoom, _ ->
                                scale = (scale * zoom).coerceIn(0.5f, 5f)
                                offset += pan
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    // Visual Preview
                    AsyncImage(
                        model = imageUri,
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer(
                                scaleX = scale,
                                scaleY = scale,
                                translationX = offset.x,
                                translationY = offset.y
                            ),
                        contentScale = ContentScale.Fit
                    )

                    // Circular Mask Overlay
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer {
                                compositingStrategy = CompositingStrategy.Offscreen
                            }
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val radius = size.minDimension * 0.4f
                            
                            drawRect(color = Color.Black.copy(alpha = 0.7f))
                            
                            drawCircle(
                                color = Color.Transparent,
                                radius = radius,
                                center = center,
                                blendMode = BlendMode.Clear
                            )
                            
                            drawCircle(
                                brush = Brush.sweepGradient(
                                    listOf(ChromeSilver, DarkSteel, MutedSlate, ChromeSilver)
                                ),
                                radius = radius,
                                center = center,
                                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.dp.toPx())
                            )
                        }
                    }
                }
                
                // Guidance Bar
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding(),
                    color = ObsidianVoid
                ) {
                    ExorkNeumorphicCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        cornerRadius = 24.dp
                    ) {
                        Text(
                            "Pinch to Zoom • Drag to Pan",
                            color = TitaniumGray,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}
