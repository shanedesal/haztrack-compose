package com.danger.haztrack.presentation.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.danger.haztrack.R

@Composable
fun GoogleSignInButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
    enabled: Boolean = true,
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled && !isLoading,
        border = ButtonDefaults.outlinedButtonBorder(enabled = enabled && !isLoading).copy(
            brush = SolidColor(MaterialTheme.colorScheme.outline),
        ),
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                color = MaterialTheme.colorScheme.primary,
                strokeWidth = 2.dp,
            )
        } else {
            Image(
                painter = painterResource(R.drawable.ic_google),
                contentDescription = stringResource(R.string.auth_google_logo_content_description),
                modifier = Modifier.size(20.dp),
            )
            Spacer(modifier = Modifier.size(12.dp))
            Text(
                text = stringResource(R.string.auth_continue_with_google),
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}
