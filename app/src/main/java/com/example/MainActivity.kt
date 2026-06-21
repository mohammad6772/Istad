package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.SelfImprovement
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.data.AppDatabase
import com.example.data.SittingRepository
import com.example.ui.SittingViewModel
import com.example.ui.screens.ExercisesScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.ReportsScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.IstadTheme
import com.example.ui.theme.TealPrimary

import android.content.Context
import com.example.util.LocaleHelper
import com.example.util.Localization

enum class AppScreen(
    val englishKey: String,
    val farsiLabel: String,
    val activeIcon: ImageVector,
    val inactiveIcon: ImageVector
) {
    HOME("Home", "خانه", Icons.Filled.Home, Icons.Outlined.Home),
    EXERCISES("Exercises", "تمرینات کششی", Icons.Filled.SelfImprovement, Icons.Outlined.SelfImprovement),
    REPORTS("Reports", "کارنامه پایش", Icons.Filled.BarChart, Icons.Outlined.BarChart),
    SETTINGS("Settings", "تنظیمات", Icons.Filled.Settings, Icons.Outlined.Settings)
}

class MainActivity : ComponentActivity() {

    override fun attachBaseContext(newBase: Context) {
        val prefs = newBase.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val lang = prefs.getString("app_lang", "fa") ?: "fa"
        super.attachBaseContext(LocaleHelper.setLocale(newBase, lang))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Request POST_NOTIFICATIONS runtime permissions on Android 13 (Tiramisu)+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), 101)
            }
        }

        // Initialize SQLite Room Database layer safely
        val database = AppDatabase.getDatabase(applicationContext)
        val repository = SittingRepository(database.sittingDao())

        // Setup viewModel factory
        val viewModel: SittingViewModel by viewModels {
            SittingViewModel.Factory(repository)
        }

        val prefs = getSharedPreferences("app_prefs", MODE_PRIVATE)
        val lang = prefs.getString("app_lang", "fa") ?: "fa"
        Localization.currentLanguage = lang
        val isDark = prefs.getBoolean("dark_theme", true)

        enableEdgeToEdge()

        setContent {
            IstadTheme(darkTheme = isDark) {
                val layoutDirection = if (Localization.isFarsi()) LayoutDirection.Rtl else LayoutDirection.Ltr
                CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
                    IstadAppContent(viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun IstadAppContent(viewModel: SittingViewModel) {
    var currentScreen by remember { mutableStateOf(AppScreen.HOME) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                AppScreen.values().forEach { screen ->
                    val isSelected = currentScreen == screen
                    val labelText = if (Localization.isFarsi()) screen.farsiLabel else screen.englishKey
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { currentScreen = screen },
                        label = {
                            Text(
                                text = labelText,
                                color = if (isSelected) TealPrimary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        },
                        icon = {
                            Icon(
                                imageVector = if (isSelected) screen.activeIcon else screen.inactiveIcon,
                                contentDescription = labelText,
                                tint = if (isSelected) TealPrimary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    )
                }
            }
        }
    ) { paddingValues ->
        val screenModifier = Modifier.padding(paddingValues)

        when (currentScreen) {
            AppScreen.HOME -> HomeScreen(viewModel = viewModel, modifier = screenModifier)
            AppScreen.EXERCISES -> ExercisesScreen(viewModel = viewModel, modifier = screenModifier)
            AppScreen.REPORTS -> ReportsScreen(viewModel = viewModel, modifier = screenModifier)
            AppScreen.SETTINGS -> SettingsScreen(viewModel = viewModel, modifier = screenModifier)
        }
    }
}
