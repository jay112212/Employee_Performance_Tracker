package com.gtu.employeeperformancetracker.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState

private data class BottomNavItem(
    val label: String,
    val screen: Screen,
    val icon: @Composable () -> Unit
)

@Composable
fun BottomNavBar(navController: NavController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val items = listOf(
        BottomNavItem(
            label = "Dashboard",
            screen = Screen.Dashboard,
            icon = { Icon(Icons.Default.Home, contentDescription = null) }
        ),
        BottomNavItem(
            label = "Employees",
            screen = Screen.Employees,
            icon = { Icon(Icons.Default.Person, contentDescription = null) }
        ),
        BottomNavItem(
            label = "Tasks",
            screen = Screen.Tasks,
            icon = { Icon(Icons.AutoMirrored.Filled.List, contentDescription = null) }
        ),
        BottomNavItem(
            label = "Analytics",
            screen = Screen.Analytics,
            icon = { Icon(Icons.Default.Info, contentDescription = null) }
        ),
        BottomNavItem(
            label = "Reports",
            screen = Screen.Reports,
            icon = { Icon(Icons.Default.Settings, contentDescription = null) }
        )
    )

    NavigationBar {
        items.forEach { item ->
            NavigationBarItem(
                selected = currentDestination.isSelected(item.screen),
                onClick = {
                    navController.navigate(item.screen.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = item.icon,
                label = { Text(item.label) }
            )
        }
    }
}

private fun NavDestination?.isSelected(screen: Screen): Boolean {
    if (this == null) return false

    return when (screen) {
        Screen.Employees -> hierarchy.any { destination ->
            destination.route == Screen.Employees.route ||
                destination.route == Screen.AddEmployee.route ||
                destination.route == Screen.EmployeeDetail.route ||
                destination.route?.startsWith("employee_detail/") == true
        }

        else -> hierarchy.any { destination -> destination.route == screen.route }
    }
}
