package de.irmo.bluebreath.haptics

import android.media.AudioAttributes
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import de.irmo.bluebreath.model.BreathingPhase
import de.irmo.bluebreath.model.VibrationPattern

private const val TAG = "HapticsManager"

class HapticsManager(private val vibrator: Vibrator) {

    // usage=USAGE_ALARM to bypass "Touch feedback" system toggle
    private val audioAttributes = AudioAttributes.Builder()
        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
        .setUsage(AudioAttributes.USAGE_ALARM)
        .build()

    fun vibrateForPhase(
        phase: BreathingPhase,
        pattern: VibrationPattern,
        intensity: Float,
        pulseDurationMs: Long = 80L
    ) {
        if (!vibrator.hasVibrator()) return
        // No explicit cancel() here — vibrate() implicitly replaces any ongoing vibration.
        // Calling cancel() right before vibrate() can race and kill the new vibration on some devices.

        when (pattern) {
            VibrationPattern.GENTLE -> vibrateGentle(phase, intensity)
            VibrationPattern.STANDARD -> vibrateStandard(phase, intensity)
            VibrationPattern.STRONG -> vibrateStrong(phase, intensity)
            VibrationPattern.PULSE -> vibratePulse(phase, intensity)
            VibrationPattern.SIMPLE_1 -> vibrateSimple1(phase, intensity, pulseDurationMs)
            VibrationPattern.SIMPLE_2 -> vibrateSimple2(phase, intensity, pulseDurationMs)
        }
    }

    fun vibrateTransition(intensity: Float) {
        if (!vibrator.hasVibrator()) return
        val amplitude = (200 * intensity).toInt().coerceIn(1, 255)
        if (vibrator.hasAmplitudeControl()) {
            vibrator.vibrate(VibrationEffect.createOneShot(60, amplitude), audioAttributes)
        } else {
            vibrator.vibrate(VibrationEffect.createOneShot(60, VibrationEffect.DEFAULT_AMPLITUDE), audioAttributes)
        }
    }

