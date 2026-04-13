package com.gtu.employeeperformancetracker.ui.navigation

sealed class Screen(val route: String) {

    object Dashboard : Screen("dashboard")
    object Employees : Screen("employees")
    object AddEmployee : Screen("add_employee")

    // 🔥 Dynamic route with ID
    object EmployeeDetail : Screen("employee_detail/{employeeId}") {
        fun createRoute(employeeId: Int) = "employee_detail/$employeeId"
    }

    object Tasks : Screen("tasks")
    object Analytics : Screen("analytics")
    object Reports : Screen("reports")
    object Performance : Screen("performance")
}