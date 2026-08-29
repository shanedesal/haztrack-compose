package com.danger.haztrack.presentation.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.danger.haztrack.R
import com.danger.haztrack.presentation.auth.common.GoogleAuthClient
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(
    onSignedOut: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val googleAuthClient = remember { GoogleAuthClient() }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                HomeEvent.NavigateToLogin -> onSignedOut()
            }
        }
    }

    HomeContent(
        uiState = uiState,
        modifier = modifier,
        onSignOutClick = {
            coroutineScope.launch {
                runCatching { googleAuthClient.signOut(context) }
                viewModel.onSignOutClick()
            }
        },
    )
}

@Composable
private fun HomeContent(
    uiState: HomeUiState,
    onSignOutClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(modifier = modifier) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.height(96.dp))
            Text(
                text = stringResource(R.string.home_welcome_title),
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(
                    R.string.home_welcome_message,
                    uiState.user?.email ?: uiState.user?.displayName.orEmpty(),
                ),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(40.dp))
            OutlinedButton(
                onClick = onSignOutClick,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                    contentDescription = null,
                )
                Text(
                    text = stringResource(R.string.home_sign_out_button),
                    modifier = Modifier.padding(start = 8.dp),
                )
            }
        }
    }
}
