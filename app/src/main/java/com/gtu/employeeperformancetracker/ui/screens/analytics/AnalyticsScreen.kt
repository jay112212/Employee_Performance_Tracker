package com.gtu.employeeperformancetracker.ui.screens.analytics

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.gtu.employeeperformancetracker.data.local.entity.Employee
import com.gtu.employeeperformancetracker.data.local.entity.Performance
import com.gtu.employeeperformancetracker.ui.navigation.sharedAuthViewModel
import com.gtu.employeeperformancetracker.utils.Roles
import com.gtu.employeeperformancetracker.viewmodel.AuthViewModel
import com.gtu.employeeperformancetracker.viewmodel.EmployeeViewModel
import com.gtu.employeeperformancetracker.viewmodel.PerformanceViewModel
import com.gtu.employeeperformancetracker.viewmodel.TaskViewModel
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

@Composable
fun AnalyticsScreen(
    navController: NavController,
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

    val employeeSummaries = employees.map { employee ->
        val employeeReviews = reviews.filter { it.employeeId == employee.id }
        val employeeTasks = tasks.filter { it.employeeId == employee.id }
        val average = employeeReviews.map { it.overallRating }.average().takeIf { !it.isNaN() } ?: 0.0
        val completion = if (employeeTasks.isEmpty()) 0.0 else {
            employeeTasks.count { it.status == "Completed" || it.status == "Reviewed" }.toDouble() / employeeTasks.size.toDouble()
        }
        EmployeeSummary(employee, average, completion)
    }.sortedByDescending { it.averageRating }

    val departmentSummaries = employees.groupBy { it.department }.map { (department, members) ->
        val ids = members.map { it.id }.toSet()
        val departmentTasks = tasks.filter { it.employeeId in ids }
        val departmentReviews = reviews.filter { it.employeeId in ids }
        val average = departmentReviews.map { it.overallRating }.average().takeIf { !it.isNaN() } ?: 0.0
        val completion = if (departmentTasks.isEmpty()) 0.0 else {
            departmentTasks.count { it.status == "Completed" || it.status == "Reviewed" }.toDouble() / departmentTasks.size.toDouble()
        }
        DepartmentSummary(department, members.size, average, completion)
    }.sortedByDescending { it.averageRating }

    val monthlyTrend = buildMonthlyTrend(reviews)
    val completionRate = if (tasks.isEmpty()) 0f else {
        tasks.count { it.status == "Completed" || it.status == "Reviewed" }.toFloat() / tasks.size.toFloat()
    }
    val topPerformers = employeeSummaries.filter { it.averageRating > 0.0 }.take(3)

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            TextButton(onClick = { navController.popBackStack() }) {
                Text("Back")
            }
            Text("Analytics", style = MaterialTheme.typography.headlineMedium)
            Text(
                "Charts, summaries, and leaderboard insights for performance tracking.",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        item {
            AnalyticsCard(title = "Employee Summary Dashboard") {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                    MiniStat("Employees", employees.size.toString(), Modifier.weight(1f))
                    MiniStat("Departments", departmentSummaries.size.toString(), Modifier.weight(1f))
                    MiniStat("Tasks Done", "${(completionRate * 100).toInt()}%", Modifier.weight(1f))
                }
            }
        }

        item {
            AnalyticsCard(title = "Average Performance Rating by Employee (Bar Chart)") {
                if (employeeSummaries.isEmpty()) {
                    Text("No employee ratings available yet.")
                } else {
                    employeeSummaries.forEach { summary ->
                        ProgressRow(
                            label = summary.employee.name,
                            value = (summary.averageRating.toFloat() / 5f).coerceIn(0f, 1f),
                            valueText = String.format("%.1f / 5", summary.averageRating)
                        )
                    }
                }
            }
        }

        item {
            AnalyticsCard(title = "Department Performance Comparison (Pie Chart)") {
                if (departmentSummaries.isEmpty()) {
                    Text("Department comparison appears once employees and reviews are available.")
                } else {
                    DepartmentPieChart(departmentSummaries = departmentSummaries)
                }
            }
        }

        item {
            AnalyticsCard(title = "Monthly Improvement Trend (Line Chart)") {
                if (monthlyTrend.isEmpty()) {
                    Text("Monthly trend data will appear after performance reviews are added.")
                } else {
                    MonthlyLineChart(points = monthlyTrend)
                }
            }
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
            Text("Top 3 Performers", style = MaterialTheme.typography.titleLarge)
        }

        if (topPerformers.isEmpty()) {
            item { Text("No performers to rank yet.") }
        } else {
            items(topPerformers, key = { it.employee.id }) { summary ->
                LeaderboardCard(
                    title = summary.employee.name,
                    subtitle = "${summary.employee.department} | ${summary.employee.role}",
                    value = String.format("%.1f / 5", summary.averageRating)
                )
            }
        }
    }
}

