package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.SittingSession
import com.example.ui.SittingViewModel
import com.example.ui.theme.CoralWarning
import com.example.ui.theme.TealPrimary
import com.example.ui.theme.TealSecondary
import com.example.ui.theme.TextGray
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextWhite
import com.example.util.Localization
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ReportsScreen(
    viewModel: SittingViewModel,
    modifier: Modifier = Modifier
) {
    val sessions by viewModel.sessionsList.collectAsState()
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    // Aggregate statistics
    val totalSessions = sessions.size
    val totalSittingSeconds = sessions.filter { it.isSitting }.sumOf { it.durationSeconds }
    val totalStandingSeconds = sessions.filter { !it.isSitting }.sumOf { it.durationSeconds }
    val totalSittingMinutes = totalSittingSeconds / 60
    val totalStandingMinutes = totalStandingSeconds / 60

    val sittingRatio = if (totalSittingSeconds + totalStandingSeconds > 0) {
        (totalSittingSeconds.toFloat() / (totalSittingSeconds + totalStandingSeconds) * 100).toInt()
    } else {
        0
    }

    val standingRatio = if (totalSittingSeconds + totalStandingSeconds > 0) {
        (totalStandingSeconds.toFloat() / (totalSittingSeconds + totalStandingSeconds) * 100).toInt()
    } else {
        0
    }

    // Prepare weekly trend data: sum sitting minutes per date for last 5 unique days
    val dailySums = remember(sessions) {
        sessions
            .filter { it.isSitting }
            .groupBy { it.dateString }
            .mapValues { entry -> entry.value.sumOf { it.durationSeconds } / 60 }
            .toList()
            .sortedBy { it.first }
            .takeLast(5)
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // App Stats summary cards
        item {
            Text(
                text = Localization.t("کارنامه ارگونومیک شما", "Your Ergonomic Summary"),
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 6.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = Localization.t("کل زمان نشستن", "Total Sitting"),
                            style = MaterialTheme.typography.bodySmall,
                            color = TextGray
                        )
                        Text(
                            text = "$totalSittingMinutes ${Localization.t("دقیقه", "mins")}",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = CoralWarning
                            )
                        )
                    }
                }

                Card(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 6.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = Localization.t("کل زمان ایستادن", "Total Standing"),
                            style = MaterialTheme.typography.bodySmall,
                            color = TextGray
                        )
                        Text(
                            text = "$totalStandingMinutes ${Localization.t("دقیقه", "mins")}",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = TealPrimary
                            )
                        )
                    }
                }
            }
        }

        // Sitting vs Standing Ratio progress bar card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = Localization.t("تناسب نشستن به ایستادن", "Sitting vs Standing Ratio"),
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    if (totalSittingSeconds + totalStandingSeconds > 0) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "${Localization.t("نشستن:", "Sitting:")} $sittingRatio%",
                                style = MaterialTheme.typography.bodySmall,
                                color = CoralWarning
                            )
                            Text(
                                text = "${Localization.t("ایستادن:", "Standing:")} $standingRatio%",
                                style = MaterialTheme.typography.bodySmall,
                                color = TealPrimary
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))

                        // Custom percentage bar
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(16.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Row(modifier = Modifier.fillMaxSize()) {
                                if (sittingRatio > 0) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxHeight()
                                            .weight(sittingRatio.toFloat())
                                            .background(CoralWarning)
                                    )
                                }
                                if (standingRatio > 0) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxHeight()
                                            .weight(standingRatio.toFloat())
                                            .background(TealPrimary)
                                    )
                                }
                            }
                        }
                    } else {
                        Text(
                            text = Localization.t(
                                "هنوز داده‌ای پایش نشده است. نمودار پس از ثبت اولین جلسه رسم می‌شود.",
                                "No tracking records found yet. Active charts will populate as soon as you record your first session."
                            ),
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextGray
                        )
                    }
                }
            }
        }

        // Custom Canvas drawing for the bar chart
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Start
                    ) {
                        Icon(
                            imageVector = Icons.Default.BarChart,
                            contentDescription = "",
                            tint = TealSecondary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = Localization.t("روند نشستن روزانه (دقیقه)", "Daily Sitting Trend (Minutes)"),
                            style = MaterialTheme.typography.titleLarge.copy(fontSize = 16.sp, fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Spacer(modifier = Modifier.height(20.dp))

                    if (dailySums.isNotEmpty()) {
                        CustomBarChart(dailySums = dailySums)
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = Localization.t("نموداری برای رسم یافت نشد.", "No trend charts available yet."),
                                color = TextMuted,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }

        // History logs
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = "",
                        tint = TextGray,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = Localization.t("سوابق اخیر", "Recent History"),
                        style = MaterialTheme.typography.titleLarge.copy(fontSize = 18.sp, fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }

                if (sessions.isNotEmpty()) {
                    IconButton(
                        onClick = { showDeleteConfirmDialog = true },
                        colors = IconButtonDefaults.iconButtonColors(contentColor = CoralWarning),
                        modifier = Modifier.testTag("clear_history_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = Localization.t("پاکسازی کل تاریخچه", "Clear entire history")
                        )
                    }
                }
            }
        }

        if (sessions.isEmpty()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .padding(24.dp)
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.EventNote,
                            contentDescription = "",
                            tint = TextMuted,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = Localization.t("تاریخچه‌ای وجود ندارد", "No history available"),
                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = Localization.t(
                                "پس از تکمیل پایش فعالیت‌های روزانه، سوابق شما در اینجا فهرست خواهند شد.",
                                "Once you complete daily sessions, your active/inactive logs will be cataloged here."
                            ),
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextGray,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } else {
            items(sessions, key = { it.id }) { session ->
                SessionLogItem(session = session)
            }
        }
    }

    // Delete trigger dialog
    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = {
                Text(
                    text = Localization.t("پاکسازی کل تاریخچه؟", "Clear Entire History?"),
                    style = MaterialTheme.typography.titleLarge
                )
            },
            text = {
                Text(
                    text = Localization.t(
                        "آیا مطمئن هستید که می‌خواهید کل فعالیت‌ها و سوابق ثبت شده قبلی را حذف نمایید؟ این عملیات غیرقابل بازگشت است.",
                        "Are you absolutely sure you want to clear your entire ergonomic tracking log history? This action cannot be undone."
                    )
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.clearAllHistory()
                        showDeleteConfirmDialog = false
                    }
                ) {
                    Text(
                        text = Localization.t("بله، پاک شود", "Yes, Clear"),
                        color = CoralWarning
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text(
                        text = Localization.t("انصراف", "Cancel"),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        )
    }
}

