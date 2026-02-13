package de.irmo.bluebreath.data

import android.content.Context
import android.content.SharedPreferences
import de.irmo.bluebreath.model.VibrationPattern

class UserPreferences(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)

    var reps: Int
        get() = prefs.getInt(KEY_REPS, 4)
        set(value) = prefs.edit().putInt(KEY_REPS, value).apply()

    var pattern: VibrationPattern
        get() {
            val name = prefs.getString(KEY_PATTERN, VibrationPattern.STANDARD.name) ?: VibrationPattern.STANDARD.name
            return try {
                VibrationPattern.valueOf(name)
            } catch (e: IllegalArgumentException) {
                VibrationPattern.STANDARD
            }
        }
        set(value) = prefs.edit().putString(KEY_PATTERN, value.name).apply()

    var intensity: Float
        get() = prefs.getFloat(KEY_INTENSITY, 0.7f)
        set(value) = prefs.edit().putFloat(KEY_INTENSITY, value).apply()

    var pulseDurationMs: Float
        get() = prefs.getFloat(KEY_PULSE_DURATION, 80f)
        set(value) = prefs.edit().putFloat(KEY_PULSE_DURATION, value).apply()

    companion object {
        private const val KEY_REPS = "reps"
        private const val KEY_PATTERN = "pattern"
        private const val KEY_INTENSITY = "intensity"
        private const val KEY_PULSE_DURATION = "pulse_duration"
    }
}
