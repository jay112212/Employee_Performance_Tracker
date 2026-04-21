package com.gtu.employeeperformancetracker.ui.screens.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.gtu.employeeperformancetracker.data.local.entity.Employee
import com.gtu.employeeperformancetracker.ui.navigation.Screen
import com.gtu.employeeperformancetracker.ui.navigation.sharedAuthViewModel
import com.gtu.employeeperformancetracker.utils.Roles
import com.gtu.employeeperformancetracker.viewmodel.AttendanceViewModel
import com.gtu.employeeperformancetracker.viewmodel.AuthViewModel
import com.gtu.employeeperformancetracker.viewmodel.LeaveViewModel
import com.gtu.employeeperformancetracker.viewmodel.PerformanceViewModel
import com.gtu.employeeperformancetracker.viewmodel.TaskViewModel
import java.time.LocalDate

@Composable
fun DashboardScreen(
    navController: NavController,
    attendanceViewModel: AttendanceViewModel = viewModel(),
    leaveViewModel: LeaveViewModel = viewModel(),
    taskViewModel: TaskViewModel = viewModel(),
    performanceViewModel: PerformanceViewModel = viewModel()
) {
    val authViewModel: AuthViewModel = sharedAuthViewModel()
    val currentUser by authViewModel.currentUser.collectAsState()
    val currentEmployee by authViewModel.currentEmployee.collectAsState()
    val employees by authViewModel.employees.collectAsState()
    val emailLogs by authViewModel.emailLogs.collectAsState()
    val attendanceRecords by attendanceViewModel.attendanceRecords.collectAsState()
    val leaveRequests by leaveViewModel.leaveRequests.collectAsState()
    val tasks by taskViewModel.tasks.collectAsState()
    val reviews by performanceViewModel.reviews.collectAsState()

    if (currentUser?.role == Roles.EMPLOYEE) {
        val myTasks = tasks.filter { it.employeeId == currentUser?.employeeId }
        val myReviews = reviews.filter { it.employeeId == currentUser?.employeeId }
        val myAttendance = attendanceRecords.filter { it.employeeId == currentUser?.employeeId }
        val myLeaves = leaveRequests.filter { it.employeeId == currentUser?.employeeId }
        val completionRate = if (myTasks.isEmpty()) 0 else {
            ((myTasks.count { it.status == "Completed" || it.status == "Reviewed" }.toFloat() / myTasks.size.toFloat()) * 100).toInt()
        }
        val averageRating = myReviews.map { it.overallRating }.average().takeIf { !it.isNaN() } ?: 0.0

        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text("Employee Home", style = MaterialTheme.typography.headlineMedium)
                Text(
                    "Welcome ${currentEmployee?.name ?: currentUser?.displayName.orEmpty()}",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                    StatCard("My Tasks", myTasks.size.toString(), Modifier.weight(1f))
                    StatCard("Completion", "$completionRate%", Modifier.weight(1f))
                }
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                    StatCard("Reviews", myReviews.size.toString(), Modifier.weight(1f))
                    StatCard("Average", if (myReviews.isEmpty()) "--" else String.format("%.1f / 5", averageRating), Modifier.weight(1f))
                }
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                    StatCard("Attendance", myAttendance.size.toString(), Modifier.weight(1f))
                    StatCard("Leave", myLeaves.count { it.status == "Pending" }.toString(), Modifier.weight(1f))
                }
            }
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Onboarding Status", fontWeight = FontWeight.SemiBold)
                        val onboardingMail = emailLogs.firstOrNull { it.recipientEmail == currentUser?.email }
                        Text(onboardingMail?.body ?: "No onboarding email logged for this account yet.")
                    }
                }
            }
        }
    } else {
        val departmentCount = employees.map { it.department }.distinct().size
        val pendingLeaves = leaveRequests.count { it.status == "Pending" }
        val todayAttendance = attendanceRecords.count { it.date == LocalDate.now().toString() }
        val completionRate = if (tasks.isEmpty()) 0 else {
            ((tasks.count { it.status == "Completed" || it.status == "Reviewed" }.toFloat() / tasks.size.toFloat()) * 100).toInt()
        }
        val upcomingDeadlines = tasks.filter { task ->
            val deadline = runCatching { LocalDate.parse(task.deadline) }.getOrNull()
            deadline != null &&
                !deadline.isBefore(LocalDate.now()) &&
                !deadline.isAfter(LocalDate.now().plusDays(3)) &&
                task.status != "Completed" &&
                task.status != "Reviewed"
        }.sortedBy { it.deadline }
        val unreviewedTasks = tasks.filter { it.status == "Completed" }
        val topPerformers = employees.mapNotNull { employee ->
            val employeeReviews = reviews.filter { it.employeeId == employee.id }
            val average = employeeReviews.map { it.overallRating }.average()
            if (average.isNaN()) null else employee to average
        }.sortedByDescending { it.second }.take(3)

        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    if (currentUser?.role == Roles.HR) "HR Dashboard" else "Admin Dashboard",
                    style = MaterialTheme.typography.headlineMedium
                )
                Text(
                    "Manage workforce onboarding, reviews, and productivity.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text("Signed In Account", fontWeight = FontWeight.SemiBold)
                        Text(currentUser?.displayName ?: "-", style = MaterialTheme.typography.titleMedium)
                        Text(currentUser?.email ?: "-", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("Role: ${currentUser?.role ?: "-"}")
                    }
                }
            }
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text("Quick Actions", fontWeight = FontWeight.SemiBold)
                        Button(
                            onClick = { navController.navigate(Screen.Reports.route) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Open Reports")
                        }
                    }
                }
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                    StatCard("Employees", employees.size.toString(), Modifier.weight(1f))
                    StatCard("Departments", departmentCount.toString(), Modifier.weight(1f))
                }
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                    StatCard("Tasks", tasks.size.toString(), Modifier.weight(1f))
                    StatCard("Completion", "$completionRate%", Modifier.weight(1f))
                }
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                    StatCard("Today Attendance", todayAttendance.toString(), Modifier.weight(1f))
                    StatCard("Pending Leave", pendingLeaves.toString(), Modifier.weight(1f))
                }
            }
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Recent Onboarding Emails", fontWeight = FontWeight.SemiBold)
                        if (emailLogs.isEmpty()) {
                            Text("No onboarding emails have been generated yet.")
                        } else {
                            emailLogs.take(3).forEach { email ->
                                Text("${email.recipientName} - ${email.recipientEmail}")
                            }
                        }
                    }
                }
            }
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Notifications & Reminders", fontWeight = FontWeight.SemiBold)
                        if (upcomingDeadlines.isEmpty() && unreviewedTasks.isEmpty()) {
                            Text("No urgent reminders right now.")
                        } else {
                            if (upcomingDeadlines.isNotEmpty()) {
                                Text(
                                    "Upcoming deadlines in next 3 days: ${upcomingDeadlines.size}",
                                    color = MaterialTheme.colorScheme.primary
                                )
                                upcomingDeadlines.take(3).forEach { task ->
                                    val employeeName = employees.find { it.id == task.employeeId }?.name ?: "Unknown"
                                    Text("${task.description} - $employeeName - due ${task.deadline}")
                                }
                            }
                            if (unreviewedTasks.isNotEmpty()) {
                                Text(
                                    "Completed tasks waiting for review: ${unreviewedTasks.size}",
                                    color = MaterialTheme.colorScheme.primary
                                )
                                unreviewedTasks.take(3).forEach { task ->
                                    val employeeName = employees.find { it.id == task.employeeId }?.name ?: "Unknown"
                                    Text("${task.description} - $employeeName")
                                }
                            }
                        }
                    }
                }
            }
            item {
                Text("Top Performers", style = MaterialTheme.typography.titleLarge)
            }
            if (topPerformers.isEmpty()) {
                item {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("No performance reviews yet.")
                        }
                    }
                }
            } else {
                items(topPerformers, key = { it.first.id }) { (employee, average) ->
                    PerformerCard(employee = employee, average = average)
                }
            }
            item {
                Button(
                    onClick = { authViewModel.logout() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Logout")
                }
            }
        }
    }
}

@Composable
private fun StatCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(title, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun PerformerCard(
    employee: Employee,
    average: Double
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(employee.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text("${employee.department} | ${employee.role}", color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("Average Rating: ${String.format("%.1f / 5", average)}")
        }
    }
}
