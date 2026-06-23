package com.example.ui

import android.app.AlarmManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.MainActivity
import com.example.data.AlarmActionReceiver
import com.example.data.SittingRepository
import com.example.data.SittingSession
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

import com.example.service.TrackingService

class SittingViewModel(private val repository: SittingRepository) : ViewModel() {

    // Exposure of all database sessions (reactive flow)
    val sessionsList: StateFlow<List<SittingSession>> = repository.allSessions
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Current tracking states linked to unified background service
    val isTracking: StateFlow<Boolean> = TrackingService.isServiceActive
    val isSitting: StateFlow<Boolean> = TrackingService.isSittingState
    val secondsElapsed: StateFlow<Long> = TrackingService.elapsedSeconds
    val sittingLimitMinutes: StateFlow<Int> = TrackingService.limitMinutesState
    val hasExceededLimit: StateFlow<Boolean> = TrackingService.hasExceededLimit
    val isAutoDetectMotion: StateFlow<Boolean> = TrackingService.isAutoDetectMotion
    val soundSelected: StateFlow<String> = TrackingService.soundSelected

    // Daily Exercises checklist completion state
    private val _completedExercises = MutableStateFlow(setOf<Int>())
    val completedExercises: StateFlow<Set<Int>> = _completedExercises.asStateFlow()

    fun setSittingLimit(minutes: Int, context: Context) {
        val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        prefs.edit().putInt("sitting_limit", minutes).apply()

        val intent = Intent(context, TrackingService::class.java).apply {
            action = TrackingService.ACTION_UPDATE_LIMIT
            putExtra(TrackingService.EXTRA_LIMIT_MINUTES, minutes)
        }
        context.startService(intent)
    }

    fun setSoundSelected(sound: String, context: Context) {
        val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        prefs.edit().putString("selected_sound", sound).apply()
        TrackingService.soundSelected.value = sound
    }

    fun setAutoDetectMotion(enabled: Boolean, context: Context) {
        val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        prefs.edit().putBoolean("auto_detect", enabled).apply()
        TrackingService.isAutoDetectMotion.value = enabled
    }

    fun playSoundPreview(context: Context, sound: String) {
        val intent = Intent(context, TrackingService::class.java).apply {
            action = TrackingService.ACTION_PREVIEW
            putExtra("sound_type", sound)
        }
        context.startService(intent)
    }

    fun toggleExerciseCompletion(id: Int) {
        val current = _completedExercises.value
        _completedExercises.value = if (current.contains(id)) {
            current - id
        } else {
            current + id
        }
    }

    fun startTracking(context: Context, isSittingSession: Boolean) {
        val intent = Intent(context, TrackingService::class.java).apply {
            action = TrackingService.ACTION_START
            putExtra(TrackingService.EXTRA_IS_SITTING, isSittingSession)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent)
        } else {
            context.startService(intent)
        }
    }

    fun switchMode(context: Context) {
        val intent = Intent(context, TrackingService::class.java).apply {
            action = TrackingService.ACTION_SWITCH
        }
        context.startService(intent)
    }

    fun stopTracking(context: Context) {
        val intent = Intent(context, TrackingService::class.java).apply {
            action = TrackingService.ACTION_STOP
        }
        context.startService(intent)
    }

    fun snoozeAlarm(context: Context) {
        val intent = Intent(context, TrackingService::class.java).apply {
            action = TrackingService.ACTION_SNOOZE
        }
        context.startService(intent)
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            repository.clearAll()
        }
    }

    // ViewModel Factory pattern
    class Factory(private val repo: SittingRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(SittingViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return SittingViewModel(repo) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
