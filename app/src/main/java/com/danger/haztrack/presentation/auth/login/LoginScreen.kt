package com.danger.haztrack.presentation.auth.login

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.danger.haztrack.R
import com.danger.haztrack.presentation.auth.common.GoogleAuthClient
import com.danger.haztrack.presentation.components.AuthDivider
import com.danger.haztrack.presentation.components.GoogleSignInButton
import com.danger.haztrack.presentation.components.HaztrackPasswordField
import com.danger.haztrack.presentation.components.HaztrackPrimaryButton
import com.danger.haztrack.presentation.components.HaztrackTextField
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    onNavigateToRegister: () -> Unit,
    onNavigateToForgotPassword: () -> Unit,
    onSignedIn: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: LoginViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val googleAuthClient = remember { GoogleAuthClient() }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                LoginEvent.NavigateToHome -> onSignedIn()
            }
        }
    }

    LoginContent(
        uiState = uiState,
        modifier = modifier,
        onEmailChange = viewModel::onEmailChange,
        onPasswordChange = viewModel::onPasswordChange,
        onPasswordVisibilityToggle = viewModel::onPasswordVisibilityToggle,
        onSignInClick = viewModel::onSignInClick,
        onForgotPasswordClick = onNavigateToForgotPassword,
        onRegisterClick = onNavigateToRegister,
        onGoogleSignInClick = {
            coroutineScope.launch {
                viewModel.onGoogleSignInStarted()
                runCatching { googleAuthClient.requestIdToken(context) }
                    .onSuccess(viewModel::onGoogleIdTokenReceived)
                    .onFailure(viewModel::onGoogleSignInFailed)
            }
        },
    )
}

@Composable
private fun LoginContent(
    uiState: LoginUiState,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onPasswordVisibilityToggle: () -> Unit,
    onSignInClick: () -> Unit,
    onForgotPasswordClick: () -> Unit,
    onRegisterClick: () -> Unit,
    onGoogleSignInClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val focusManager = LocalFocusManager.current

    Scaffold(modifier = modifier) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.height(56.dp))
            Image(
                painter = painterResource(R.drawable.ic_app_logo),
                contentDescription = stringResource(R.string.app_name),
                modifier = Modifier.height(72.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.login_title),
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.login_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(32.dp))

            HaztrackTextField(
                value = uiState.email,
                onValueChange = onEmailChange,
                label = stringResource(R.string.login_email_label),
                leadingIcon = Icons.Filled.Email,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next,
                ),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) },
                ),
                enabled = !uiState.isLoading,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(modifier = Modifier.height(16.dp))
            HaztrackPasswordField(
                value = uiState.password,
                onValueChange = onPasswordChange,
                label = stringResource(R.string.login_password_label),
                isPasswordVisible = uiState.isPasswordVisible,
                onVisibilityToggle = onPasswordVisibilityToggle,
                keyboardActions = KeyboardActions(onDone = { onSignInClick() }),
                enabled = !uiState.isLoading,
                modifier = Modifier.fillMaxWidth(),
            )

            TextButton(
                onClick = onForgotPasswordClick,
                modifier = Modifier.align(Alignment.End),
            ) {
                Text(text = stringResource(R.string.login_forgot_password))
            }

            uiState.errorMessageRes?.let { errorRes ->
                Text(
                    text = stringResource(errorRes),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(bottom = 8.dp),
                )
            }

            HaztrackPrimaryButton(
                text = stringResource(R.string.login_sign_in_button),
                onClick = onSignInClick,
                isLoading = uiState.isLoading,
                enabled = uiState.isSignInEnabled,
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(modifier = Modifier.height(24.dp))
            AuthDivider(modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(24.dp))

            GoogleSignInButton(
                onClick = onGoogleSignInClick,
                isLoading = uiState.isGoogleSignInLoading,
                enabled = !uiState.isLoading && !uiState.isGoogleSignInLoading,
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(modifier = Modifier.height(32.dp))
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(R.string.login_no_account_prompt),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                TextButton(onClick = onRegisterClick) {
                    Text(text = stringResource(R.string.login_sign_up_link))
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
