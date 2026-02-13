package de.irmo.bluebreath.ui.screen

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.LaunchedEffect
import de.irmo.bluebreath.data.UserPreferences
import de.irmo.bluebreath.model.BreathingPhase
import de.irmo.bluebreath.model.CYCLE_DURATION_SECONDS
import de.irmo.bluebreath.model.VibrationPattern
import de.irmo.bluebreath.receiver.SleepDeviceAdminReceiver
import de.irmo.bluebreath.service.BreathingService
import de.irmo.bluebreath.ui.components.BreathingCircle
import de.irmo.bluebreath.ui.components.FingerDial

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BreathingScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current

    // Service state
    val isRunning by BreathingService.isRunning.collectAsState()
    val currentPhase by BreathingService.currentPhase.collectAsState()
    val currentRep by BreathingService.currentRep.collectAsState()
    val totalReps by BreathingService.totalReps.collectAsState()
    val phaseProgress by BreathingService.phaseProgress.collectAsState()
    val phaseTimeRemaining by BreathingService.phaseTimeRemaining.collectAsState()

    // User settings
    val userPreferences = remember { UserPreferences(context) }
    var selectedReps by rememberSaveable { mutableIntStateOf(userPreferences.reps) }
    var selectedPattern by rememberSaveable { mutableStateOf(userPreferences.pattern) }
    var vibrationIntensity by rememberSaveable { mutableFloatStateOf(userPreferences.intensity) }
    var pulseDurationMs by rememberSaveable { mutableFloatStateOf(userPreferences.pulseDurationMs) }
    var showSettings by rememberSaveable { mutableStateOf(false) }

    // Save changes to preferences
    LaunchedEffect(selectedReps) { userPreferences.reps = selectedReps }
    LaunchedEffect(selectedPattern) { userPreferences.pattern = selectedPattern }
    LaunchedEffect(vibrationIntensity) { userPreferences.intensity = vibrationIntensity }
    LaunchedEffect(pulseDurationMs) { userPreferences.pulseDurationMs = pulseDurationMs }

    // Notification permission (API 33+)
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* proceed regardless */ }

    // Device Admin
    val deviceAdminComponent = ComponentName(context, SleepDeviceAdminReceiver::class.java)
    val dpm = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
    var isDeviceAdminActive by remember { mutableStateOf(dpm.isAdminActive(deviceAdminComponent)) }

    val deviceAdminLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        isDeviceAdminActive = dpm.isAdminActive(deviceAdminComponent)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("BlueBreath", fontWeight = FontWeight.Bold)
                        Text(
                            "4-7-8 Breathing Technique",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    if (!isRunning) {
                        IconButton(onClick = { showSettings = !showSettings }) {
                            Icon(Icons.Default.Settings, "Settings")
                        }
                    }
                }
            )
        },
        modifier = modifier
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            // Breathing circle
            BreathingCircle(
                phase = if (isRunning) currentPhase else BreathingPhase.IDLE,
                progress = phaseProgress,
                timeRemaining = phaseTimeRemaining,
                currentRep = currentRep,
                totalReps = totalReps
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Device Admin banner
            if (!isDeviceAdminActive && !isRunning) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Auto-sleep",
                                style = MaterialTheme.typography.titleSmall
                            )
                            Text(
                                "Enable to lock screen after exercise",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        FilledTonalButton(onClick = {
                            val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN).apply {
                                putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, deviceAdminComponent)
                                putExtra(
                                    DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                                    "BlueBreath needs this permission to lock the screen after your breathing exercise completes."
                                )
                            }
                            deviceAdminLauncher.launch(intent)
                        }) {
                            Text("Enable")
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Configuration section (hidden when running)
            AnimatedVisibility(
                visible = !isRunning,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    // Finger dial
                    FingerDial(
                        value = selectedReps,
                        onValueChange = { selectedReps = it }
                    )

                    // Total time display
                    val totalSeconds = selectedReps * CYCLE_DURATION_SECONDS
                    val minutes = totalSeconds / 60
                    val seconds = totalSeconds % 60
                    Text(
                        text = "Total time: ${if (minutes > 0) "${minutes}m " else ""}${seconds}s",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 8.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Settings section
                    AnimatedVisibility(visible = showSettings) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp)
                        ) {
                            Column(modifier = Modifier.padding(20.dp)) {
                                Text(
                                    "Vibration Pattern",
                                    style = MaterialTheme.typography.titleSmall
                                )
                                Spacer(modifier = Modifier.height(8.dp))

                                @OptIn(ExperimentalLayoutApi::class)
                                FlowRow(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    VibrationPattern.entries.forEach { pattern ->
                                        FilterChip(
                                            selected = selectedPattern == pattern,
                                            onClick = { selectedPattern = pattern },
                                            label = { Text(pattern.displayName) }
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(20.dp))

                                Text(
                                    "Vibration Intensity: ${(vibrationIntensity * 100).toInt()}%",
                                    style = MaterialTheme.typography.titleSmall
                                )
                                Spacer(modifier = Modifier.height(4.dp))

                                Slider(
                                    value = vibrationIntensity,
                                    onValueChange = { vibrationIntensity = it },
                                    valueRange = 0.1f..1.0f,
                                    steps = 8
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Light", style = MaterialTheme.typography.labelSmall)
                                    Text("Strong", style = MaterialTheme.typography.labelSmall)
                                }

                                Spacer(modifier = Modifier.height(20.dp))

                                Text(
                                    "Pulse Duration: ${pulseDurationMs.toInt()}ms",
                                    style = MaterialTheme.typography.titleSmall
                                )
                                Spacer(modifier = Modifier.height(4.dp))

                                Slider(
                                    value = pulseDurationMs,
                                    onValueChange = { pulseDurationMs = it },
                                    valueRange = 30f..200f,
                                    steps = 16
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Short", style = MaterialTheme.typography.labelSmall)
                                    Text("Long", style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                }
            }

            // Start / Stop button
            if (isRunning) {
                FilledTonalButton(
                    onClick = {
                        val intent = Intent(context, BreathingService::class.java).apply {
                            action = BreathingService.ACTION_STOP
                        }
                        context.startService(intent)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 48.dp)
                        .height(56.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer
                    )
                ) {
                    Icon(Icons.Default.Close, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Stop", style = MaterialTheme.typography.titleMedium)
                }
            } else {
                Button(
                    onClick = {
                        // Request notification permission on API 33+
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            notificationPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                        }

                        val intent = Intent(context, BreathingService::class.java).apply {
                            action = BreathingService.ACTION_START
                            putExtra(BreathingService.EXTRA_REPS, selectedReps)
                            putExtra(BreathingService.EXTRA_PATTERN, selectedPattern.name)
                            putExtra(BreathingService.EXTRA_INTENSITY, vibrationIntensity)
                            putExtra(BreathingService.EXTRA_PULSE_DURATION, pulseDurationMs.toLong())
                        }
                        context.startForegroundService(intent)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 48.dp)
                        .height(56.dp)
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Start Breathing", style = MaterialTheme.typography.titleMedium)
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