@Composable
fun CustomBarChart(dailySums: List<Pair<String, Long>>) {
    val maxVal = remember(dailySums) { (dailySums.maxOfOrNull { it.second } ?: 10L).coerceAtLeast(10L) }
    val labelColorArgb = MaterialTheme.colorScheme.onSurface.toArgb()
    val valColorArgb = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f).toArgb()

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp)
    ) {
        val width = size.width
        val height = size.height

        val barCount = dailySums.size
        val gap = 20.dp.toPx()
        val totalGapsWidth = gap * (barCount + 1)
        val barWidth = (width - totalGapsWidth) / barCount

        val minGridY = height * 0.1f
        val maxGridY = height * 0.85f
        val chartHeight = maxGridY - minGridY

        dailySums.forEachIndexed { idx, pair ->
            val ratio = pair.second.toFloat() / maxVal
            val xPos = gap + idx * (barWidth + gap)
            val barHeight = chartHeight * ratio
            val yPos = maxGridY - barHeight

            // Draw bar
            drawRoundRect(
                color = CoralWarning,
                topLeft = Offset(xPos, yPos),
                size = Size(barWidth, barHeight),
                cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx())
            )

            // Label text drawing on Canvas using Native Paint
            drawIntoCanvas { canvas ->
                val paint = android.graphics.Paint().apply {
                    color = labelColorArgb
                    textSize = 10.sp.toPx()
                    textAlign = android.graphics.Paint.Align.CENTER
                    isAntiAlias = true
                }
                
                // Draw date underneath
                val displayLabel = pair.first.substringAfter("-") // Shows MM-DD instead of YYYY-MM-DD
                canvas.nativeCanvas.drawText(
                    displayLabel,
                    xPos + barWidth / 2,
                    maxGridY + 18.dp.toPx(),
                    paint
                )

                // Draw value on top of bar
                val valPaint = android.graphics.Paint().apply {
                    color = valColorArgb
                    textSize = 10.sp.toPx()
                    textAlign = android.graphics.Paint.Align.CENTER
                    isAntiAlias = true
                }
                val labelText = if (Localization.isFarsi()) "${pair.second}د" else "${pair.second}m"
                canvas.nativeCanvas.drawText(
                    labelText,
                    xPos + barWidth / 2,
                    yPos - 6.dp.toPx(),
                    valPaint
                )
            }
        }
    }
}

@Composable
fun SessionLogItem(session: SittingSession) {
    val timeFormatter = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }
    val startTimeStr = remember(session.startTime) { timeFormatter.format(Date(session.startTime)) }
    val endTimeStr = remember(session.endTime) { timeFormatter.format(Date(session.endTime)) }

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(
                            if (session.isSitting) CoralWarning.copy(alpha = 0.15f) else TealPrimary.copy(alpha = 0.15f)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (session.isSitting) Icons.Default.Info else Icons.Default.DirectionsWalk,
                        contentDescription = "",
                        tint = if (session.isSitting) CoralWarning else TealPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = if (session.isSitting) {
                            Localization.t("زمان نشستن متوالی", "Consecutive Sitting")
                        } else {
                            Localization.t("زمان ایستادن متوالی", "Consecutive Standing")
                        },
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (Localization.isFarsi()) {
                            "تاریخ: ${session.dateString}  • ساعت ${startTimeStr} الی ${endTimeStr}"
                        } else {
                            "Date: ${session.dateString}  • Time: ${startTimeStr} to ${endTimeStr}"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = TextGray
                    )
                }
            }

            Text(
                text = "${session.durationSeconds / 60} ${Localization.t("دقیقه", "mins")}",
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                color = if (session.isSitting) CoralWarning else TealPrimary
            )
        }
    }
}

// Custom Row Helper
@Composable
private fun RowScope_Custom(modifier: Modifier = Modifier) {
    // Spacer helper
}

// Helper block to enable weights on Compose Box Row Scope
private fun Box(modifier: Modifier, fillMaxHeight: Boolean) {
    // Empty Box helper
}
