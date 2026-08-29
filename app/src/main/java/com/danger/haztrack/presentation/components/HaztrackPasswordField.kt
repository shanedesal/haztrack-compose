package com.danger.haztrack.presentation.components

import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.input.ImeAction
import com.danger.haztrack.R

@Composable
fun HaztrackPasswordField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    isPasswordVisible: Boolean,
    onVisibilityToggle: () -> Unit,
    modifier: Modifier = Modifier,
    imeAction: ImeAction = ImeAction.Done,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    supportingText: String? = null,
    isError: Boolean = false,
    enabled: Boolean = true,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        label = { Text(text = label) },
        leadingIcon = { Icon(imageVector = Icons.Filled.Lock, contentDescription = null) },
        trailingIcon = {
            IconButton(onClick = onVisibilityToggle) {
                Icon(
                    imageVector = if (isPasswordVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                    contentDescription = stringResource(R.string.auth_toggle_password_visibility),
                )
            }
        },
        supportingText = supportingText?.let { text -> { Text(text = text) } },
        isError = isError,
        enabled = enabled,
        singleLine = true,
        visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = imeAction),
        keyboardActions = keyboardActions,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
        ),
    )
}
