package com.gtu.employeeperformancetracker.ui.screens.analytics

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gtu.employeeperformancetracker.ui.navigation.sharedAuthViewModel
import com.gtu.employeeperformancetracker.utils.Roles
import com.gtu.employeeperformancetracker.viewmodel.AuthViewModel
import com.gtu.employeeperformancetracker.viewmodel.EmployeeViewModel
import com.gtu.employeeperformancetracker.viewmodel.PerformanceViewModel
import com.gtu.employeeperformancetracker.viewmodel.TaskViewModel

@Composable
fun AnalyticsScreen(
    employeeViewModel: EmployeeViewModel = viewModel(),
    taskViewModel: TaskViewModel = viewModel(),
    performanceViewModel: PerformanceViewModel = viewModel()
) {
    val authViewModel: AuthViewModel = sharedAuthViewModel()
    val currentUser by authViewModel.currentUser.collectAsState()
    val employees by employeeViewModel.employees.collectAsState()
    val tasks by taskViewModel.tasks.collectAsState()
    val reviews by performanceViewModel.reviews.collectAsState()

    if (currentUser?.role == Roles.EMPLOYEE) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Analytics Unavailable", style = MaterialTheme.typography.headlineSmall)
            Text(
                "Detailed team analytics are restricted to Admin and HR accounts.",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        return
    }

    val ratingByEmployee = employees.map { employee ->
        val employeeReviews = reviews.filter { it.employeeId == employee.id }
        val average = employeeReviews.map { it.overallRating }.average().takeIf { !it.isNaN() } ?: 0.0
        employee.name to average.toFloat()
    }.sortedByDescending { it.second }

    val departmentPerformance = employees.groupBy { it.department }.mapValues { (_, departmentEmployees) ->
        val departmentIds = departmentEmployees.map { it.id }.toSet()
        val departmentReviews = reviews.filter { it.employeeId in departmentIds }
        departmentReviews.map { it.overallRating }.average().takeIf { !it.isNaN() } ?: 0.0
    }.toList().sortedByDescending { it.second }

    val completionRate = if (tasks.isEmpty()) 0f else {
        tasks.count { it.status == "Completed" || it.status == "Reviewed" }.toFloat() / tasks.size.toFloat()
    }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text("Analytics", style = MaterialTheme.typography.headlineMedium)
            Text(
                "Employee ratings, department comparisons, and task completion insights.",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        item {
            AnalyticsCard(title = "Task Completion Rate") {
                Text(
                    text = "${(completionRate * 100).toInt()}%",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                LinearProgressIndicator(
                    progress = { completionRate },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .clip(CircleShape)
                )
            }
        }

        item {
            AnalyticsCard(title = "Average Rating by Employee") {
                if (ratingByEmployee.isEmpty()) {
                    Text("No performance reviews available yet.")
                } else {
                    ratingByEmployee.forEach { (name, rating) ->
                        ProgressRow(
                            label = name,
                            value = rating / 5f,
                            valueText = String.format("%.1f / 5", rating)
                        )
                    }
                }
            }
        }

        item {
            AnalyticsCard(title = "Department Performance Comparison") {
                if (departmentPerformance.isEmpty()) {
                    Text("Department analytics will appear once reviews are added.")
                } else {
                    departmentPerformance.forEach { (department, average) ->
                        ProgressRow(
                            label = department,
                            value = (average.toFloat() / 5f).coerceIn(0f, 1f),
                            valueText = String.format("%.1f / 5", average)
                        )
                    }
                }
            }
        }

        item {
            Text("Top 3 Performers", style = MaterialTheme.typography.titleLarge)
        }

        if (ratingByEmployee.isEmpty()) {
            item { Text("No performers to rank yet.") }
        } else {
            items(ratingByEmployee.take(3), key = { it.first }) { item ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(item.first, fontWeight = FontWeight.SemiBold)
                        Text(String.format("%.1f / 5", item.second))
                    }
                }
            }
        }
    }
}

@Composable
private fun AnalyticsCard(
    title: String,
    content: @Composable () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            content()
        }
    }
}

@Composable
private fun ProgressRow(
    label: String,
    value: Float,
    valueText: String
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            Text(label)
            Text(valueText, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        LinearProgressIndicator(
            progress = { value.coerceIn(0f, 1f) },
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp)
                .clip(CircleShape)
        )
    }
}
