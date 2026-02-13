package de.irmo.bluebreath

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import de.irmo.bluebreath.ui.screen.BreathingScreen
import de.irmo.bluebreath.ui.theme.BlueBreathTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BlueBreathTheme {
                BreathingScreen()
            }
        }
    }
}
