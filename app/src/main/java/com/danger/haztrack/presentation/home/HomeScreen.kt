package com.danger.haztrack.presentation.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.ReportProblem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.danger.haztrack.R
import com.danger.haztrack.presentation.components.QuickActionCard

@Composable
fun HomeScreen(
    onNavigateToReport: () -> Unit,
    onNavigateToMyReports: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    HomeContent(
        uiState = uiState,
        modifier = modifier,
        onNavigateToReport = onNavigateToReport,
        onNavigateToMyReports = onNavigateToMyReports,
    )
}

@Composable
private fun HomeContent(
    uiState: HomeUiState,
    onNavigateToReport: () -> Unit,
    onNavigateToMyReports: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val greetingName = uiState.user?.displayName?.takeIf { it.isNotBlank() }
        ?: uiState.user?.email
        ?: stringResource(R.string.home_greeting_fallback_name)

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp),
    ) {
        Spacer(modifier = Modifier.height(32.dp))
        Text(
            text = stringResource(R.string.home_greeting_title, greetingName),
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.home_greeting_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(modifier = Modifier.height(36.dp))
        Text(
            text = stringResource(R.string.home_quick_actions_title),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Spacer(modifier = Modifier.height(12.dp))

        QuickActionCard(
            icon = Icons.Filled.ReportProblem,
            title = stringResource(R.string.home_action_report_title),
            subtitle = stringResource(R.string.home_action_report_subtitle),
            onClick = onNavigateToReport,
        )
        Spacer(modifier = Modifier.height(12.dp))
        QuickActionCard(
            icon = Icons.AutoMirrored.Filled.Assignment,
            title = stringResource(R.string.home_action_my_reports_title),
            subtitle = stringResource(R.string.home_action_my_reports_subtitle),
            onClick = onNavigateToMyReports,
        )
        Spacer(modifier = Modifier.height(32.dp))
    }
}
