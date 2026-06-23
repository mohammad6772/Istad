package com.example.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.data.AppDatabase
import com.example.data.SittingRepository
import com.example.data.SittingSession
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TrackingService : Service(), SensorEventListener {

    companion object {
        const val CHANNEL_ID = "istad_background_channel"
        const val NOTIFICATION_ID = 5005

        const val ACTION_START = "ACTION_START"
        const val ACTION_STOP = "ACTION_STOP"
        const val ACTION_SWITCH = "ACTION_SWITCH"
        const val ACTION_UPDATE_LIMIT = "ACTION_UPDATE_LIMIT"
        const val ACTION_PREVIEW = "ACTION_PREVIEW"
        const val ACTION_SNOOZE = "ACTION_SNOOZE"

        const val EXTRA_IS_SITTING = "EXTRA_IS_SITTING"
        const val EXTRA_LIMIT_MINUTES = "EXTRA_LIMIT_MINUTES"

        // Live States exposed thread-safely
        val isServiceActive = MutableStateFlow(false)
        val isSittingState = MutableStateFlow(true)
        val elapsedSeconds = MutableStateFlow(0L)
        val limitMinutesState = MutableStateFlow(45)
        val isAutoDetectMotion = MutableStateFlow(true)
        val soundSelected = MutableStateFlow("default") // "default", "gong", "beep", "piano"
        val hasExceededLimit = MutableStateFlow(false)
        val snoozeSecondsState = MutableStateFlow(0L)
    }

    private val serviceScope = CoroutineScope(Dispatchers.Main + Job())
    private var tickerJob: Job? = null
    private var sessionStartTime: Long = 0L

    private lateinit var repository: SittingRepository
    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null

    // Accelerometer filtering
    private var baseline = 9.8f
    private var lastStepTime = 0L
    private var motionMatchCount = 0

    override fun onCreate() {
        super.onCreate()
        val database = AppDatabase.getDatabase(applicationContext)
        repository = SittingRepository(database.sittingDao())
        
        // Sync persistent settings
        val prefs = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        limitMinutesState.value = prefs.getInt("sitting_limit", 45)
        isAutoDetectMotion.value = prefs.getBoolean("auto_detect", true)
        soundSelected.value = prefs.getString("selected_sound", "default") ?: "default"

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        if (accelerometer != null && isAutoDetectMotion.value) {
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL)
        }

        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action

        when (action) {
            ACTION_START -> {
                val isSitting = intent.getBooleanExtra(EXTRA_IS_SITTING, true)
                startTracking(isSitting)
            }
            ACTION_SWITCH -> {
                switchTrackingMode()
            }
            ACTION_STOP -> {
                stopTrackingService()
            }
            ACTION_UPDATE_LIMIT -> {
                val newLimit = intent.getIntExtra(EXTRA_LIMIT_MINUTES, limitMinutesState.value)
                limitMinutesState.value = newLimit
            }
            ACTION_PREVIEW -> {
                val previewSound = intent?.getStringExtra("sound_type") ?: "default"
                serviceScope.launch(Dispatchers.IO) {
                    playSynthesizedSound(previewSound)
                }
            }
            ACTION_SNOOZE -> {
                snoozeAlarm()
            }
        }

        return START_NOT_STICKY
    }

    private fun startTracking(isSitting: Boolean) {
        if (isServiceActive.value) {
            saveCurrentSession()
        }

        isServiceActive.value = true
        isSittingState.value = isSitting
        elapsedSeconds.value = 0L
        hasExceededLimit.value = false
        snoozeSecondsState.value = 0L
        stopAlarmLoop()
        sessionStartTime = System.currentTimeMillis()

        // Sync sensor registrations
        if (isSitting && isAutoDetectMotion.value) {
            registerAccelerometer()
        } else {
            unregisterAccelerometer()
        }

        startForeground(NOTIFICATION_ID, buildNotification())
        startTicker()
    }

    private fun switchTrackingMode() {
        if (!isServiceActive.value) return

        saveCurrentSession()
        
        val newIsSitting = !isSittingState.value
        isSittingState.value = newIsSitting
        elapsedSeconds.value = 0L
        hasExceededLimit.value = false
        snoozeSecondsState.value = 0L
        stopAlarmLoop()
        sessionStartTime = System.currentTimeMillis()

        if (newIsSitting && isAutoDetectMotion.value) {
            registerAccelerometer()
        } else {
            unregisterAccelerometer()
        }

        updateNotification()
    }

    private fun startTicker() {
        tickerJob?.cancel()
        tickerJob = serviceScope.launch {
            while (isActive) {
                delay(1000)
                elapsedSeconds.value++

                // Exceed checking
                if (isSittingState.value && !hasExceededLimit.value) {
                    val limitSeconds = limitMinutesState.value * 60L + snoozeSecondsState.value
                    if (elapsedSeconds.value >= limitSeconds) {
                        hasExceededLimit.value = true
                        startAlarmLoop()
                    }
                }
                updateNotification()
            }
        }
    }

    private var alarmJob: Job? = null
    private var activeRingtone: android.media.Ringtone? = null

    private fun startAlarmLoop() {
        alarmJob?.cancel()
        alarmJob = serviceScope.launch(Dispatchers.IO) {
            while (isActive && hasExceededLimit.value && isSittingState.value && isServiceActive.value) {
                playSynthesizedSound(soundSelected.value)
                val sleepTime = if (soundSelected.value == "gong") 4500L
                                else if (soundSelected.value == "piano") 3500L
                                else 2500L
                delay(sleepTime)
            }
        }
    }

    private fun stopAlarmLoop() {
        alarmJob?.cancel()
        alarmJob = null
        try {
            activeRingtone?.stop()
            activeRingtone = null
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun snoozeAlarm() {
        snoozeSecondsState.value += 5 * 60L
        hasExceededLimit.value = false
        stopAlarmLoop()
        updateNotification()
    }

    private fun playSynthesizedSound(type: String) {
        if (type.startsWith("content://")) {
            try {
                val uri = android.net.Uri.parse(type)
                val ringtone = android.media.RingtoneManager.getRingtone(applicationContext, uri)
                activeRingtone?.stop()
                activeRingtone = ringtone
                ringtone?.play()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return
        }

        val sampleRate = 8000
        val durationSeconds = if (type == "gong") 2.5 else if (type == "piano") 1.8 else 0.8
        val numSamples = (durationSeconds * sampleRate).toInt()
        val samples = FloatArray(numSamples)

        when (type) {
            "gong" -> {
                // Zen Bell resonance sound
                for (i in 0 until numSamples) {
                    val t = i.toFloat() / sampleRate
                    val decay = Math.exp(-1.5 * t).toFloat()
                    samples[i] = (Math.sin(2.0 * Math.PI * 150.0 * t) * 0.5 + 
                                 Math.sin(2.0 * Math.PI * 300.0 * t) * 0.25).toFloat() * decay
                }
            }
            "beep" -> {
                // Warning beops
                for (i in 0 until numSamples) {
                    val t = i.toFloat() / sampleRate
                    val block = (t * 5).toInt() % 2
                    if (block == 0) {
                        samples[i] = Math.sin(2.0 * Math.PI * 880.0 * t).toFloat() * 0.6f
                    } else {
                        samples[i] = 0.0f
                    }
                }
            }
            "piano" -> {
                // Soft double chords
                for (i in 0 until numSamples) {
                    val t = i.toFloat() / sampleRate
                    val decay = Math.exp(-1.0 * t).toFloat()
                    samples[i] = (Math.sin(2.0 * Math.PI * 440.0 * t) * 0.4 + 
                                 Math.sin(2.0 * Math.PI * 554.37 * t) * 0.3).toFloat() * decay
                }
            }
            else -> {
                // Default clean tone
                for (i in 0 until numSamples) {
                    val t = i.toFloat() / sampleRate
                    samples[i] = Math.sin(2.0 * Math.PI * 660.0 * t).toFloat() * 0.5f
                }
            }
        }

        val buffer = ShortArray(numSamples)
        for (i in 0 until numSamples) {
            val scaledVal = (samples[i] * 32767).toInt()
            buffer[i] = scaledVal.coerceIn(-32768, 32767).toShort()
        }

        try {
            val audioTrack = android.media.AudioTrack(
                android.media.AudioManager.STREAM_NOTIFICATION,
                sampleRate,
                android.media.AudioFormat.CHANNEL_OUT_MONO,
                android.media.AudioFormat.ENCODING_PCM_16BIT,
                buffer.size * 2,
                android.media.AudioTrack.MODE_STATIC
            )
            audioTrack.write(buffer, 0, buffer.size)
            audioTrack.play()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun autoSwitchToStanding() {
        serviceScope.launch(Dispatchers.Main) {
            // Only switch automatically if we are currently sitting and tracking
            if (isServiceActive.value && isSittingState.value) {
                // Trigger sound play to let user know standing was automatically registered!
                serviceScope.launch(Dispatchers.IO) {
                    playSynthesizedSound("piano") 
                }
                switchTrackingMode()
            }
        }
    }

    private fun saveCurrentSession() {
        val duration = elapsedSeconds.value
        if (duration <= 3) return // Ignore instant clicks

        val startTime = sessionStartTime
        val endTime = System.currentTimeMillis()
        val isSit = isSittingState.value
        val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val dateStr = formatter.format(Date(startTime))

        serviceScope.launch(Dispatchers.IO) {
            repository.insertSession(
                SittingSession(
                    startTime = startTime,
                    endTime = endTime,
                    durationSeconds = duration,
                    isSitting = isSit,
                    dateString = dateStr
                )
            )
        }
    }

    private fun stopTrackingService() {
        if (isServiceActive.value) {
            saveCurrentSession()
        }

        isServiceActive.value = false
        elapsedSeconds.value = 0L
        hasExceededLimit.value = false
        snoozeSecondsState.value = 0L
        stopAlarmLoop()
        tickerJob?.cancel()

        unregisterAccelerometer()
        stopForeground(true)
        stopSelf()
    }

    private fun registerAccelerometer() {
        if (accelerometer != null && isAutoDetectMotion.value) {
            sensorManager.unregisterListener(this)
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    private fun unregisterAccelerometer() {
        sensorManager.unregisterListener(this)
        motionMatchCount = 0
        lastStepTime = 0L
        baseline = 9.8f
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null || event.sensor.type != Sensor.TYPE_ACCELEROMETER) return
        if (!isServiceActive.value || !isSittingState.value) return 
        if (!isAutoDetectMotion.value) return

        val x = event.values[0]
        val y = event.values[1]
        val z = event.values[2]

        val magnitude = Math.sqrt((x * x + y * y + z * z).toDouble()).toFloat()

        // Track gravity baseline using low-pass exponential moving average
        baseline = 0.95f * baseline + 0.05f * magnitude

        val dynamicAcceleration = Math.abs(magnitude - baseline)
        val currentTime = System.currentTimeMillis()

        // 2.8f is a robust threshold for actual rhythmic walking steps,
        // preventing triggers when simply lifting or rearranging the phone.
        if (dynamicAcceleration > 2.8f) {
            // A realistic human step occurs with an interval between 350ms and 3000ms.
            if (currentTime - lastStepTime > 350L) {
                if (currentTime - lastStepTime > 3000L && lastStepTime != 0L) {
                    // Too long since last step, reset chain but count this as step #1
                    motionMatchCount = 1
                } else {
                    motionMatchCount++
                }
                
                lastStepTime = currentTime

                // If 3 rhythmic continuous steps are completed
                if (motionMatchCount >= 3) {
                    motionMatchCount = 0
                    autoSwitchToStanding()
                }
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    private fun updateNotification() {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, buildNotification())
    }

    private fun buildNotification(): Notification {
        val launchIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            launchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val switchIntent = Intent(this, TrackingService::class.java).apply {
            action = ACTION_SWITCH
        }
        val pSwitchIntent = PendingIntent.getService(
            this,
            1,
            switchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val stopIntent = Intent(this, TrackingService::class.java).apply {
            action = ACTION_STOP
        }
        val pStopIntent = PendingIntent.getService(
            this,
            2,
            stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val durationFormatted = formatDuration(elapsedSeconds.value)
        val modeText = if (isSittingState.value) {
            if (com.example.util.Localization.isFarsi()) "نشسته" else "Sitting"
        } else {
            if (com.example.util.Localization.isFarsi()) "ایستاده" else "Standing"
        }
        val statusText = if (com.example.util.Localization.isFarsi()) {
            if (snoozeSecondsState.value > 0 && isSittingState.value) {
                "شما در حالت $modeText هستید • مدت: $durationFormatted (تأخیر فعال)"
            } else {
                "شما در حالت $modeText هستید • مدت: $durationFormatted"
            }
        } else {
            if (snoozeSecondsState.value > 0 && isSittingState.value) {
                "Active state: $modeText • Time: $durationFormatted (Snoozed)"
            } else {
                "Active state: $modeText • Time: $durationFormatted"
            }
        }

        val title = if (hasExceededLimit.value) {
            if (com.example.util.Localization.isFarsi()) "⚠️ خطر نشستن طولانی!" else "⚠️ Prolonged Sitting Alert!"
        } else {
            if (com.example.util.Localization.isFarsi()) "طرح سلامت کمر ایستاد" else "Ista Lumbar Protection"
        }

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(statusText)
            .setOnlyAlertOnce(true)
            .setContentIntent(pendingIntent)
            .setPriority(if (hasExceededLimit.value) NotificationCompat.PRIORITY_HIGH else NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .addAction(
                android.R.drawable.ic_menu_directions,
                if (isSittingState.value) {
                    if (com.example.util.Localization.isFarsi()) "بلند شدم 🚶" else "I stood up 🚶"
                } else {
                    if (com.example.util.Localization.isFarsi()) "نشستم 🪑" else "I sat down 🪑"
                },
                pSwitchIntent
            )

        if (hasExceededLimit.value) {
            val snoozeIntent = Intent(this, TrackingService::class.java).apply {
                action = ACTION_SNOOZE
            }
            val pSnoozeIntent = PendingIntent.getService(
                this,
                3,
                snoozeIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            builder.addAction(
                android.R.drawable.ic_popup_sync,
                if (com.example.util.Localization.isFarsi()) "تأخیر ۵ دقیقه ⏰" else "Snooze 5 Min ⏰",
                pSnoozeIntent
            )
        }

        builder.addAction(
            android.R.drawable.ic_menu_close_clear_cancel,
            if (com.example.util.Localization.isFarsi()) "پایان پایش" else "Stop Tracking",
            pStopIntent
        )

        return builder.build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "پایش سلامتی فعال ایستاد",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "آمار زنده نشستن و ایستادن دائم در نوار ابزار بالا"
            }
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun formatDuration(totalSeconds: Long): String {
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.launch {
            tickerJob?.cancel()
            unregisterAccelerometer()
        }
    }
}
