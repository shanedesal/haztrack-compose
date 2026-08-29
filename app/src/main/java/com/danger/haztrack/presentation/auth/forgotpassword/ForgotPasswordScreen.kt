package com.danger.haztrack.presentation.auth.forgotpassword

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.danger.haztrack.R
import com.danger.haztrack.presentation.components.AuthTopBar
import com.danger.haztrack.presentation.components.HaztrackPrimaryButton
import com.danger.haztrack.presentation.components.HaztrackTextField

@Composable
fun ForgotPasswordScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ForgotPasswordViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    ForgotPasswordContent(
        uiState = uiState,
        modifier = modifier,
        onNavigateBack = onNavigateBack,
        onEmailChange = viewModel::onEmailChange,
        onSendResetLinkClick = viewModel::onSendResetLinkClick,
    )
}

@Composable
private fun ForgotPasswordContent(
    uiState: ForgotPasswordUiState,
    onNavigateBack: () -> Unit,
    onEmailChange: (String) -> Unit,
    onSendResetLinkClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val focusManager = LocalFocusManager.current

    LaunchedEffect(uiState.isEmailSent) {
        if (uiState.isEmailSent) {
            focusManager.clearFocus()
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = { AuthTopBar(onNavigateBack = onNavigateBack) },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            if (uiState.isEmailSent) {
                Icon(
                    imageVector = Icons.Filled.CheckCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.height(48.dp),
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = stringResource(R.string.forgot_password_success_message),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center,
                )
                Spacer(modifier = Modifier.height(24.dp))
                TextButton(onClick = onNavigateBack) {
                    Text(text = stringResource(R.string.forgot_password_back_to_login))
                }
            } else {
                Text(
                    text = stringResource(R.string.forgot_password_title),
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.forgot_password_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
                Spacer(modifier = Modifier.height(32.dp))

                HaztrackTextField(
                    value = uiState.email,
                    onValueChange = onEmailChange,
                    label = stringResource(R.string.forgot_password_email_label),
                    leadingIcon = Icons.Filled.Email,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Done,
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = { onSendResetLinkClick() },
                    ),
                    enabled = !uiState.isLoading,
                    modifier = Modifier.fillMaxWidth(),
                )

                uiState.errorMessageRes?.let { errorRes ->
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = stringResource(errorRes),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))
                HaztrackPrimaryButton(
                    text = stringResource(R.string.forgot_password_send_button),
                    onClick = onSendResetLinkClick,
                    isLoading = uiState.isLoading,
                    enabled = uiState.isSubmitEnabled,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
