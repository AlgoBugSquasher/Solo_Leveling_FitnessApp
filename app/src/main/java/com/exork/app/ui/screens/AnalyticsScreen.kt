package com.exork.app.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.exork.app.ui.theme.*
import com.exork.app.viewmodel.AnalyticsViewModel
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState

@Composable
fun AnalyticsScreen(
    viewModel: AnalyticsViewModel,
    listState: LazyListState = rememberLazyListState()
) {
    val currentMonth by viewModel.currentMonth.collectAsState()
    val workoutDays by viewModel.workoutDays.collectAsState()
    val distribution by viewModel.todayCategoryDistribution.collectAsState()
    val performance by viewModel.rolling7DayPerformance.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.resetToCurrentMonth()
    }

    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = 20.dp,
            end = 20.dp,
            top = 10.dp, 
            bottom = 120.dp
        ),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        item {
            ExorkNeumorphicSectionHeader(title = "Daily Focus")
            CategoryPieChart(distribution)
        }

        item {
            ExorkNeumorphicSectionHeader(title = "7-Day Performance")
            PerformanceLineGraph(performance)
        }

        item {
            ExorkNeumorphicSectionHeader(title = "Hunter Activity")
            CalendarHeatmap(
                currentMonth = currentMonth,
                workoutDaysSet = workoutDays,
                onPrevious = { viewModel.previousMonth() },
                onNext = { viewModel.nextMonth() }
            )
        }
    }
}

