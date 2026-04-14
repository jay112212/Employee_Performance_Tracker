package com.gtu.employeeperformancetracker.ui.screens.reports

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
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
fun ReportsScreen(
    employeeViewModel: EmployeeViewModel = viewModel(),
    taskViewModel: TaskViewModel = viewModel(),
    performanceViewModel: PerformanceViewModel = viewModel()
) {
    val authViewModel: AuthViewModel = sharedAuthViewModel()
    val currentUser by authViewModel.currentUser.collectAsState()
    val context = LocalContext.current
    val employees by employeeViewModel.employees.collectAsState()
    val tasks by taskViewModel.tasks.collectAsState()
    val reviews by performanceViewModel.reviews.collectAsState()

    if (currentUser?.role == Roles.EMPLOYEE) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Reports Unavailable", style = MaterialTheme.typography.headlineSmall)
            Text(
                "Only Admin and HR users can generate workforce reports.",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        return
    }

    var departmentFilter by remember { mutableStateOf("") }
    var performanceCategory by remember { mutableStateOf("") }
    var timeRange by remember { mutableStateOf("All Time") }

    val filteredEmployees = employees.filter { employee ->
        val matchesDepartment = departmentFilter.isBlank() || employee.department.contains(departmentFilter, true)
        val employeeReviews = reviews.filter { it.employeeId == employee.id }
        val average = employeeReviews.map { it.overallRating }.average().takeIf { !it.isNaN() } ?: 0.0
        val matchesCategory = when (performanceCategory.lowercase()) {
            "", "all" -> true
            "high" -> average >= 4.0
            "medium" -> average in 3.0..3.99
            "low" -> average in 0.1..2.99
            else -> true
        }
        matchesDepartment && matchesCategory
    }

    val csvReport = buildString {
        appendLine("Employee ID,Name,Department,Role,Joining Date,Total Tasks,Completed Tasks,Average Rating,Time Range")
        filteredEmployees.forEach { employee ->
            val employeeTasks = tasks.filter { it.employeeId == employee.id }
            val employeeReviews = reviews.filter { it.employeeId == employee.id }
            val average = employeeReviews.map { it.overallRating }.average().takeIf { !it.isNaN() } ?: 0.0
            appendLine(
                listOf(
                    employee.employeeCode,
                    employee.name,
                    employee.department,
                    employee.role,
                    employee.joiningDate,
                    employeeTasks.size.toString(),
                    employeeTasks.count { it.status == "Completed" || it.status == "Reviewed" }.toString(),
                    String.format("%.2f", average),
                    timeRange
                ).joinToString(",")
            )
        }
    }

    Column(
        modifier = Modifier
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text("Reports", style = MaterialTheme.typography.headlineMedium)
        Text(
            text = "Generate filtered summaries and share a CSV-style export.",
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        OutlinedTextField(value = departmentFilter, onValueChange = { departmentFilter = it }, label = { Text("Department Filter") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = performanceCategory, onValueChange = { performanceCategory = it }, label = { Text("Performance Category (High / Medium / Low)") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = timeRange, onValueChange = { timeRange = it }, label = { Text("Time Range") }, modifier = Modifier.fillMaxWidth())

        Button(
            onClick = {
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/csv"
                    putExtra(Intent.EXTRA_SUBJECT, "Employee Performance Report")
                    putExtra(Intent.EXTRA_TEXT, csvReport)
                }
                context.startActivity(Intent.createChooser(intent, "Share report"))
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Share CSV Report")
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("Report Preview", fontWeight = FontWeight.SemiBold)
                Text("Rows: ${filteredEmployees.size}")
                Text(csvReport)
            }
        }
    }
}
