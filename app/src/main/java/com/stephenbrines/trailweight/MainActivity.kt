package com.stephenbrines.trailweight

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.stephenbrines.trailweight.ui.OnboardingScreen
import com.stephenbrines.trailweight.ui.TrailWeightNavHost
import com.stephenbrines.trailweight.ui.theme.TrailWeightTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val prefs = getSharedPreferences("trailweight", MODE_PRIVATE)
        val hasSeenOnboarding = prefs.getBoolean("hasSeenOnboarding", false)

        setContent {
            TrailWeightTheme {
                var onboardingDone by remember { mutableStateOf(hasSeenOnboarding) }
                if (onboardingDone) {
                    TrailWeightNavHost()
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
