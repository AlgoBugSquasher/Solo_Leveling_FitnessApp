package com.exork.app.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.exork.app.ui.theme.ChromeSilver
import com.exork.app.ui.theme.ObsidianVoid
import com.exork.app.ui.theme.parseAvatarToBitmap

@Composable
fun AvatarImage(
    avatarData: String?,
    modifier: Modifier = Modifier,
    updateKey: Long = 0L,
    placeholderSize: Dp = 40.dp,
    contentDescription: String? = "Avatar"
) {
    val context = LocalContext.current

    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(ObsidianVoid),
        contentAlignment = Alignment.Center
    ) {
        val bitmap = remember(avatarData, updateKey) { parseAvatarToBitmap(avatarData) }

        if (bitmap != null) {
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = contentDescription,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else if (!avatarData.isNullOrBlank() && avatarData.startsWith("http")) {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(avatarData)
                    .crossfade(true)
                    .memoryCacheKey("${avatarData}_$updateKey")
                    .build(),
                contentDescription = contentDescription,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                tint = ChromeSilver,
                modifier = Modifier.size(placeholderSize)
            )
        }
    }
}