    fun vibrateCompletion(intensity: Float) {
        if (!vibrator.hasVibrator()) return
        val amp = (220 * intensity).toInt().coerceIn(1, 255)
        val timings = longArrayOf(0, 100, 100, 100, 100, 200)
        val amplitudes = intArrayOf(0, amp, 0, amp, 0, amp)
        vibrator.vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1), audioAttributes)
    }

    fun cancel() {
        vibrator.cancel()
    }

    // --- Gentle: soft ticks with low amplitude ---

    private fun vibrateGentle(phase: BreathingPhase, intensity: Float) {
        val base = (80 * intensity).toInt().coerceIn(1, 255)
        when (phase) {
            BreathingPhase.INHALE -> {
                // Rising soft ticks over 4 seconds
                val steps = 16
                val timings = LongArray(steps * 2)
                val amplitudes = IntArray(steps * 2)
                for (i in 0 until steps) {
                    timings[i * 2] = 80
                    timings[i * 2 + 1] = 170
                    amplitudes[i * 2] = ((base * (i + 1)) / steps).coerceIn(1, 255)
                    amplitudes[i * 2 + 1] = 0
                }
                vibrateWaveform(timings, amplitudes)
            }
            BreathingPhase.HOLD -> {
                // Subtle steady ticks over 7 seconds
                val steps = 14
                val timings = LongArray(steps * 2)
                val amplitudes = IntArray(steps * 2)
                for (i in 0 until steps) {
                    timings[i * 2] = 50
                    timings[i * 2 + 1] = 450
                    amplitudes[i * 2] = (base * 0.3f).toInt().coerceIn(1, 255)
                    amplitudes[i * 2 + 1] = 0
                }
                vibrateWaveform(timings, amplitudes)
            }
            BreathingPhase.EXHALE -> {
                // Fading soft ticks over 8 seconds
                val steps = 16
                val timings = LongArray(steps * 2)
                val amplitudes = IntArray(steps * 2)
                for (i in 0 until steps) {
                    timings[i * 2] = 80
                    timings[i * 2 + 1] = 420
                    amplitudes[i * 2] = ((base * (steps - i)) / steps).coerceIn(0, 255)
                    amplitudes[i * 2 + 1] = 0
                }
                // End at zero
                amplitudes[amplitudes.lastIndex] = 0
                amplitudes[amplitudes.lastIndex - 1] = 0
                vibrateWaveform(timings, amplitudes)
            }
            else -> {}
        }
    }

    // --- Standard: smooth ramp with medium amplitude ---

    private fun vibrateStandard(phase: BreathingPhase, intensity: Float) {
        val base = (150 * intensity).toInt().coerceIn(1, 255)
        when (phase) {
            BreathingPhase.INHALE -> {
                // Smooth ramp up over 4 seconds
                val steps = 20
                val timings = LongArray(steps) { 200L }
                val amplitudes = IntArray(steps) { i ->
                    ((base * (i + 1)) / steps).coerceIn(1, 255)
                }
                vibrateWaveform(timings, amplitudes)
            }
            BreathingPhase.HOLD -> {
                // Steady medium vibration over 7 seconds
                val timings = longArrayOf(7000L)
                val amplitudes = intArrayOf((base * 0.4f).toInt().coerceIn(1, 255))
                vibrateWaveform(timings, amplitudes)
            }
            BreathingPhase.EXHALE -> {
                // Smooth ramp down over 8 seconds
                val steps = 20
                val timings = LongArray(steps) { 400L }
                val amplitudes = IntArray(steps) { i ->
                    ((base * (steps - i)) / steps).coerceIn(0, 255)
                }
                amplitudes[amplitudes.lastIndex] = 0
                vibrateWaveform(timings, amplitudes)
            }
            else -> {}
        }
    }

    // --- Strong: high amplitude with thuds ---

    private fun vibrateStrong(phase: BreathingPhase, intensity: Float) {
        val base = (255 * intensity).toInt().coerceIn(1, 255)
        when (phase) {
            BreathingPhase.INHALE -> {
                // Strong ascending over 4 seconds
                val steps = 16
                val timings = LongArray(steps) { 250L }
                val amplitudes = IntArray(steps) { i ->
                    ((base * (i + 1)) / steps).coerceIn(1, 255)
                }
                vibrateWaveform(timings, amplitudes)
            }
            BreathingPhase.HOLD -> {
                // Strong steady over 7 seconds
                val timings = longArrayOf(7000L)
                val amplitudes = intArrayOf(base)
                vibrateWaveform(timings, amplitudes)
            }
            BreathingPhase.EXHALE -> {
                // Strong descending over 8 seconds
                val steps = 16
                val timings = LongArray(steps) { 500L }
                val amplitudes = IntArray(steps) { i ->
                    ((base * (steps - i)) / steps).coerceIn(0, 255)
                }
                amplitudes[amplitudes.lastIndex] = 0
                vibrateWaveform(timings, amplitudes)
            }
            else -> {}
        }
    }

    // --- Pulse: rhythmic on/off pattern ---

    private fun vibratePulse(phase: BreathingPhase, intensity: Float) {
        val base = (200 * intensity).toInt().coerceIn(1, 255)
        when (phase) {
            BreathingPhase.INHALE -> {
                // Accelerating pulses over 4 seconds
                val pulses = 8
                val timings = LongArray(pulses * 2)
                val amplitudes = IntArray(pulses * 2)
                for (i in 0 until pulses) {
                    val onDuration = (400 - i * 30).toLong().coerceAtLeast(100)
                    val offDuration = (100 - i * 5).toLong().coerceAtLeast(50)
                    timings[i * 2] = onDuration
                    timings[i * 2 + 1] = offDuration
                    amplitudes[i * 2] = ((base * (i + 1)) / pulses).coerceIn(1, 255)
                    amplitudes[i * 2 + 1] = 0
                }
                vibrateWaveform(timings, amplitudes)
            }
            BreathingPhase.HOLD -> {
                // Steady pulse over 7 seconds
                val pulses = 7
                val timings = LongArray(pulses * 2)
                val amplitudes = IntArray(pulses * 2)
                for (i in 0 until pulses) {
                    timings[i * 2] = 500L
                    timings[i * 2 + 1] = 500L
                    amplitudes[i * 2] = (base * 0.5f).toInt().coerceIn(1, 255)
                    amplitudes[i * 2 + 1] = 0
                }
                vibrateWaveform(timings, amplitudes)
            }
            BreathingPhase.EXHALE -> {
                // Decelerating pulses over 8 seconds
                val pulses = 8
                val timings = LongArray(pulses * 2)
                val amplitudes = IntArray(pulses * 2)
                for (i in 0 until pulses) {
                    timings[i * 2] = (200 + i * 50).toLong()
                    timings[i * 2 + 1] = (200 + i * 50).toLong()
                    amplitudes[i * 2] = ((base * (pulses - i)) / pulses).coerceIn(0, 255)
                    amplitudes[i * 2 + 1] = 0
                }
                amplitudes[amplitudes.lastIndex - 1] = 0
                vibrateWaveform(timings, amplitudes)
            }
            else -> {}
        }
    }

    // --- Simple I: inhale=1 buzz, hold=2 buzzes, exhale=3 buzzes ---

    private fun vibrateSimple1(phase: BreathingPhase, intensity: Float, pulseDurationMs: Long) {
        val amp = (180 * intensity).toInt().coerceIn(1, 255)
        val gap = 150L
        when (phase) {
            BreathingPhase.INHALE -> {
                vibrateShortBuzzes(1, amp, pulseDurationMs, gap)
            }
            BreathingPhase.HOLD -> {
                vibrateShortBuzzes(2, amp, pulseDurationMs, gap)
            }
            BreathingPhase.EXHALE -> {
                vibrateShortBuzzes(3, amp, pulseDurationMs, gap)
            }
            else -> {}
        }
    }

    // --- Simple II: inhale=1 buzz, hold=1 buzz, exhale=2 buzzes ---

    private fun vibrateSimple2(phase: BreathingPhase, intensity: Float, pulseDurationMs: Long) {
        val amp = (180 * intensity).toInt().coerceIn(1, 255)
        val gap = 150L
        when (phase) {
            BreathingPhase.INHALE -> {
                vibrateShortBuzzes(1, amp, pulseDurationMs, gap)
            }
            BreathingPhase.HOLD -> {
                vibrateShortBuzzes(1, amp, pulseDurationMs, gap)
            }
            BreathingPhase.EXHALE -> {
                vibrateShortBuzzes(2, amp, pulseDurationMs, gap)
            }
            else -> {}
        }
    }

    private fun vibrateShortBuzzes(count: Int, amplitude: Int, durationMs: Long, gapMs: Long) {
        if (count <= 0) return
        // Allow shorter durations now that we use USAGE_ALARM, but keep a tiny safety floor
        val effectiveDuration = durationMs.coerceAtLeast(20L)
        // Respect the requested amplitude (intensity) completely
        val effectiveAmplitude = amplitude.coerceIn(1, 255)
        Log.d(TAG, "vibrateShortBuzzes: count=$count, amp=$effectiveAmplitude, duration=${effectiveDuration}ms")

        if (count == 1) {
            Log.d(TAG, "Firing single buzz as createOneShot (${effectiveDuration}ms, amp=$effectiveAmplitude)")
            vibrator.vibrate(VibrationEffect.createOneShot(effectiveDuration, effectiveAmplitude), audioAttributes)
            return
        }

        // Always use waveform — createOneShot is unreliable for short durations on some devices
        val timings = LongArray(count * 2)
        val amplitudes = IntArray(count * 2)
        for (i in 0 until count) {
            timings[i * 2] = effectiveDuration
            timings[i * 2 + 1] = gapMs
            amplitudes[i * 2] = effectiveAmplitude
            amplitudes[i * 2 + 1] = 0
        }
        vibrateWaveform(timings, amplitudes)
    }

    private fun vibrateWaveform(timings: LongArray, amplitudes: IntArray) {
        vibrator.vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1), audioAttributes)
    }
}
