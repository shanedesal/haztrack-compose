package com.danger.haztrack

import android.content.Intent
import android.net.Uri
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
import kotlinx.coroutines.flow.MutableStateFlow

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val oobCodeFlow = MutableStateFlow<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handleIntent(intent)
        setContent {
            HaztrackTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    HaztrackNavHost(oobCodeFlow = oobCodeFlow)
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        val data = intent?.data ?: return
        val actionData = data.getQueryParameter("link")
            ?.let(Uri::parse)
            ?: data

        if (actionData.getQueryParameter("mode") == "resetPassword") {
            actionData.getQueryParameter("oobCode")?.let { oobCode ->
                oobCodeFlow.value = oobCode
            }
        }
    }
}
