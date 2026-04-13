package com.gtu.employeeperformancetracker.ui.navigation

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*

@Composable
fun BottomNavBar(navController: NavController) {

    NavigationBar {

        NavigationBarItem(
            selected = false,
            onClick = { navController.navigate(Screen.Dashboard.route) },
            icon = { Icon(Icons.Default.Home, null) },
            label = { Text("Dashboard") }
        )

        NavigationBarItem(
            selected = false,
            onClick = { navController.navigate(Screen.Employees.route) },
            icon = { Icon(Icons.Default.Person, null) },
            label = { Text("Employees") }
        )

        NavigationBarItem(
            selected = false,
            onClick = { navController.navigate(Screen.Tasks.route) },
            icon = { Icon(Icons.Default.List, null) },
            label = { Text("Tasks") }
        )

        NavigationBarItem(
            selected = false,
            onClick = { navController.navigate(Screen.Analytics.route) },
            icon = { Icon(Icons.Default.Info, null) }, // ✅ FIXED
            label = { Text("Analytics") }
        )

        NavigationBarItem(
            selected = false,
            onClick = { navController.navigate(Screen.Reports.route) },
            icon = { Icon(Icons.Default.Settings, null) }, // ✅ FIXED
            label = { Text("Reports") }
        )
    }
}