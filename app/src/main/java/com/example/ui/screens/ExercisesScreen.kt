package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.SittingViewModel
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.TealPrimary
import com.example.ui.theme.TealSecondary
import com.example.ui.theme.TextGray
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextWhite
import com.example.util.Localization
import kotlinx.coroutines.delay

data class LumbarExercise(
    val id: Int,
    val title: String,
    val description: String,
    val benefits: String,
    val durationSeconds: Int
)

@Composable
fun ExercisesScreen(
    viewModel: SittingViewModel,
    modifier: Modifier = Modifier
) {
    val completedExercises by viewModel.completedExercises.collectAsState()

    val initialExercises = remember {
        listOf(
            LumbarExercise(
                id = 1,
                title = Localization.t("کشش به چپ و راست (Side Stretch)", "Side Stretch"),
                description = Localization.t(
                    "بایستید، یک دست خود را به کمر تکیه دهید و دست دیگر را از بالای سر عبور داده و مایل شوید. ۱۰ الی ۱۵ ثانیه برای هر طرف مکث کنید.",
                    "Stand up, place one hand on your hip, and extend the other arm overhead, leaning to the side. Pause for 10-15 seconds on each side."
                ),
                benefits = Localization.t(
                    "آزادسازی ماهیچه‌های جانبی کمر و کاهش فشردگی‌های ستون فقرات ناشی از پهلو خوابیدن یا تکیه کج",
                    "Releases lateral lower back muscles and reduces spinal compression from prolonged sitting."
                ),
                durationSeconds = 30
            ),
            LumbarExercise(
                id = 2,
                title = Localization.t("حرکت کبرا (McKenzie Extension)", "McKenzie Cobra Stretch"),
                description = Localization.t(
                    "شکم خود را به زمین تکیه دهید. بر روی آرنج یا کف دست فشار آورده و سر و قفسه سینه را بالا بیاورید. عضلات باسن را شل نگه دارید.",
                    "Lie face down. Press up on your elbows or palms to lift your chest off the floor, keeping your glutes relaxed."
                ),
                benefits = Localization.t(
                    "بسیار حیاتی برای عقب راندن دیسک‌های فتق‌یافته (L4-S1) و تسکین تیر کشیدن عصب سیاتیک پایینی",
                    "Crucial for reducing intradiscal pressure at L4-S1 and relieving lower back tension."
                ),
                durationSeconds = 45
            ),
            LumbarExercise(
                id = 3,
                title = Localization.t("حرکت گربه شتر (Cat-Camel)", "Cat-Camel Pose"),
                description = Localization.t(
                    "چهار دست و پا روی تشک قرار بگیرید. ستون فقرات را به بیرون گرد کنید (گربه)، سپس کمر را فرو دهید و سر را بالا بیاورید (شتر).",
                    "Get on all fours. Gently arch your spine upward toward the sky (Cat), then let your belly sink while lifting your chest and head (Camel)."
                ),
                benefits = Localization.t(
                    "افزایش جریان خون و روان‌سازی مفاصل ستون فقرات و غضروف‌ها بعد از نشستن ممتد روی مانیتور",
                    "Boosts blood flow and lubricates spinal facet joints and discs after sitting for a long period."
                ),
                durationSeconds = 60
            ),
            LumbarExercise(
                id = 4,
                title = Localization.t("پل لگن (Glute Bridge)", "Glute Bridge"),
                description = Localization.t(
                    "به پشت دراز به هم بکشید و زانوها را خم کنید. عضلات باسن و پشت کمر را فشرده کرده و باسن خود را از زمین جدا کنید. ۵ ثانیه بالای حرکت مکث کنید.",
                    "Lie on your back with knees bent and feet flat. Squeeze your glutes and pull your hips up toward the ceiling. Hold for 5 seconds."
                ),
                benefits = Localization.t(
                    "تقویت زنجیره عضلانی پشتی برای پایدار نگه داشتن کمر هنگام نشستن در موقعیت‌های ناصحیح ارگونومیک",
                    "Strengthens the posterior chain muscles to stabilize the pelvis and support the spine during sitting."
                ),
                durationSeconds = 40
            )
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Explanatory Top Banner
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.SelfImprovement,
                    contentDescription = "",
                    tint = TealPrimary,
                    modifier = Modifier.size(36.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = Localization.t("جلوگیری از عوارض دیسک کمر", "Prevent Lower Back Pain"),
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                    Text(
                        text = Localization.t(
                            "تمرینات فیزیوتراپی کششی ال۵-اس۱ زیر توسط متخصصین جهت به حداقل رساندن فشار بین مهره‌ای پیشنهاد شده‌اند.",
                            "These target stretches are recommended by spinal physical therapists to reduce disk pressure and tension."
                        ),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                    )
                }
            }
        }

        // Exercises lists
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(initialExercises, key = { it.id }) { exercise ->
                val isCompleted = completedExercises.contains(exercise.id)
                ExerciseItem(
                    exercise = exercise,
                    isCompleted = isCompleted,
                    onToggleComplete = { viewModel.toggleExerciseCompletion(exercise.id) }
                )
            }
        }
    }
}

