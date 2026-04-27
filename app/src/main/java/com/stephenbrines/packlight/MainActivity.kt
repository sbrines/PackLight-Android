package com.stephenbrines.packlight

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.stephenbrines.packlight.ui.OnboardingScreen
import com.stephenbrines.packlight.ui.PackLightNavHost
import com.stephenbrines.packlight.ui.theme.PackLightTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val prefs = getSharedPreferences("packlight", MODE_PRIVATE)
        val hasSeenOnboarding = prefs.getBoolean("hasSeenOnboarding", false)

        setContent {
            PackLightTheme {
                var onboardingDone by remember { mutableStateOf(hasSeenOnboarding) }
                if (onboardingDone) {
                    PackLightNavHost()
                } else {
                    OnboardingScreen {
                        prefs.edit().putBoolean("hasSeenOnboarding", true).apply()
                        onboardingDone = true
                    }
                }
            }
        }
    }
}
