package com.exork.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.exork.app.ui.theme.*

@Composable
fun HunterAudioSliderRow(
    label: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label.uppercase(),
                style = ExorkTypography.labelMedium.copy(fontWeight = FontWeight.Black),
                color = Color.White,
                letterSpacing = 1.sp
            )
            Text(
                text = "${(value * 100).toInt()}%",
                style = ExorkTypography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = ChromeSilver
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Stepper Minus
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(Color(0xFF1A1A24), RoundedCornerShape(8.dp))
                    .clickable { onValueChange((value - 0.1f).coerceIn(0f, 1f)) },
                contentAlignment = Alignment.Center
            ) {
                Text("-", color = ChromeSilver, fontWeight = FontWeight.Black, fontSize = 20.sp)
            }

            // Slider
            Slider(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.weight(1f),
                colors = SliderDefaults.colors(
                    thumbColor = ElectricCyan,
                    activeTrackColor = ElectricCyan,
                    inactiveTrackColor = Color(0xFF1C1C26)
                )
            )

            // Stepper Plus
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(Color(0xFF1A1A24), RoundedCornerShape(8.dp))
                    .clickable { onValueChange((value + 0.1f).coerceIn(0f, 1f)) },
                contentAlignment = Alignment.Center
            ) {
                Text("+", color = ChromeSilver, fontWeight = FontWeight.Black, fontSize = 20.sp)
            }
        }
    }
}
