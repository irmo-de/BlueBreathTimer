package de.irmo.bluebreath

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.core.content.ContextCompat
import de.irmo.bluebreath.data.UserPreferences
import de.irmo.bluebreath.service.BreathingService

class StartBreathingActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        startBreathing()
        finish()
    }

    private fun startBreathing() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 1001)
        }

        val preferences = UserPreferences(this)
        val intent = Intent(this, BreathingService::class.java).apply {
            action = BreathingService.ACTION_START
            putExtra(BreathingService.EXTRA_REPS, AppActions.DEFAULT_ASSIST_REPS)
            putExtra(BreathingService.EXTRA_PATTERN, preferences.pattern.name)
            putExtra(BreathingService.EXTRA_INTENSITY, preferences.intensity)
            putExtra(BreathingService.EXTRA_PULSE_DURATION, preferences.pulseDurationMs.toLong())
        }
        ContextCompat.startForegroundService(this, intent)
    }
}
