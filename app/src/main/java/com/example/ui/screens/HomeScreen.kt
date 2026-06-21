package com.example.ui.screens

import android.content.Context
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.rounded.AirlineSeatReclineNormal
import androidx.compose.material.icons.rounded.DirectionsWalk
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.SittingViewModel
import com.example.ui.theme.CoralWarning
import com.example.ui.theme.TealPrimary
import com.example.ui.theme.TealSecondary
import com.example.ui.theme.TextGray
import com.example.ui.theme.TextWhite
import com.example.util.Localization
import java.util.Locale

@Composable
fun HomeScreen(
    viewModel: SittingViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isTracking by viewModel.isTracking.collectAsState()
    val isSitting by viewModel.isSitting.collectAsState()
    val secondsElapsed by viewModel.secondsElapsed.collectAsState()
    val limitMinutes by viewModel.sittingLimitMinutes.collectAsState()
    val hasExceededLimit by viewModel.hasExceededLimit.collectAsState()

    // Conversions for UI
    val limitSeconds = limitMinutes * 60L
    val progressFraction = if (isSitting && limitSeconds > 0) {
        (secondsElapsed.toFloat() / limitSeconds).coerceIn(0f, 1f)
    } else {
        1f
    }

    // Color animations for states
    val stateColor by animateColorAsState(
        targetValue = when {
            !isTracking -> MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
            hasExceededLimit -> CoralWarning
            !isSitting -> TealSecondary
            else -> TealPrimary
        },
        label = "stateColor"
    )

    // Pulsing scale transition for the center warning ring
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = if (hasExceededLimit) 1.06f else 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // App header and status banner
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = Localization.t("ایستا", "Ista"),
                style = MaterialTheme.typography.displayLarge.copy(
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Black,
                    color = TealPrimary
                ),
                textAlign = TextAlign.Center
            )
            Text(
                text = Localization.t("یادآور ارگونومی و محافظت دیسک کمر", "Ergonomics & Lumbar Protection"),
                style = MaterialTheme.typography.bodyMedium,
                color = TextGray,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(20.dp))

            // Dynamic warnings and banners
            if (isTracking) {
                if (hasExceededLimit) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = CoralWarning.copy(alpha = 0.15f)),
                        border = borderStroke(CoralWarning),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Start
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = Localization.t("هشدار ایستادن", "Standing Warning"),
                                tint = CoralWarning,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = Localization.t("زمان برخاستن فرا رسیده است!", "Time to stand up!"),
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = CoralWarning
                                    )
                                )
                                Text(
                                    text = Localization.t("بیش از حد معین نشسته‌اید، لطفاً چند دقیقه راه بروید.", "You've been sitting longer than allowed. Please walk for a few minutes."),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                } else {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSitting) TealPrimary.copy(alpha = 0.08f) else TealSecondary.copy(alpha = 0.08f)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Schedule,
                                contentDescription = Localization.t("در حال پاییش", "Monitoring"),
                                tint = if (isSitting) TealPrimary else TealSecondary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = if (isSitting) {
                                    Localization.t("شما در حال حاضر نشسته هستید.", "You are currently sitting.")
                                } else {
                                    Localization.t("شما هم‌اکنون ایستاده هستید.", "You are currently standing.")
                                },
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            )
                        }
                    }
                }
            } else {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = Localization.t("پایش متوقف است", "Tracking is inactive"),
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = Localization.t(
                                "برای شروع ثبت و یادآوری ارگونومیک، گزینه شروع نشستن را انتخاب فرمایید.",
                                "Tap Start Sitting to begin tracking and receive ergonomic posture notifications."
                            ),
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextGray,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }

        // Animated Circle Timer / Status Ring
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxWidth(0.72f)
                .aspectRatio(1f)
                .scale(pulseScale)
        ) {
            // Background track gradient circle
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
                    .border(
                        width = 8.dp,
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = CircleShape
                    )
            )

            // Colorful active status arc outline (represented here elegantly via linear gradient border)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(4.dp)
                    .clip(CircleShape)
                    .border(
                        width = 6.dp,
                        brush = Brush.sweepGradient(
                            listOf(
                                stateColor,
                                stateColor.copy(alpha = 0.4f),
                                stateColor
                            )
                        ),
                        shape = CircleShape
                    )
            )

            // Center content inside the circle
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = if (isSitting) Icons.Rounded.AirlineSeatReclineNormal else Icons.Rounded.DirectionsWalk,
                    contentDescription = "",
                    tint = stateColor,
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = if (isTracking) formatDuration(secondsElapsed) else "00:00",
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontSize = 44.sp,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                )
                Text(
                    text = if (!isTracking) {
                        Localization.t("غیرفعال", "Inactive")
                    } else if (isSitting) {
                        Localization.t("زمان نشستن", "Sitting Time")
                    } else {
                        Localization.t("زمان ایستادن", "Standing Time")
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextGray
                )
            }
        }

        // Control Panel Buttons
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (!isTracking) {
                // Button to start tracking sitting session
                Button(
                    onClick = { viewModel.startTracking(context, isSittingSession = true) },
                    colors = ButtonDefaults.buttonColors(containerColor = TealPrimary),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .testTag("start_sitting_button"),
                    shape = RoundedCornerShape(27.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = Localization.t("شروع پایش نشستن", "Start Sitting Tracking"),
                        tint = Color.Black
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = Localization.t("شروع پایش نشستن", "Start Sitting Tracking"),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                }
            } else {
                // Active session triggers
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Transition trigger (Switch Sitting / Standing)
                    Button(
                        onClick = { viewModel.switchMode(context) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isSitting) TealSecondary else TealPrimary
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .height(54.dp)
                            .testTag("switch_mode_button"),
                        shape = RoundedCornerShape(27.dp)
                    ) {
                        Icon(
                            imageVector = if (isSitting) Icons.Rounded.DirectionsWalk else Icons.Rounded.AirlineSeatReclineNormal,
                            contentDescription = Localization.t("جابجایی حالت", "Switch Mode"),
                            tint = Color.Black
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isSitting) Localization.t("بلند شدم! 🚶", "Walk! 🚶") else Localization.t("نشستم! 🪑", "Sit! 🪑"),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    // Pause/Stop trigger
                    OutlinedButton(
                        onClick = { viewModel.stopTracking(context) },
                        modifier = Modifier
                            .weight(1f)
                            .height(54.dp)
                            .testTag("stop_tracking_button"),
                        shape = RoundedCornerShape(27.dp),
                        border = borderStroke(CoralWarning)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Stop,
                            contentDescription = Localization.t("توقف یادآور", "Stop Reminder"),
                            tint = CoralWarning
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = Localization.t("پایان پایش", "End Tracking"),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = CoralWarning
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "${Localization.t("حد هشدار نشستن", "Sitting Limit Alert")}: $limitMinutes ${Localization.t("دقیقه", "minutes")}",
                    style = MaterialTheme.typography.labelLarge,
                    color = TextGray
                )
            }
        }
    }
}

// Utility stroke builder to handle border on composables cleanly
private fun borderStroke(color: Color) = androidx.compose.foundation.BorderStroke(
    width = 1.5.dp,
    color = color.copy(alpha = 0.5f)
)

private fun formatDuration(totalSeconds: Long): String {
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
}