@Composable
fun CategoryPieChart(distribution: Map<String, Float>) {
    val categories = listOf(
        "PUSHUPS" to ElectricCyan,
        "PULLUPS" to DeepManaBlue,
        "PLANK" to RankA,
        "CARDIO" to ManaPurple,
        "OTHER" to TitaniumGray
    )

    val animationProgress by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing),
        label = "PieChartEntry"
    )

    ExorkNeumorphicCard(modifier = Modifier.fillMaxWidth().height(260.dp)) {
        if (distribution.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Canvas(modifier = Modifier.size(140.dp)) {
                        drawCircle(color = DarkSteel, style = Stroke(width = 20.dp.toPx()))
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("NO ACTIVITY TODAY", style = ExorkTypography.labelMedium, color = TitaniumGray)
                }
            }
        } else {
            Row(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(modifier = Modifier.size(160.dp), contentAlignment = Alignment.Center) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        var startAngle = -90f
                        categories.forEach { (name, color) ->
                            val sweep = (distribution[name] ?: 0f) * 360f * animationProgress
                            if (sweep > 0) {
                                drawArc(
                                    color = color,
                                    startAngle = startAngle,
                                    sweepAngle = sweep,
                                    useCenter = false,
                                    style = Stroke(width = 24.dp.toPx())
                                )
                                startAngle += sweep
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.width(24.dp))
                
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    categories.forEach { (name, color) ->
                        if ((distribution[name] ?: 0f) > 0) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(10.dp).background(color, CircleShape))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(name, style = ExorkTypography.labelSmall, color = Color.White)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PerformanceLineGraph(data: List<com.exork.app.data.DayPerformance>) {
    val animationProgress by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing),
        label = "LineGraphEntry"
    )

    ExorkNeumorphicCard(modifier = Modifier.fillMaxWidth().height(220.dp)) {
        if (data.isEmpty()) return@ExorkNeumorphicCard

        val maxXP = (data.maxOfOrNull { it.xp } ?: 1).coerceAtLeast(100)
        
        Box(modifier = Modifier.fillMaxSize().padding(top = 16.dp, start = 8.dp, end = 8.dp, bottom = 8.dp)) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val width = size.width
                val height = size.height
                val stepX = width / (data.size - 1)
                
                val path = Path()
                val fillPath = Path()
                
                data.forEachIndexed { i, day ->
                    val x = i * stepX
                    val y = height - (day.xp.toFloat() / maxXP * height * animationProgress)
                    
                    if (i == 0) {
                        path.moveTo(x, y)
                        fillPath.moveTo(x, height)
                        fillPath.lineTo(x, y)
                    } else {
                        path.lineTo(x, y)
                        fillPath.lineTo(x, y)
                    }
                    
                    if (i == data.size - 1) {
                        fillPath.lineTo(x, height)
                        fillPath.close()
                    }
                    
                    drawCircle(color = ElectricCyan, radius = 4.dp.toPx(), center = Offset(x, y))
                }
                
                drawPath(path, color = ElectricCyan, style = Stroke(width = 2.dp.toPx()))
                drawPath(
                    fillPath,
                    brush = Brush.verticalGradient(listOf(ElectricCyan.copy(alpha = 0.3f), Color.Transparent))
                )
            }
            
            Row(modifier = Modifier.fillMaxWidth().align(Alignment.BottomCenter)) {
                data.forEach { day ->
                    Text(
                        text = day.label,
                        modifier = Modifier.weight(1f),
                        style = ExorkTypography.labelSmall.copy(fontSize = 9.sp),
                        color = TitaniumGray,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
fun CalendarHeatmap(
    currentMonth: String,
    workoutDaysSet: Set<String>,
    onPrevious: () -> Unit,
    onNext: () -> Unit
) {
    val calendar = Calendar.getInstance().apply {
        val sdf = SimpleDateFormat("yyyy-MM", Locale.US)
        time = sdf.parse(currentMonth) ?: Date()
    }
    
    val monthLabel = SimpleDateFormat("MMMM yyyy", Locale.US).format(calendar.time).uppercase()
    
    val todayCalendar = Calendar.getInstance()
    val todayDay = todayCalendar.get(Calendar.DAY_OF_MONTH)
    val todayMonthYear = SimpleDateFormat("yyyy-MM", Locale.US).format(todayCalendar.time)
    val isCurrentMonth = currentMonth == todayMonthYear

    val firstDayOfMonth = (calendar.clone() as Calendar).apply { set(Calendar.DAY_OF_MONTH, 1) }
    val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
    val startOffset = when (firstDayOfMonth.get(Calendar.DAY_OF_WEEK)) {
        Calendar.MONDAY -> 0
        Calendar.TUESDAY -> 1
        Calendar.WEDNESDAY -> 2
        Calendar.THURSDAY -> 3
        Calendar.FRIDAY -> 4
        Calendar.SATURDAY -> 5
        Calendar.SUNDAY -> 6
        else -> 0
    }

    ExorkNeumorphicCard(modifier = Modifier.fillMaxWidth()) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onPrevious) {
                    Icon(Icons.Default.ChevronLeft, null, tint = ChromeSilver)
                }
                Text(monthLabel, style = ExorkTypography.labelLarge.copy(fontWeight = FontWeight.Black), color = Color.White)
                IconButton(
                    onClick = onNext,
                    enabled = !isCurrentMonth
                ) {
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = if (isCurrentMonth) TitaniumGray.copy(alpha = 0.3f) else ChromeSilver
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            val days = listOf("M", "T", "W", "T", "F", "S", "S")
            Row(modifier = Modifier.fillMaxWidth()) {
                days.forEach { d ->
                    Text(d, modifier = Modifier.weight(1f), textAlign = TextAlign.Center, color = TitaniumGray, style = ExorkTypography.labelSmall)
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            val daySdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            
            for (row in 0 until 6) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    for (col in 0 until 7) {
                        val dayNumber = row * 7 + col - startOffset + 1
                        val isMonthDay = dayNumber in 1..daysInMonth
                        
                        Box(
                            modifier = Modifier.weight(1f).aspectRatio(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isMonthDay) {
                                val currentDayCal = (calendar.clone() as Calendar).apply { set(Calendar.DAY_OF_MONTH, dayNumber) }
                                val dateString = daySdf.format(currentDayCal.time)
                                val isWorkoutDone = workoutDaysSet.contains(dateString)
                                val isToday = dayNumber == todayDay && isCurrentMonth
                                
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(
                                            when {
                                                isWorkoutDone -> ElectricCyan.copy(alpha = 0.2f)
                                                isToday -> Color.White.copy(alpha = 0.05f)
                                                else -> Color.Transparent
                                            }
                                        )
                                        .border(
                                            width = if (isWorkoutDone || isToday) 1.5.dp else 0.dp,
                                            color = when {
                                                isWorkoutDone -> ElectricCyan
                                                isToday -> TitaniumGray.copy(alpha = 0.5f)
                                                else -> Color.Transparent
                                            },
                                            shape = RoundedCornerShape(8.dp)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "$dayNumber",
                                        style = ExorkTypography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                        color = if (isWorkoutDone) Color.White else if (isToday) ElectricCyan else TitaniumGray
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
