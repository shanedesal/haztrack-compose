package com.danger.haztrack.presentation.myreports

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.danger.haztrack.R
import com.danger.haztrack.presentation.components.EmptyStateMessage

/**
 * Placeholder for the list of hazard reports the signed-in user has made.
 * No report data source exists yet.
 */
@Composable
fun MyReportsScreen(modifier: Modifier = Modifier) {
    EmptyStateMessage(
        icon = Icons.AutoMirrored.Filled.Assignment,
        title = stringResource(R.string.my_reports_title),
        message = stringResource(R.string.my_reports_empty_message),
        modifier = modifier,
    )
}
