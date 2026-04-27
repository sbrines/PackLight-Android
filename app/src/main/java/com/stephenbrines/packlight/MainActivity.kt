package com.stephenbrines.packlight

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.stephenbrines.packlight.ui.PackLightNavHost
import com.stephenbrines.packlight.ui.theme.PackLightTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PackLightTheme {
                PackLightNavHost()
            }
        }
    }
}