@Composable
fun ExerciseItem(
    exercise: LumbarExercise,
    isCompleted: Boolean,
    onToggleComplete: () -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }
    var timeLeft by remember { mutableIntStateOf(exercise.durationSeconds) }
    var timerRunning by remember { mutableStateOf(false) }

    // Countdown logic
    LaunchedEffect(timerRunning, timeLeft) {
        if (timerRunning && timeLeft > 0) {
            delay(1000)
            timeLeft--
        } else if (timeLeft == 0) {
            timerRunning = false
            onToggleComplete()
        }
    }

    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (isCompleted) TealPrimary.copy(alpha = 0.04f) else MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { isExpanded = !isExpanded }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Checkbox(
                        checked = isCompleted,
                        onCheckedChange = { onToggleComplete() },
                        colors = CheckboxDefaults.colors(
                            checkedColor = TealPrimary,
                            uncheckedColor = TextMuted
                        ),
                        modifier = Modifier.testTag("exercise_checkbox_${exercise.id}")
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = exercise.title,
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = if (isCompleted) TealPrimary else MaterialTheme.colorScheme.onSurface
                            )
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Schedule,
                                contentDescription = "",
                                tint = TextMuted,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${Localization.t("مدت توصیه شده:", "Recommended:")} ${exercise.durationSeconds} ${Localization.t("ثانیه", "sec")}",
                                style = MaterialTheme.typography.labelLarge,
                                color = TextMuted
                            )
                        }
                    }
                }

                Icon(
                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = if (isExpanded) {
                        Localization.t("بستن توضیحات", "Collapse details")
                    } else {
                        Localization.t("مشاهده توضیحات", "View details")
                    },
                    tint = TextMuted
                )
            }

            AnimatedVisibility(
                visible = isExpanded,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                ) {
                    // Description
                    Text(
                        text = Localization.t("نحوه انجام کار:", "Instructions:"),
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = TealSecondary
                        )
                    )
                    Text(
                        text = exercise.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Benefits
                    Text(
                        text = Localization.t("مزیت ارگونومی:", "Ergonomic benefit:"),
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = TealSecondary
                        )
                    )
                    Text(
                        text = exercise.benefits,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        modifier = Modifier.padding(vertical = 4.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Stretch timer player widget
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = {
                                    if (timerRunning) {
                                        timerRunning = false
                                    } else {
                                        if (timeLeft == 0) timeLeft = exercise.durationSeconds
                                        timerRunning = true
                                    }
                                },
                                colors = IconButtonDefaults.iconButtonColors(containerColor = TealPrimary),
                                modifier = Modifier.size(40.dp)
                            ) {
                                Icon(
                                    imageVector = if (timerRunning) Icons.Default.Check else Icons.Default.PlayArrow,
                                    contentDescription = Localization.t("پخش", "Play"),
                                    tint = Color.Black
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = if (timeLeft > 0) {
                                    "${Localization.t("ثانیه مانده:", "Sec remaining:")} $timeLeft"
                                } else {
                                    Localization.t("حرکت کامل شد! 🎉", "Exercise completed! 🎉")
                                },
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (timeLeft == 0) TealPrimary else MaterialTheme.colorScheme.onSurface
                                )
                            )
                        }

                        // Reset stretch
                        IconButton(
                            onClick = {
                                timerRunning = false
                                timeLeft = exercise.durationSeconds
                            },
                            colors = IconButtonDefaults.iconButtonColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = Localization.t("تکرار زمان سنج", "Reset"),
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }
    }
}
