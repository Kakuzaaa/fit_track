package com.example.fitnesstracker.ui.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.fitnesstracker.ui.history.HistoryScreen
import com.example.fitnesstracker.ui.profile.ProfileScreen
import com.example.fitnesstracker.ui.today.TodayScreen

private data class TopDestination(
    val route: String,
    val label: String,
    val icon: ImageVector
)

// NavigationBarItem сам подсвечивает иконку в выбранном состоянии.
private val topDestinations = listOf(
    TopDestination("today", "Сегодня", Icons.Filled.Today),
    TopDestination("history", "История", Icons.Filled.History),
    TopDestination("profile", "Профиль", Icons.Filled.Person)
)

/**
 * Корень приложения: нижняя панель навигации из 3 вкладок + NavHost.
 */
@Composable
fun FitTrackerAppRoot() {
    val navController = rememberNavController()
    val snackbarHostState = remember { SnackbarHostState() }
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    Scaffold(
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.padding(16.dp)
            ) { snackbarData ->
                Snackbar(
                    snackbarData = snackbarData,
                    shape = RoundedCornerShape(16.dp),
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                    actionColor = MaterialTheme.colorScheme.primary,
                    dismissActionContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        bottomBar = {
            NavigationBar {
                topDestinations.forEach { destination ->
                    NavigationBarItem(
                        selected = currentRoute == destination.route,
                        onClick = {
                            navController.navigate(destination.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = {
                            Icon(
                                imageVector = destination.icon,
                                contentDescription = destination.label
                            )
                        },
                        label = { Text(destination.label) }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "today",
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            composable("today") {
                TodayScreen(snackbarHostState = snackbarHostState)
            }
            composable("history") {
                HistoryScreen(snackbarHostState = snackbarHostState)
            }
            composable("profile") {
                ProfileScreen(snackbarHostState = snackbarHostState)
            }
        }
    }
}
