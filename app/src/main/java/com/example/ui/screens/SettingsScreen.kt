package com.example.ui.screens

import android.content.Context
import android.media.RingtoneManager
import android.widget.Toast
import com.example.util.LocaleHelper
import com.example.util.Localization
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.DeveloperMode
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.SittingViewModel
import com.example.ui.theme.TealPrimary
import com.example.ui.theme.TealSecondary
import com.example.ui.theme.TextGray
import com.example.ui.theme.TextWhite
import com.example.ui.theme.CoralWarning

@Composable
fun SettingsScreen(
    viewModel: SittingViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val limitMinutes by viewModel.sittingLimitMinutes.collectAsState()
    val isAutoDetectEnabled by viewModel.isAutoDetectMotion.collectAsState()
    val selectedSound by viewModel.soundSelected.collectAsState()
    val scrollState = rememberScrollState()

    var manualInputText by remember(limitMinutes) { mutableStateOf(limitMinutes.toString()) }
    var inputErrorText by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = Localization.t("تنظیمات یادآور ایستا", "Ista Reminder Settings"),
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            ),
            modifier = Modifier.fillMaxWidth(),
            textAlign = if (Localization.isFarsi()) TextAlign.Right else TextAlign.Left
        )

        // 1. Language Option Card (Farsi / English)
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Start
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "زبان / Language",
                        tint = TealPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = Localization.t("زبان اپلیکیشن / Language", "Select Language / زبان"),
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    // Farsi Option
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable {
                            if (Localization.currentLanguage != "fa") {
                                LocaleHelper.setLocale(context, "fa")
                                (context as? android.app.Activity)?.recreate()
                            }
                        }
                    ) {
                        RadioButton(
                            selected = (Localization.currentLanguage == "fa"),
                            onClick = {
                                if (Localization.currentLanguage != "fa") {
                                    LocaleHelper.setLocale(context, "fa")
                                    (context as? android.app.Activity)?.recreate()
                                }
                            },
                            colors = RadioButtonDefaults.colors(selectedColor = TealPrimary)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "فارسی", color = MaterialTheme.colorScheme.onSurface)
                    }

                    // English Option
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable {
                            if (Localization.currentLanguage != "en") {
                                LocaleHelper.setLocale(context, "en")
                                (context as? android.app.Activity)?.recreate()
                            }
                        }
                    ) {
                        RadioButton(
                            selected = (Localization.currentLanguage == "en"),
                            onClick = {
                                if (Localization.currentLanguage != "en") {
                                    LocaleHelper.setLocale(context, "en")
                                    (context as? android.app.Activity)?.recreate()
                                }
                            },
                            colors = RadioButtonDefaults.colors(selectedColor = TealPrimary)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "English", color = MaterialTheme.colorScheme.onSurface)
                    }
                }
            }
        }

        // 2. Theme Option Card (Light / Dark)
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Start
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "پوسته / Theme",
                        tint = TealPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = Localization.t("پوسته برنامه (تم)", "App Theme"),
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))

                val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
                val isDark = prefs.getBoolean("dark_theme", true)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    // Dark Option
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable {
                            if (!isDark) {
                                prefs.edit().putBoolean("dark_theme", true).apply()
                                (context as? android.app.Activity)?.recreate()
                            }
                        }
                    ) {
                        RadioButton(
                            selected = isDark,
                            onClick = {
                                if (!isDark) {
                                    prefs.edit().putBoolean("dark_theme", true).apply()
                                    (context as? android.app.Activity)?.recreate()
                                }
                            },
                            colors = RadioButtonDefaults.colors(selectedColor = TealPrimary)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = Localization.t("تیره (Dark)", "Dark"), color = MaterialTheme.colorScheme.onSurface)
                    }

                    // Light Option
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable {
                            if (isDark) {
                                prefs.edit().putBoolean("dark_theme", false).apply()
                                (context as? android.app.Activity)?.recreate()
                            }
                        }
                    ) {
                        RadioButton(
                            selected = !isDark,
                            onClick = {
                                if (isDark) {
                                    prefs.edit().putBoolean("dark_theme", false).apply()
                                    (context as? android.app.Activity)?.recreate()
                                }
                            },
                            colors = RadioButtonDefaults.colors(selectedColor = TealPrimary)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = Localization.t("روشن (Light)", "Light"), color = MaterialTheme.colorScheme.onSurface)
                    }
                }
            }
        }

        // 3. Adjust/Manual Setting of Sitting Limit Card
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Start
                ) {
                    Icon(
                        imageVector = Icons.Default.Timer,
                        contentDescription = "زمان حد مجاز",
                        tint = TealPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = Localization.t("حد مجاز نشستن مداوم", "Continuous Sitting Limit"),
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = Localization.t(
                        "مدت مجاز نشستن را توسط اسلایدر زیر یا به صورت عددی تایپ کنید (بین ۱ تا ۳۰۰ دقیقه):",
                        "Set the sitting limit using the slider or input time manually (1 to 300 minutes):"
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextGray
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Display Slider
                Slider(
                    value = limitMinutes.coerceIn(15, 90).toFloat(),
                    onValueChange = { 
                        val mins = it.toInt()
                        viewModel.setSittingLimit(mins, context)
                    },
                    valueRange = 15f..90f,
                    steps = 4,
                    colors = SliderDefaults.colors(
                        thumbColor = TealPrimary,
                        activeTrackColor = TealPrimary,
                        inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("limit_slider")
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(Localization.t("۱۵ دقیقه", "15 Min"), style = MaterialTheme.typography.labelLarge, color = TextGray)
                    Text(Localization.t("۴۵ دقیقه", "45 Min"), style = MaterialTheme.typography.labelLarge, color = TextGray)
                    Text(Localization.t("۹۰ دقیقه", "90 Min"), style = MaterialTheme.typography.labelLarge, color = TextGray)
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Manual Text Input Entry Fields
                OutlinedTextField(
                    value = manualInputText,
                    onValueChange = { entry ->
                        val digits = entry.filter { it.isDigit() }
                        manualInputText = digits
                        val parsed = digits.toIntOrNull()
                        if (parsed == null) {
                            inputErrorText = Localization.t("یک عدد معتبر وارد کنید", "Enter a valid number")
                        } else if (parsed < 1 || parsed > 300) {
                            inputErrorText = Localization.t("مدت مجاز باید بین ۱ و ۳۰۰ دقیقه باشد", "Limit must be between 1 and 300 minutes")
                        } else {
                            inputErrorText = null
                            viewModel.setSittingLimit(parsed, context)
                        }
                    },
                    label = { Text(Localization.t("تنظیم دستی مدت زمان (دقیقه)", "Set time manually (minutes)"), color = TextGray) },
                    isError = inputErrorText != null,
                    supportingText = {
                        inputErrorText?.let {
                            Text(text = it, color = CoralWarning, style = MaterialTheme.typography.bodySmall)
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                        focusedBorderColor = TealPrimary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.surfaceVariant,
                        errorBorderColor = CoralWarning,
                        errorLabelColor = CoralWarning
                    ),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Done
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("manual_duration_input")
                )
            }
        }

        // 4. Alarm Sound Custom Preference Card
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Start
                ) {
                    Icon(
                        imageVector = Icons.Default.VolumeUp,
                        contentDescription = "صدای زنگ هشدار",
                        tint = TealPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = Localization.t("انتخاب صدای هشدار صوتی", "Choose Sound Notification Alert"),
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = Localization.t(
                        "صدای زنگ دلخواه خود را گزینش کنید و جهت شنیدن دکمه پخش پیش‌نمایش را فشار دهید:",
                        "Choose your favorite ringtone sound and tap Play to preview:"
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextGray
                )
                Spacer(modifier = Modifier.height(16.dp))

                val systemRingtones = remember {
                    try {
                        val manager = RingtoneManager(context)
                        manager.setType(RingtoneManager.TYPE_NOTIFICATION)
                        val cursor = manager.cursor
                        val list = mutableListOf<Pair<String, String>>()
                        var count = 0
                        while (cursor.moveToNext() && count < 8) {
                            val title = cursor.getString(RingtoneManager.TITLE_COLUMN_INDEX)
                            val uri = manager.getRingtoneUri(cursor.position).toString()
                            list.add(uri to title)
                            count++
                        }
                        list
                    } catch (e: Exception) {
                        emptyList<Pair<String, String>>()
                    }
                }

                val soundOptions = remember(systemRingtones) {
                    val list = mutableListOf(
                        "default" to Localization.t("زنگ پیش‌فرض استاندارد", "Standard Default Ring"),
                        "gong" to Localization.t("ناقوس آرامش‌بخش ذن (Zen Gong)", "Zen Gong"),
                        "beep" to Localization.t("بوق هشدار دیجیتال پیاپی", "Digital Wave Beep"),
                        "piano" to Localization.t("ملودی ملایم پیانو", "Soft Piano Chord")
                    )
                    systemRingtones.forEach { (uri, title) ->
                        list.add(uri to "${Localization.t("سیستم", "System")}: $title")
                    }
                    list
                }

                soundOptions.forEach { (optionKey, optionLabel) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.setSoundSelected(optionKey, context) }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            RadioButton(
                                selected = (selectedSound == optionKey),
                                onClick = { viewModel.setSoundSelected(optionKey, context) },
                                colors = RadioButtonDefaults.colors(
                                    selectedColor = TealPrimary,
                                    unselectedColor = TextGray
                                )
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = optionLabel,
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (selectedSound == optionKey) TealPrimary else MaterialTheme.colorScheme.onSurface
                            )
                        }

                        // Play Preview Button
                        Button(
                            onClick = { viewModel.playSoundPreview(context, optionKey) },
                            colors = ButtonDefaults.buttonColors(containerColor = TealSecondary.copy(alpha = 0.2f)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = Localization.t("شنیدن 🔊", "Play 🔊"),
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                color = TealSecondary
                            )
                        }
                    }
                }
            }
        }

        // 5. Sensor Motion Auto Detection Card
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.DirectionsWalk,
                            contentDescription = "سنسور حرکت",
                            tint = TealSecondary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = Localization.t("شناسایی خودکار سنسور حرکت", "Auto Motion Sensor Tracking"),
                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Switch(
                        checked = isAutoDetectEnabled,
                        onCheckedChange = { viewModel.setAutoDetectMotion(it, context) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = TealPrimary,
                            checkedTrackColor = TealPrimary.copy(alpha = 0.4f),
                            uncheckedThumbColor = TextGray,
                            uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = Localization.t(
                        "با فعال‌سازی این قابلیت، به کمک سنسور شتاب‌سنج داخلی موبایل در صورت بلند شدن و راه رفتن نشستن شما تشخیص داده شده و شمارشگر نشستن قطع و به حالت ایستاده تغییر می‌کند.",
                        "When enabled, the internal accelerometer sensor detects when you stand up and walk. It automatically switches the state to Standing and pauses the sitting timer."
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextGray
                )
            }
        }

        // 6. Ergonomical Health Guidelines (Desk setup)
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = Localization.t("وضعیت نشستن ارگونومیک (صحیح)", "Ergonomic Sitting Guidelines"),
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))

                val ergoTips = listOf(
                    Localization.t(
                        "مانیتور هم‌سطح چشم: لبه بالایی صفحه موازی با چشمان باشد.",
                        "Monitor at eye level: Upper edge parallel with your eyes."
                    ),
                    Localization.t(
                        "تکیه کامل کمر: قوس ستون فقرات با پشتی کامپکت مجهز باشد.",
                        "Full back support: Fill the spine curve using a physical cushion."
                    ),
                    Localization.t(
                        "زاویه ۹۰ درجه زانو: هر دو پا کاملاً تخت روی کف اتاق فیکس دائم قرار گیرد.",
                        "90-degree knees: Keep feet flat on the ground comfortably."
                    )
                )

                ergoTips.forEach { tip ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(text = "💡 ", style = MaterialTheme.typography.bodyMedium)
                        Text(
                            text = tip,
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextGray
                        )
                    }
                }
            }
        }

        // 7. System Credits & Developer section
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Start
                ) {
                    Icon(
                        imageVector = Icons.Default.DeveloperMode,
                        contentDescription = "طراح سیستم",
                        tint = TealPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = Localization.t("طراح و توسعه‌دهنده سیستم", "System Designer & Developer"),
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = Localization.t("محمد نوروزی تبار", "Mohammad Norouzi Tabar"),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = Localization.t(
                        "ایمیل توسعه و پشتیبانی رسمی:\nnorouzi.mhmd88@gmail.com",
                        "Development & Support Email:\nnorouzi.mhmd88@gmail.com"
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    color = TealSecondary,
                    lineHeight = 20.sp
                )
            }
        }

        // Version info card
        Text(
            text = Localization.t(
                "برنامه سلامت و ارگونومی ایستا • نسخه ۱.۱.۰",
                "Ista Ergonomics & Health App • Version 1.1.0"
            ),
            style = MaterialTheme.typography.bodySmall,
            color = TextGray.copy(alpha = 0.5f),
            modifier = Modifier.padding(vertical = 12.dp)
        )
    }
}