private data class EmployeeSummary(
    val employee: Employee,
    val averageRating: Double,
    val taskCompletionRate: Double
)

private data class DepartmentSummary(
    val department: String,
    val employeeCount: Int,
    val averageRating: Double,
    val taskCompletionRate: Double
)

private data class MonthlyTrendPoint(
    val label: String,
    val sortKey: YearMonth,
    val average: Double
)

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

@Composable
private fun DepartmentPieChart(
    departmentSummaries: List<DepartmentSummary>
) {
    val chartColors = listOf(
        Color(0xFF3949AB),
        Color(0xFF43A047),
        Color(0xFFF9A825),
        Color(0xFFE53935),
        Color(0xFF00897B)
    )
    val total = departmentSummaries.sumOf { it.averageRating }.takeIf { it > 0.0 } ?: 1.0

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Canvas(modifier = Modifier.size(140.dp)) {
            var startAngle = -90f
            departmentSummaries.forEachIndexed { index, summary ->
                val sweep = ((summary.averageRating / total) * 360f).toFloat()
                drawArc(
                    color = chartColors[index % chartColors.size],
                    startAngle = startAngle,
                    sweepAngle = sweep,
                    useCenter = true
                )
                startAngle += sweep
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.weight(1f)) {
            departmentSummaries.forEachIndexed { index, summary ->
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .padding(0.dp)
                    ) {
                        Canvas(modifier = Modifier.size(10.dp)) {
                            drawCircle(color = chartColors[index % chartColors.size])
                        }
                    }
                Text(
                    "${summary.department}: ${String.format("%.1f / 5", summary.averageRating)}",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun MonthlyLineChart(
    points: List<MonthlyTrendPoint>
) {
    val maxValue = points.maxOf { it.average }.takeIf { it > 0.0 } ?: 1.0
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
        ) {
            if (points.isEmpty()) return@Canvas

            val leftPadding = 24f
            val bottomPadding = 24f
            val topPadding = 16f
            val chartWidth = size.width - leftPadding
            val chartHeight = size.height - bottomPadding - topPadding
            val stepX = if (points.size == 1) 0f else chartWidth / (points.size - 1)

            val path = Path()
            val offsets = points.mapIndexed { index, point ->
                val x = leftPadding + (index * stepX)
                val y = topPadding + chartHeight - ((point.average / maxValue).toFloat() * chartHeight)
                Offset(x, y)
            }

            offsets.forEachIndexed { index, offset ->
                if (index == 0) path.moveTo(offset.x, offset.y) else path.lineTo(offset.x, offset.y)
            }

            drawPath(
                path = path,
                color = Color(0xFF3949AB),
                style = Stroke(width = 6f, cap = StrokeCap.Round)
            )

            offsets.forEach { offset ->
                drawCircle(color = Color(0xFF43A047), radius = 8f, center = offset)
            }
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            points.forEach { point ->
                Text(point.label, style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}

@Composable
private fun LeaderboardCard(
    title: String,
    subtitle: String,
    value: String
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(title, fontWeight = FontWeight.SemiBold)
                Text(subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text(value, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun MiniStat(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(title, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
    }
}

private fun buildMonthlyTrend(reviews: List<Performance>): List<MonthlyTrendPoint> {
    return reviews
        .groupBy { review ->
            parseDate(review.reviewDate)?.let { YearMonth.from(it) }
        }
        .filterKeys { it != null }
        .map { (month, monthReviews) ->
            val average = monthReviews.map { it.overallRating }.average().takeIf { !it.isNaN() } ?: 0.0
            MonthlyTrendPoint(
                label = month?.format(DateTimeFormatter.ofPattern("MMM")).orEmpty(),
                sortKey = month ?: YearMonth.now(),
                average = average
            )
        }
        .sortedBy { it.sortKey }
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
