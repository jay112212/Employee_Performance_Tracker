package com.gtu.employeeperformancetracker.ui.screens.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.gtu.employeeperformancetracker.data.local.entity.Employee
import com.gtu.employeeperformancetracker.viewmodel.EmployeeViewModel
import com.gtu.employeeperformancetracker.viewmodel.PerformanceViewModel
import com.gtu.employeeperformancetracker.viewmodel.TaskViewModel

@Composable
fun DashboardScreen(
    employeeViewModel: EmployeeViewModel = viewModel(),
    taskViewModel: TaskViewModel = viewModel(),
    performanceViewModel: PerformanceViewModel = viewModel()
) {
    val employees by employeeViewModel.employees.collectAsState()
    val tasks by taskViewModel.tasks.collectAsState()
    val reviews by performanceViewModel.reviews.collectAsState()

    val departmentCount = employees.map { it.department }.distinct().size
    val completionRate = if (tasks.isEmpty()) 0 else {
        ((tasks.count { it.status == "Completed" || it.status == "Reviewed" }.toFloat() / tasks.size.toFloat()) * 100).toInt()
    }
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
            Text("Dashboard", style = MaterialTheme.typography.headlineMedium)
            Text(
                "Quick stats, top performers, and recent workforce health at a glance.",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
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
