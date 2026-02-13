package de.irmo.bluebreath.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.core.app.NotificationCompat
import android.app.Service
import de.irmo.bluebreath.MainActivity
import de.irmo.bluebreath.haptics.HapticsManager
import de.irmo.bluebreath.model.BreathingPhase
import de.irmo.bluebreath.model.VibrationPattern
import de.irmo.bluebreath.receiver.SleepDeviceAdminReceiver
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class BreathingService : Service() {

    companion object {
        const val CHANNEL_ID = "breathing_channel"
        const val NOTIFICATION_ID = 1
        const val ACTION_START = "ACTION_START"
        const val ACTION_STOP = "ACTION_STOP"
        const val EXTRA_REPS = "reps"
        const val EXTRA_PATTERN = "pattern"
        const val EXTRA_INTENSITY = "intensity"

        private val _isRunning = MutableStateFlow(false)
        val isRunning: StateFlow<Boolean> = _isRunning

        private val _currentPhase = MutableStateFlow(BreathingPhase.IDLE)
        val currentPhase: StateFlow<BreathingPhase> = _currentPhase

        private val _currentRep = MutableStateFlow(0)
        val currentRep: StateFlow<Int> = _currentRep

        private val _totalReps = MutableStateFlow(0)
        val totalReps: StateFlow<Int> = _totalReps

        private val _phaseProgress = MutableStateFlow(0f)
        val phaseProgress: StateFlow<Float> = _phaseProgress

        private val _phaseTimeRemaining = MutableStateFlow(0)
        val phaseTimeRemaining: StateFlow<Int> = _phaseTimeRemaining
    }

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var wakeLock: PowerManager.WakeLock? = null
    private var hapticsManager: HapticsManager? = null
    private var breathingJob: Job? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vm = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vm.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
        hapticsManager = HapticsManager(vibrator)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                val reps = intent.getIntExtra(EXTRA_REPS, 4)
                val patternName = intent.getStringExtra(EXTRA_PATTERN) ?: VibrationPattern.STANDARD.name
                val intensity = intent.getFloatExtra(EXTRA_INTENSITY, 0.7f)
                startBreathing(reps, VibrationPattern.valueOf(patternName), intensity)
            }
            ACTION_STOP -> {
                stopBreathing()
            }
        }
        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun startBreathing(reps: Int, pattern: VibrationPattern, intensity: Float) {
        val notification = buildNotification("Starting breathing exercise...")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(
                NOTIFICATION_ID, notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }

        acquireWakeLock()

        _totalReps.value = reps
        _isRunning.value = true

        breathingJob = serviceScope.launch {
            for (rep in 1..reps) {
                _currentRep.value = rep

                runPhase(BreathingPhase.INHALE, pattern, intensity)
                runPhase(BreathingPhase.HOLD, pattern, intensity)
                runPhase(BreathingPhase.EXHALE, pattern, intensity)
            }

            // Complete
            _currentPhase.value = BreathingPhase.COMPLETE
            _phaseProgress.value = 1f
            hapticsManager?.cancel()

            // Completion haptic feedback
            hapticsManager?.vibrateCompletion(intensity)

            delay(2000)

            // Lock screen via Device Admin
            lockScreen()

            stopBreathing()
        }
    }

    private suspend fun runPhase(
        phase: BreathingPhase,
        pattern: VibrationPattern,
        intensity: Float
    ) {
        _currentPhase.value = phase
        val durationMs = phase.durationSeconds * 1000L
        val updateInterval = 50L

        // Transition haptic
        hapticsManager?.vibrateTransition(intensity)
        delay(100)

        // Phase vibration pattern
        hapticsManager?.vibrateForPhase(phase, pattern, intensity)

        // Update notification
        updateNotification("Cycle ${_currentRep.value}/${_totalReps.value} - ${phase.displayName}")

        var elapsed = 0L
        while (elapsed < durationMs) {
            _phaseProgress.value = elapsed.toFloat() / durationMs
            _phaseTimeRemaining.value = ((durationMs - elapsed) / 1000).toInt() + 1
            delay(updateInterval)
            elapsed += updateInterval
        }

        _phaseProgress.value = 1f
        _phaseTimeRemaining.value = 0
        hapticsManager?.cancel()
    }

    private fun lockScreen() {
        try {
            val dpm = getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
            val adminComponent = ComponentName(this, SleepDeviceAdminReceiver::class.java)
            if (dpm.isAdminActive(adminComponent)) {
                dpm.lockNow()
            }
        } catch (_: Exception) {
            // Device admin not active, skip
        }
    }

    private fun stopBreathing() {
        breathingJob?.cancel()
        hapticsManager?.cancel()
        _isRunning.value = false
        _currentPhase.value = BreathingPhase.IDLE
        _currentRep.value = 0
        _phaseProgress.value = 0f
        _phaseTimeRemaining.value = 0
        releaseWakeLock()
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun acquireWakeLock() {
        val pm = getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = pm.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            "BlueBreath::BreathingWakeLock"
        ).apply {
            acquire(10 * 60 * 1000L) // Max 10 minutes
        }
    }

    private fun releaseWakeLock() {
        wakeLock?.let {
            if (it.isHeld) it.release()
        }
        wakeLock = null
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Breathing Exercise",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Shows breathing exercise progress"
            setShowBadge(false)
        }
        val nm = getSystemService(NotificationManager::class.java)
        nm.createNotificationChannel(channel)
    }

    private fun buildNotification(text: String): Notification {
        val pendingIntent = PendingIntent.getActivity(
            this, 0,
            Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
            },
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("BlueBreath")
            .setContentText(text)
            .setSmallIcon(android.R.drawable.ic_popup_sync)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setSilent(true)
            .build()
    }

    private fun updateNotification(text: String) {
        val nm = getSystemService(NotificationManager::class.java)
        nm.notify(NOTIFICATION_ID, buildNotification(text))
    }

    override fun onDestroy() {
        stopBreathing()
        serviceScope.cancel()
        super.onDestroy()
    }
}
