package com.gtu.employeeperformancetracker.ui.navigation

sealed class Screen(val route: String) {
    data object Welcome : Screen("welcome")
    data object Dashboard : Screen("dashboard")
    data object Employees : Screen("employees")
    data object AddEmployee : Screen("add_employee")
    data object EmployeeDetail : Screen("employee_detail/{employeeId}") {
        fun createRoute(employeeId: Int) = "employee_detail/$employeeId"
    }
    data object Tasks : Screen("tasks")
    data object Attendance : Screen("attendance")
    data object Leave : Screen("leave")
    data object Analytics : Screen("analytics")
    data object Reports : Screen("reports")
    data object Performance : Screen("performance")
    data object Profile : Screen("profile")
    data object ChangePassword : Screen("change_password")
}
