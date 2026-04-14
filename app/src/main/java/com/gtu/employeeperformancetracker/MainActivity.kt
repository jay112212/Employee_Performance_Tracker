package com.gtu.employeeperformancetracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.gtu.employeeperformancetracker.ui.navigation.AppNavGraph
import com.gtu.employeeperformancetracker.ui.navigation.BottomNavBar
import com.gtu.employeeperformancetracker.ui.navigation.Screen
import com.gtu.employeeperformancetracker.ui.theme.EmployeePerformanceTrackerTheme
import com.gtu.employeeperformancetracker.viewmodel.AuthViewModel

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            EmployeePerformanceTrackerTheme(dynamicColor = false) {
                val navController = rememberNavController()
                val authViewModel: AuthViewModel = viewModel()
                val currentUser by authViewModel.currentUser.collectAsState()
                val user = currentUser
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route
                val showBottomBar =
                    user != null &&
                        currentRoute != Screen.Welcome.route &&
                        currentRoute != Screen.ChangePassword.route

                LaunchedEffect(user?.id, user?.forcePasswordReset, currentRoute) {
                    when {
                        user == null && currentRoute != Screen.Welcome.route -> {
                            navController.navigate(Screen.Welcome.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    inclusive = true
                                }
                                launchSingleTop = true
                            }
                        }

                        user?.forcePasswordReset == true &&
                            currentRoute != Screen.ChangePassword.route -> {
                            navController.navigate(Screen.ChangePassword.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    inclusive = false
                                }
                                launchSingleTop = true
                            }
                        }

                        user != null &&
                            user.forcePasswordReset == false &&
                            (currentRoute == Screen.Welcome.route || currentRoute == Screen.ChangePassword.route) -> {
                            navController.navigate(Screen.Dashboard.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    inclusive = true
                                }
                                launchSingleTop = true
                            }
                        }
                    }
                }

                Scaffold(
                    bottomBar = {
                        if (showBottomBar) {
                            BottomNavBar(
                                navController = navController,
                                userRole = user?.role
                            )
                        }
                    }
                ) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {
                        AppNavGraph(navController)
                    }
                }
            }
        }
    }
}
