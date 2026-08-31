package com.danger.haztrack.presentation.report

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ReportProblem
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.danger.haztrack.R
import com.danger.haztrack.presentation.components.EmptyStateMessage

/**
 * Placeholder for the hazard-reporting flow, reachable from the docked FAB
 * or the Home dashboard's quick action. The reporting form and submission
 * pipeline are not implemented yet.
 */
@Composable
fun ReportScreen(modifier: Modifier = Modifier) {
    EmptyStateMessage(
        icon = Icons.Filled.ReportProblem,
        title = stringResource(R.string.report_title),
        message = stringResource(R.string.report_coming_soon_message),
        modifier = modifier,
    )
}
