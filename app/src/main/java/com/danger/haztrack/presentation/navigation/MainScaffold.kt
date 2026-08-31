package com.danger.haztrack.presentation.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.danger.haztrack.R

private val BarHeight = 64.dp
private val BarShape = RoundedCornerShape(32.dp)
private val FabSize = 60.dp
private val FabRingSize = 76.dp

/**
 * Shared shell for the 5 post-login tabs (Home, My Reports, Notifications,
 * Settings + the Report action). Provides a floating rounded bottom bar with
 * a "+" FAB embedded into its top edge (overlapping, with a ring gap) rather
 * than floating separately above it. Screens render their content through
 * [content], receiving the inner padding needed to avoid the bar.
 */
@Composable
fun MainScaffold(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    content: @Composable (PaddingValues) -> Unit,
) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            HaztrackBottomBar(
                currentRoute = currentRoute,
                onNavigateTab = { route -> navController.navigateToTab(route) },
                onReportClick = { navController.navigateToTab(HaztrackDestination.Report.route) },
            )
        },
    ) { paddingValues ->
        content(paddingValues)
    }
}

@Composable
private fun HaztrackBottomBar(
    currentRoute: String?,
    onNavigateTab: (String) -> Unit,
    onReportClick: () -> Unit,
) {
    val startItems = bottomNavItems.take(2)
    val endItems = bottomNavItems.drop(2)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 20.dp, vertical = 16.dp)
            .height(BarHeight + FabRingSize / 2),
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(BarHeight)
                .align(Alignment.BottomCenter),
            shape = BarShape,
            color = MaterialTheme.colorScheme.surfaceContainer,
            shadowElevation = 6.dp,
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                startItems.forEach { item ->
                    HaztrackNavBarItem(
                        item = item,
                        isSelected = currentRoute == item.destination.route,
                        onNavigate = onNavigateTab,
                    )
                }
                Spacer(modifier = Modifier.width(FabRingSize))
                endItems.forEach { item ->
                    HaztrackNavBarItem(
                        item = item,
                        isSelected = currentRoute == item.destination.route,
                        onNavigate = onNavigateTab,
                    )
                }
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .size(FabRingSize)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.Center,
        ) {
            FloatingActionButton(
                onClick = onReportClick,
                modifier = Modifier.size(FabSize),
                shape = CircleShape,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = stringResource(R.string.nav_report),
                )
            }
        }
    }
}

@Composable
private fun RowScope.HaztrackNavBarItem(
    item: BottomNavItem,
    isSelected: Boolean,
    onNavigate: (String) -> Unit,
) {
    val label = stringResource(item.labelRes)
    NavigationBarItem(
        selected = isSelected,
        onClick = { onNavigate(item.destination.route) },
        icon = {
            Icon(
                imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                contentDescription = label,
            )
        },
        label = null,
        colors = NavigationBarItemDefaults.colors(
            selectedIconColor = MaterialTheme.colorScheme.primary,
            indicatorColor = MaterialTheme.colorScheme.primaryContainer,
            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
        ),
    )
}

private fun NavHostController.navigateToTab(route: String) {
    navigate(route) {
        popUpTo(HaztrackDestination.Home.route) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}
