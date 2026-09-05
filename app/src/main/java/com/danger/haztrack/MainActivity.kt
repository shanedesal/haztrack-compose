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
import androidx.lifecycle.lifecycleScope
import com.danger.haztrack.domain.usecase.auth.AuthUseCases
import com.danger.haztrack.presentation.navigation.HaztrackNavHost
import com.danger.haztrack.presentation.theme.HaztrackTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var authUseCases: AuthUseCases
    private val recoveryEmailFlow = MutableStateFlow<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        handleIntent(intent)
        setContent {
            HaztrackTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    HaztrackNavHost(recoveryEmailFlow = recoveryEmailFlow)
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
        if (data.scheme != "com.danger.haztrack" || data.host != "reset-password") return

        lifecycleScope.launch {
            runCatching {
                authUseCases.establishSessionFromUrl(data.toString())
            }.onSuccess { authUser ->
                recoveryEmailFlow.value = authUser.email.orEmpty()
            }.onFailure { throwable ->
                Timber.w(throwable, "Failed to establish session from password recovery link")
            }
        }
    }
}
