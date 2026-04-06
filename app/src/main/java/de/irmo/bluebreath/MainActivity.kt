package de.irmo.bluebreath

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.content.ContextCompat
import de.irmo.bluebreath.ui.screen.BreathingScreen
import de.irmo.bluebreath.ui.theme.BlueBreathTheme
import de.irmo.bluebreath.data.UserPreferences
import de.irmo.bluebreath.service.BreathingService

class MainActivity : ComponentActivity() {
    companion object {
        const val ACTION_START_ASSIST_TIMER = "de.irmo.bluebreath.action.START_ASSIST_TIMER"
        const val DEFAULT_ASSIST_REPS = 16
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handleLaunchIntent(intent)
        enableEdgeToEdge()
        setContent {
            BlueBreathTheme {
                BreathingScreen()
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleLaunchIntent(intent)
    }

    private fun handleLaunchIntent(intent: Intent?) {
        if (intent?.action != ACTION_START_ASSIST_TIMER) return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) !=
            android.content.pm.PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 1001)
        }

        val preferences = UserPreferences(this)
        val startIntent = Intent(this, BreathingService::class.java).apply {
            action = BreathingService.ACTION_START
            putExtra(BreathingService.EXTRA_REPS, DEFAULT_ASSIST_REPS)
            putExtra(BreathingService.EXTRA_PATTERN, preferences.pattern.name)
            putExtra(BreathingService.EXTRA_INTENSITY, preferences.intensity)
            putExtra(BreathingService.EXTRA_PULSE_DURATION, preferences.pulseDurationMs.toLong())
        }
        ContextCompat.startForegroundService(this, startIntent)
    }
}
