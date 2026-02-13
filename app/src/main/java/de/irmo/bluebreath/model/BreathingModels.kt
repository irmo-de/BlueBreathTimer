package de.irmo.bluebreath.model

enum class BreathingPhase(val displayName: String, val durationSeconds: Int) {
    IDLE("Ready", 0),
    INHALE("Inhale", 4),
    HOLD("Hold", 7),
    EXHALE("Exhale", 8),
    COMPLETE("Complete", 0)
}

enum class VibrationPattern(val displayName: String) {
    GENTLE("Gentle"),
    STANDARD("Standard"),
    STRONG("Strong"),
    PULSE("Pulse")
}

const val CYCLE_DURATION_SECONDS = 4 + 7 + 8
