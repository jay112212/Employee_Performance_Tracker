package com.gtu.employeeperformancetracker.ui.screens.employee

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.gtu.employeeperformancetracker.ui.navigation.Screen
import com.gtu.employeeperformancetracker.viewmodel.EmployeeViewModel
import com.gtu.employeeperformancetracker.viewmodel.PerformanceViewModel
import com.gtu.employeeperformancetracker.viewmodel.TaskViewModel

@Composable
fun EmployeeDetailScreen(
    navController: NavController,
    employeeId: Int?,
    employeeViewModel: EmployeeViewModel = viewModel(),
    taskViewModel: TaskViewModel = viewModel(),
    performanceViewModel: PerformanceViewModel = viewModel()
) {
    val employees by employeeViewModel.employees.collectAsState()
    val tasks by taskViewModel.tasks.collectAsState()
    val reviews by performanceViewModel.reviews.collectAsState()
    val employee = employees.find { it.id == employeeId }

    if (employee == null) {
        Text("Employee not found.", modifier = Modifier.padding(16.dp))
        return
    }

    var name by remember { mutableStateOf(employee.name) }
    var role by remember { mutableStateOf(employee.role) }
    var department by remember { mutableStateOf(employee.department) }
    var joiningDate by remember { mutableStateOf(employee.joiningDate) }
    var email by remember { mutableStateOf(employee.email) }
    var contact by remember { mutableStateOf(employee.contact) }
    var profileUri by remember { mutableStateOf(employee.profilePictureUri.orEmpty()) }

    LaunchedEffect(employee.id) {
        name = employee.name
        role = employee.role
        department = employee.department
        joiningDate = employee.joiningDate
        email = employee.email
        contact = employee.contact
        profileUri = employee.profilePictureUri.orEmpty()
    }

    val employeeTasks = tasks.filter { it.employeeId == employee.id }
    val employeeReviews = reviews.filter { it.employeeId == employee.id }
    val averageRating = employeeReviews.map { it.overallRating }.average().takeIf { !it.isNaN() } ?: 0.0

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    employeeViewModel.deleteEmployee(employee)
                    navController.popBackStack()
                }
            ) {
                Icon(Icons.Default.Delete, contentDescription = "Delete Employee")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(employee.name, style = MaterialTheme.typography.headlineMedium)
            Text("${employee.employeeCode} | ${employee.role}", color = MaterialTheme.colorScheme.onSurfaceVariant)

            DetailSection(title = "Profile") {
                LabeledValue("Department", employee.department)
                LabeledValue("Joining Date", employee.joiningDate)
                LabeledValue("Email", employee.email)
                LabeledValue("Contact", employee.contact)
                LabeledValue("Profile URI", employee.profilePictureUri ?: "Not provided")
                LabeledValue(
                    "Average Rating",
                    if (employeeReviews.isEmpty()) "No reviews yet" else String.format("%.1f / 5", averageRating)
                )
            }

            DetailSection(title = "Quick Edit") {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Name") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = role, onValueChange = { role = it }, label = { Text("Role") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = department, onValueChange = { department = it }, label = { Text("Department") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = joiningDate, onValueChange = { joiningDate = it }, label = { Text("Joining Date") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = contact, onValueChange = { contact = it }, label = { Text("Contact") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = profileUri, onValueChange = { profileUri = it }, label = { Text("Profile Picture URI") }, modifier = Modifier.fillMaxWidth())
                Button(
                    onClick = {
                        employeeViewModel.updateEmployee(
                            employee.copy(
                                name = name.trim(),
                                role = role.trim(),
                                department = department.trim(),
                                joiningDate = joiningDate.trim(),
                                email = email.trim(),
                                contact = contact.trim(),
                                profilePictureUri = profileUri.trim().ifBlank { null }
                            )
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Update Employee")
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(onClick = { navController.navigate(Screen.Tasks.route) }, modifier = Modifier.weight(1f)) {
                    Text("Assign Task")
                }
                Button(onClick = { navController.navigate(Screen.Performance.route) }, modifier = Modifier.weight(1f)) {
                    Text("Add Review")
                }
            }

            DetailSection(title = "Assigned Tasks") {
                if (employeeTasks.isEmpty()) {
                    Text("No tasks assigned yet.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    employeeTasks.forEach { task ->
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(task.description, fontWeight = FontWeight.SemiBold)
                                Text("${task.priority} priority | ${task.status}")
                                Text("Deadline: ${task.deadline}")
                            }
                        }
                    }
                }
            }

            DetailSection(title = "Performance History") {
                if (employeeReviews.isEmpty()) {
                    Text("No performance reviews yet.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    employeeReviews.forEach { review ->
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text("Review on ${review.reviewDate}", fontWeight = FontWeight.SemiBold)
                                Text("Overall Rating: ${String.format("%.1f / 5", review.overallRating)}")
                                Text(review.remarks.ifBlank { "No remarks added." })
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailSection(
    title: String,
    content: @Composable () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            content()
        }
    }
}

@Composable
private fun LabeledValue(
    label: String,
    value: String
) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value)
    }
}
