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
import androidx.compose.material3.TextButton
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
import androidx.navigation.NavController
import com.gtu.employeeperformancetracker.data.local.entity.Employee
import com.gtu.employeeperformancetracker.ui.navigation.sharedAuthViewModel
import com.gtu.employeeperformancetracker.utils.Roles
import com.gtu.employeeperformancetracker.viewmodel.AuthViewModel
import com.gtu.employeeperformancetracker.viewmodel.EmployeeViewModel
import com.gtu.employeeperformancetracker.viewmodel.PerformanceViewModel
import com.gtu.employeeperformancetracker.viewmodel.TaskViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun ReportsScreen(
    navController: NavController,
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
    var performanceCategory by remember { mutableStateOf("All") }
    var timeRange by remember { mutableStateOf("All Time") }

    val dateThreshold = calculateThreshold(timeRange)
    val filteredEmployees = employees.filter { employee ->
        val matchesDepartment = departmentFilter.isBlank() || employee.department.contains(departmentFilter, true)
        val employeeReviews = reviews.filter { it.employeeId == employee.id }
        val average = employeeReviews.map { it.overallRating }.average().takeIf { !it.isNaN() } ?: 0.0
        val matchesCategory = when (performanceCategory.lowercase()) {
            "all" -> true
            "high" -> average >= 4.0
            "medium" -> average in 3.0..3.99
            "low" -> average in 0.0..2.99
            else -> true
        }
        val matchesTime = dateThreshold == null || employeeReviews.any { review ->
            parseDate(review.reviewDate)?.let { !it.isBefore(dateThreshold) } == true
        } || employeeReviews.isEmpty()

        matchesDepartment && matchesCategory && matchesTime
    }

    val previewRows = filteredEmployees.map { employee ->
        buildReportRow(employee, tasks, reviews, timeRange, dateThreshold)
    }

    val csvReport = buildString {
        appendLine("Employee ID,Name,Department,Role,Joining Date,Total Tasks,Completed Tasks,Average Rating,Latest Review,Time Range,Performance Category")
        previewRows.forEach { row ->
            appendLine(
                listOf(
                    row.employee.employeeCode,
                    row.employee.name,
                    row.employee.department,
                    row.employee.role,
                    row.employee.joiningDate,
                    row.totalTasks.toString(),
                    row.completedTasks.toString(),
                    String.format("%.2f", row.averageRating),
                    row.latestReviewDate,
                    row.timeRange,
                    row.performanceCategory
                ).joinToString(",")
            )
        }
    }

    val textReport = buildString {
        appendLine("Employee Performance Report")
        appendLine("Department Filter: ${departmentFilter.ifBlank { "All" }}")
        appendLine("Performance Category: $performanceCategory")
        appendLine("Time Range: $timeRange")
        appendLine("Employees Included: ${previewRows.size}")
        appendLine()
        previewRows.forEach { row ->
            appendLine("${row.employee.name} (${row.employee.employeeCode})")
            appendLine("Department: ${row.employee.department}")
            appendLine("Role: ${row.employee.role}")
            appendLine("Tasks: ${row.completedTasks}/${row.totalTasks} completed")
            appendLine("Average Rating: ${String.format("%.1f / 5", row.averageRating)}")
            appendLine("Latest Review: ${row.latestReviewDate}")
            appendLine("Category: ${row.performanceCategory}")
            appendLine()
        }
    }

    Column(
        modifier = Modifier
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        TextButton(
            onClick = { navController.popBackStack() }
        ) {
            Text("Back")
        }
        Text("Reports", style = MaterialTheme.typography.headlineMedium)
        Text(
            text = "Generate filtered summaries and export text or CSV reports.",
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        OutlinedTextField(
            value = departmentFilter,
            onValueChange = { departmentFilter = it },
            label = { Text("Department Filter") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = performanceCategory,
            onValueChange = { performanceCategory = it },
            label = { Text("Performance Category (All / High / Medium / Low)") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = timeRange,
            onValueChange = { timeRange = it },
            label = { Text("Time Range (All Time / Last 30 Days / Last 90 Days / This Year)") },
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = {
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/csv"
                    putExtra(Intent.EXTRA_SUBJECT, "Employee Performance CSV Report")
                    putExtra(Intent.EXTRA_TEXT, csvReport)
                }
                context.startActivity(Intent.createChooser(intent, "Share CSV report"))
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Share CSV Report")
        }

        Button(
            onClick = {
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_SUBJECT, "Employee Performance Summary")
                    putExtra(Intent.EXTRA_TEXT, textReport)
                }
                context.startActivity(Intent.createChooser(intent, "Share text report"))
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Share Text Report")
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("Report Preview", fontWeight = FontWeight.SemiBold)
                Text("Rows: ${previewRows.size}")
                if (previewRows.isEmpty()) {
                    Text("No employees match the selected filters.")
                } else {
                    previewRows.forEach { row ->
                        Text(
                            "${row.employee.name} | ${row.employee.department} | ${String.format("%.1f / 5", row.averageRating)} | ${row.completedTasks}/${row.totalTasks} tasks"
                        )
                    }
                }
            }
        }
    }
}

private data class ReportRow(
    val employee: Employee,
    val totalTasks: Int,
    val completedTasks: Int,
    val averageRating: Double,
    val latestReviewDate: String,
    val performanceCategory: String,
    val timeRange: String
)

private fun buildReportRow(
    employee: Employee,
    tasks: List<com.gtu.employeeperformancetracker.data.local.entity.Task>,
    reviews: List<com.gtu.employeeperformancetracker.data.local.entity.Performance>,
    timeRange: String,
    threshold: LocalDate?
): ReportRow {
    val employeeTasks = tasks.filter { it.employeeId == employee.id }
    val employeeReviews = reviews.filter { it.employeeId == employee.id }
    val reviewsInRange = if (threshold == null) {
        employeeReviews
    } else {
        employeeReviews.filter { review ->
            parseDate(review.reviewDate)?.let { !it.isBefore(threshold) } == true
        }
    }
    val average = reviewsInRange.map { it.overallRating }.average().takeIf { !it.isNaN() } ?: 0.0
    val latestDate = reviewsInRange.maxByOrNull { parseDate(it.reviewDate) ?: LocalDate.MIN }?.reviewDate ?: "N/A"
    val category = when {
        average >= 4.0 -> "High"
        average >= 3.0 -> "Medium"
        average > 0.0 -> "Low"
        else -> "Unrated"
    }

    return ReportRow(
        employee = employee,
        totalTasks = employeeTasks.size,
        completedTasks = employeeTasks.count { it.status == "Completed" || it.status == "Reviewed" },
        averageRating = average,
        latestReviewDate = latestDate,
        performanceCategory = category,
        timeRange = timeRange
    )
}

private fun calculateThreshold(timeRange: String): LocalDate? {
    val today = LocalDate.now()
    return when (timeRange.trim().lowercase()) {
        "last 30 days" -> today.minusDays(30)
        "last 90 days" -> today.minusDays(90)
        "this year" -> LocalDate.of(today.year, 1, 1)
        else -> null
    }
}

private fun parseDate(value: String): LocalDate? {
    val formatters = listOf(
        DateTimeFormatter.ISO_LOCAL_DATE,
        DateTimeFormatter.ofPattern("dd/MM/yyyy")
    )
    return formatters.firstNotNullOfOrNull { formatter ->
        runCatching { LocalDate.parse(value, formatter) }.getOrNull()
    }
}
