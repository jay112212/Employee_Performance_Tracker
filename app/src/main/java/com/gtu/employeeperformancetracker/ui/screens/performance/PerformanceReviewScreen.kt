package com.gtu.employeeperformancetracker.ui.screens.performance

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.gtu.employeeperformancetracker.ui.navigation.sharedAuthViewModel
import com.gtu.employeeperformancetracker.ui.screens.task.EmployeeSelector
import com.gtu.employeeperformancetracker.utils.Roles
import com.gtu.employeeperformancetracker.viewmodel.AuthViewModel
import com.gtu.employeeperformancetracker.viewmodel.EmployeeViewModel
import com.gtu.employeeperformancetracker.viewmodel.PerformanceViewModel
import java.time.LocalDate

@Composable
fun PerformanceReviewScreen(
    navController: NavController,
    employeeViewModel: EmployeeViewModel = viewModel(),
    performanceViewModel: PerformanceViewModel = viewModel()
) {
    val authViewModel: AuthViewModel = sharedAuthViewModel()
    val currentUser by authViewModel.currentUser.collectAsState()
    val employees by employeeViewModel.employees.collectAsState()
    val reviews by performanceViewModel.reviews.collectAsState()

    var selectedEmployeeId by remember(employees) { mutableIntStateOf(employees.firstOrNull()?.id ?: 0) }
    var quality by remember { mutableFloatStateOf(3f) }
    var timeliness by remember { mutableFloatStateOf(3f) }
    var attendance by remember { mutableFloatStateOf(3f) }
    var communication by remember { mutableFloatStateOf(3f) }
    var innovation by remember { mutableFloatStateOf(3f) }
    var remarks by remember { mutableStateOf("") }

    val overallRating = (quality + timeliness + attendance + communication + innovation) / 5f

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            TextButton(onClick = { navController.popBackStack() }) {
                Text("Back")
            }
            Text("Performance Reviews", style = MaterialTheme.typography.headlineMedium)
            Text(
                "Evaluate employees on the PRD review metrics and keep a historical record.",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        if (currentUser?.role != Roles.EMPLOYEE) {
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text("Add Evaluation", fontWeight = FontWeight.SemiBold)
                        EmployeeSelector(
                            employees = employees,
                            selectedEmployeeId = selectedEmployeeId,
                            onSelected = { selectedEmployeeId = it }
                        )
                        ReviewSlider("Quality of Work", quality) { quality = it }
                        ReviewSlider("Timeliness", timeliness) { timeliness = it }
                        ReviewSlider("Attendance", attendance) { attendance = it }
                        ReviewSlider("Communication", communication) { communication = it }
                        ReviewSlider("Innovation / Initiative", innovation) { innovation = it }
                        OutlinedTextField(
                            value = remarks,
                            onValueChange = { remarks = it },
                            label = { Text("Overall Comments") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 3
                        )
                        Text(
                            text = "Overall Rating: ${String.format("%.1f / 5", overallRating)}",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Button(
                            onClick = {
                                if (selectedEmployeeId != 0) {
                                    performanceViewModel.addReview(
                                        employeeId = selectedEmployeeId,
                                        reviewDate = LocalDate.now().toString(),
                                        qualityScore = quality.toInt(),
                                        timelinessScore = timeliness.toInt(),
                                        attendanceScore = attendance.toInt(),
                                        communicationScore = communication.toInt(),
                                        innovationScore = innovation.toInt(),
                                        overallRating = overallRating,
                                        remarks = remarks.trim()
                                    )
                                    remarks = ""
                                }
                            },
                            enabled = employees.isNotEmpty(),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Save Review")
                        }
                    }
                }
            }
        }

        item {
            Text("Review History", style = MaterialTheme.typography.titleLarge)
        }

        val visibleReviews = if (currentUser?.role == Roles.EMPLOYEE) {
            reviews.filter { it.employeeId == currentUser?.employeeId }
        } else {
            reviews
        }

        if (visibleReviews.isEmpty()) {
            item { Text("No reviews created yet.", color = MaterialTheme.colorScheme.onSurfaceVariant) }
        } else {
            items(visibleReviews, key = { it.id }) { review ->
                val employee = employees.find { it.id == review.employeeId }
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(employee?.name ?: "Unknown employee", fontWeight = FontWeight.SemiBold)
                        Text("Date: ${review.reviewDate}")
                        Text("Overall: ${String.format("%.1f / 5", review.overallRating)}")
                        Text("Q ${review.qualityScore} | T ${review.timelinessScore} | A ${review.attendanceScore} | C ${review.communicationScore} | I ${review.innovationScore}")
                        Text(review.remarks.ifBlank { "No remarks." })
                    }
                }
            }
        }
    }
}

@Composable
private fun ReviewSlider(
    label: String,
    value: Float,
    onValueChange: (Float) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text("$label: ${value.toInt()} / 5")
        Slider(
            value = value,
            onValueChange = { onValueChange(it.coerceIn(1f, 5f)) },
            valueRange = 1f..5f,
            steps = 3
        )
    }
}
