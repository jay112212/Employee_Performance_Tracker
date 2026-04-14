package com.gtu.employeeperformancetracker.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.gtu.employeeperformancetracker.ui.screens.analytics.AnalyticsScreen
import com.gtu.employeeperformancetracker.ui.screens.attendance.AttendanceScreen
import com.gtu.employeeperformancetracker.ui.screens.auth.ChangePasswordScreen
import com.gtu.employeeperformancetracker.ui.screens.dashboard.DashboardScreen
import com.gtu.employeeperformancetracker.ui.screens.employee.AddEmployeeScreen
import com.gtu.employeeperformancetracker.ui.screens.employee.EmployeeDetailScreen
import com.gtu.employeeperformancetracker.ui.screens.employee.EmployeeListScreen
import com.gtu.employeeperformancetracker.ui.screens.leave.LeaveScreen
import com.gtu.employeeperformancetracker.ui.screens.performance.PerformanceReviewScreen
import com.gtu.employeeperformancetracker.ui.screens.profile.ProfileScreen
import com.gtu.employeeperformancetracker.ui.screens.reports.ReportsScreen
import com.gtu.employeeperformancetracker.ui.screens.task.TaskBoardScreen
import com.gtu.employeeperformancetracker.ui.screens.welcome.WelcomeScreen

@Composable
fun AppNavGraph(navController: NavHostController) {
    NavHost(navController = navController, startDestination = Screen.Welcome.route) {
        composable(Screen.Welcome.route) { WelcomeScreen(navController) }
        composable(Screen.ChangePassword.route) { ChangePasswordScreen() }
        composable(Screen.Dashboard.route) { DashboardScreen() }
        composable(Screen.Employees.route) { EmployeeListScreen(navController) }
        composable(Screen.AddEmployee.route) { AddEmployeeScreen(navController) }
        composable(Screen.EmployeeDetail.route) { backStackEntry ->
            val employeeId = backStackEntry.arguments
                ?.getString("employeeId")
                ?.toIntOrNull()

            EmployeeDetailScreen(
                navController = navController,
                employeeId = employeeId
            )
        }
        composable(Screen.Tasks.route) { TaskBoardScreen() }
        composable(Screen.Attendance.route) { AttendanceScreen() }
        composable(Screen.Leave.route) { LeaveScreen() }
        composable(Screen.Analytics.route) { AnalyticsScreen() }
        composable(Screen.Reports.route) { ReportsScreen() }
        composable(Screen.Performance.route) { PerformanceReviewScreen() }
        composable(Screen.Profile.route) { ProfileScreen() }
    }
}
