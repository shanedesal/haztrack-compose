package com.danger.haztrack.presentation.auth.resetpassword

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.LockReset
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.danger.haztrack.R
import com.danger.haztrack.presentation.components.AuthTopBar
import com.danger.haztrack.presentation.components.HaztrackPasswordField
import com.danger.haztrack.presentation.components.HaztrackPrimaryButton
import com.danger.haztrack.presentation.components.IconBadge

@Composable
fun ResetPasswordScreen(
    onNavigateBack: () -> Unit,
    onResetComplete: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ResetPasswordViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    ResetPasswordContent(
        uiState = uiState,
        onNavigateBack = onNavigateBack,
        onNewPasswordChange = viewModel::onNewPasswordChange,
        onPasswordVisibilityToggle = viewModel::onPasswordVisibilityToggle,
        onConfirmResetClick = viewModel::onConfirmResetClick,
        onResetComplete = onResetComplete,
        modifier = modifier,
    )
}

@Composable
private fun ResetPasswordContent(
    uiState: ResetPasswordUiState,
    onNavigateBack: () -> Unit,
    onNewPasswordChange: (String) -> Unit,
    onPasswordVisibilityToggle: () -> Unit,
    onConfirmResetClick: () -> Unit,
    onResetComplete: () -> Unit,
    modifier: Modifier = Modifier
){
    val focusManager = LocalFocusManager.current

    LaunchedEffect(uiState.isResetSuccessful) {
        if(uiState.isResetSuccessful){
            focusManager.clearFocus()
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = { AuthTopBar(onNavigateBack = onNavigateBack) },
    ){ paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ){
            Spacer(modifier = Modifier.height(16.dp))

            when {
                uiState.isVerifying -> {
                    Spacer(modifier = Modifier.height(48.dp))
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = stringResource(R.string.reset_password_verifying),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                uiState.isResetSuccessful -> {
                    IconBadge(
                        icon = Icons.Filled.CheckCircle,
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.primary,
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = stringResource(R.string.reset_password_success_title),
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onBackground,
                        textAlign = TextAlign.Center,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.reset_password_success_message),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(32.dp))
                    TextButton(onClick = onResetComplete){
                        Text(text = stringResource(R.string.reset_password_back_to_login))
                    }
                }

                uiState.isCodeValid -> {
                    IconBadge(icon = Icons.Filled.LockReset)
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = stringResource(R.string.reset_password_title),
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onBackground,
                        textAlign = TextAlign.Center,
                    )
                    uiState.email?.let { email ->
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = stringResource(R.string.reset_password_subtitle, email),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                    Spacer(modifier = Modifier.height(32.dp))

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.extraLarge,
                        color = MaterialTheme.colorScheme.surfaceContainer,
                    ) {
                        Column(modifier = Modifier.padding(24.dp)) {
                            HaztrackPasswordField(
                                value = uiState.newPassword,
                                onValueChange = onNewPasswordChange,
                                label = stringResource(R.string.reset_password_new_password_label),
                                isPasswordVisible = uiState.isPasswordVisible,
                                onVisibilityToggle = onPasswordVisibilityToggle,
                                keyboardActions = KeyboardActions(onDone = { onConfirmResetClick() }),
                                enabled = !uiState.isLoading,
                                modifier = Modifier.fillMaxWidth(),
                            )

                            uiState.errorMessageRes?.let { errorRes ->
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = stringResource(errorRes),
                                    color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                            Spacer(modifier = Modifier.height(20.dp))
                            HaztrackPrimaryButton(
                                text = stringResource(R.string.reset_password_confirm_button),
                                onClick = onConfirmResetClick,
                                isLoading = uiState.isLoading,
                                enabled = uiState.isSubmitEnabled,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }

                else -> {
                    IconBadge(
                        icon = Icons.Filled.ErrorOutline,
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.error,
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = uiState.errorMessageRes?.let { stringResource(it)}
                            ?: stringResource(R.string.reset_password_invalid_link),
                        color = MaterialTheme.colorScheme.onBackground,
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    TextButton(onClick = onNavigateBack) {
                        Text(text = stringResource(R.string.reset_password_back_to_login))
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
