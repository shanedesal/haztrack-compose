package com.danger.haztrack

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.danger.haztrack.presentation.navigation.HaztrackNavHost
import com.danger.haztrack.presentation.theme.HaztrackTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HaztrackTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    HaztrackNavHost()
                }
            }
        }
    }
}
