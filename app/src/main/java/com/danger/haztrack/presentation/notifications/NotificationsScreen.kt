package com.danger.haztrack.presentation.notifications

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.danger.haztrack.R
import com.danger.haztrack.presentation.components.EmptyStateMessage

/**
 * Placeholder for hazard alerts and app notifications. No notification
 * pipeline exists yet.
 */
@Composable
fun NotificationsScreen(modifier: Modifier = Modifier) {
    EmptyStateMessage(
        icon = Icons.Filled.Notifications,
        title = stringResource(R.string.notifications_title),
        message = stringResource(R.string.notifications_empty_message),
        modifier = modifier,
    )
}
