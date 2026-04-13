package com.gtu.employeeperformancetracker.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable

import com.gtu.employeeperformancetracker.ui.screens.dashboard.DashboardScreen
import com.gtu.employeeperformancetracker.ui.screens.employee.*
import com.gtu.employeeperformancetracker.ui.screens.task.*
import com.gtu.employeeperformancetracker.ui.screens.analytics.*
import com.gtu.employeeperformancetracker.ui.screens.reports.*
import com.gtu.employeeperformancetracker.ui.screens.performance.*


@Composable
fun AppNavGraph(navController: NavHostController) {

    NavHost(navController, startDestination = Screen.Dashboard.route) {

        composable(Screen.Dashboard.route) { DashboardScreen() }

        composable(Screen.Employees.route) {
            EmployeeListScreen(navController)
        }

        composable(Screen.AddEmployee.route) {
            AddEmployeeScreen(navController)
        }

        composable(Screen.EmployeeDetail.route) { backStackEntry ->

            val employeeId = backStackEntry.arguments
                ?.getString("employeeId")
                ?.toIntOrNull()

            EmployeeDetailScreen(
                navController = navController,
                employeeId = employeeId
            )
        }

        composable(Screen.Tasks.route) {
            TaskBoardScreen()
        }

        composable(Screen.Analytics.route) {
            AnalyticsScreen()
        }

        composable(Screen.Reports.route) {
            ReportsScreen()
        }

        composable(Screen.Performance.route) {
            PerformanceReviewScreen()
        }
    }
}