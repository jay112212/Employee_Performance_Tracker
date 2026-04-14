package com.gtu.employeeperformancetracker.ui.screens.task

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gtu.employeeperformancetracker.data.local.entity.Employee
import com.gtu.employeeperformancetracker.data.local.entity.Task
import com.gtu.employeeperformancetracker.ui.navigation.sharedAuthViewModel
import com.gtu.employeeperformancetracker.utils.Roles
import com.gtu.employeeperformancetracker.viewmodel.AuthViewModel
import com.gtu.employeeperformancetracker.viewmodel.EmployeeViewModel
import com.gtu.employeeperformancetracker.viewmodel.TaskViewModel
import java.time.LocalDate

private val taskStatuses = listOf("Pending", "In Progress", "Completed", "Reviewed")
private val taskPriorities = listOf("Low", "Medium", "High")

@Composable
fun TaskBoardScreen(
    employeeViewModel: EmployeeViewModel = viewModel(),
    taskViewModel: TaskViewModel = viewModel()
) {
    val authViewModel: AuthViewModel = sharedAuthViewModel()
    val currentUser by authViewModel.currentUser.collectAsState()
    val employees by employeeViewModel.employees.collectAsState()
    val tasks by taskViewModel.tasks.collectAsState()

    var selectedEmployeeId by remember(employees) { mutableIntStateOf(employees.firstOrNull()?.id ?: 0) }
    var description by remember { mutableStateOf("") }
    var deadline by remember { mutableStateOf(LocalDate.now().plusDays(7).toString()) }
    var priority by remember { mutableStateOf(taskPriorities[1]) }
    var status by remember { mutableStateOf(taskStatuses[0]) }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text(
                if (currentUser?.role == Roles.EMPLOYEE) "My Tasks" else "Task Board",
                style = MaterialTheme.typography.headlineMedium
            )
            Text(
                if (currentUser?.role == Roles.EMPLOYEE) {
                    "Track your assigned work and update delivery progress."
                } else {
                    "Assign tasks, monitor deadlines, and update progress states."
                },
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
                        Text("Assign New Task", fontWeight = FontWeight.SemiBold)
                        EmployeeSelector(
                            employees = employees,
                            selectedEmployeeId = selectedEmployeeId,
                            onSelected = { selectedEmployeeId = it }
                        )
                        OutlinedTextField(
                            value = description,
                            onValueChange = { description = it },
                            label = { Text("Task Description") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = deadline,
                            onValueChange = { deadline = it },
                            label = { Text("Deadline (YYYY-MM-DD)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        SimpleDropdownField("Priority", priority, taskPriorities) { priority = it }
                        SimpleDropdownField("Status", status, taskStatuses) { status = it }
                        Button(
                            onClick = {
                                if (selectedEmployeeId != 0 && description.isNotBlank()) {
                                    taskViewModel.addTask(
                                        employeeId = selectedEmployeeId,
                                        description = description.trim(),
                                        deadline = deadline.trim(),
                                        priority = priority,
                                        assignedDate = LocalDate.now().toString(),
                                        status = status
                                    )
                                    description = ""
                                    deadline = LocalDate.now().plusDays(7).toString()
                                    priority = taskPriorities[1]
                                    status = taskStatuses[0]
                                }
                            },
                            enabled = employees.isNotEmpty(),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Assign Task")
                        }
                    }
                }
            }
        }

        item {
            Text("Assigned Tasks", style = MaterialTheme.typography.titleLarge)
        }

        val visibleTasks = if (currentUser?.role == Roles.EMPLOYEE) {
            tasks.filter { it.employeeId == currentUser?.employeeId }
        } else {
            tasks
        }

        if (visibleTasks.isEmpty()) {
            item { Text("No tasks available yet.", color = MaterialTheme.colorScheme.onSurfaceVariant) }
        } else {
            items(visibleTasks, key = { it.id }) { task ->
                TaskItem(
                    task = task,
                    employee = employees.find { it.id == task.employeeId },
                    onAdvanceStatus = {
                        val nextStatus = if (currentUser?.role == Roles.EMPLOYEE) {
                            when (task.status) {
                                "Pending" -> "In Progress"
                                "In Progress" -> "Completed"
                                else -> task.status
                            }
                        } else {
                            when (task.status) {
                                "Pending" -> "In Progress"
                                "In Progress" -> "Completed"
                                "Completed" -> "Reviewed"
                                else -> "Reviewed"
                            }
                        }
                        taskViewModel.updateTask(task.copy(status = nextStatus))
                    },
                    onDelete = {
                        if (currentUser?.role != Roles.EMPLOYEE) {
                            taskViewModel.deleteTask(task)
                        }
                    },
                    employeeMode = currentUser?.role == Roles.EMPLOYEE
                )
            }
        }
    }
}

@Composable
private fun TaskItem(
    task: Task,
    employee: Employee?,
    onAdvanceStatus: () -> Unit,
    onDelete: () -> Unit,
    employeeMode: Boolean
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(task.description, fontWeight = FontWeight.SemiBold)
                    Text(employee?.name ?: "Employee removed", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                if (!employeeMode) {
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete Task")
                    }
                }
            }
            Text("${task.priority} priority | ${task.status}")
            Text("Assigned: ${task.assignedDate}")
            Text("Deadline: ${task.deadline}")
            Button(onClick = onAdvanceStatus, modifier = Modifier.fillMaxWidth()) {
                Text(if (employeeMode) "Update Progress" else "Advance Status")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmployeeSelector(
    employees: List<Employee>,
    selectedEmployeeId: Int,
    onSelected: (Int) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedLabel = employees.find { it.id == selectedEmployeeId }?.name ?: "Select employee"

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = selectedLabel,
            onValueChange = {},
            readOnly = true,
            label = { Text("Employee") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .menuAnchor(MenuAnchorType.PrimaryNotEditable, enabled = true)
                .fillMaxWidth()
        )
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            employees.forEach { employee ->
                DropdownMenuItem(
                    text = { Text("${employee.name} (${employee.employeeCode})") },
                    onClick = {
                        onSelected(employee.id)
                        expanded = false
                    }
                )
            }
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SimpleDropdownField(
    label: String,
    selected: String,
    options: List<String>,
    onSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = selected,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .menuAnchor(MenuAnchorType.PrimaryNotEditable, enabled = true)
                .fillMaxWidth()
        )
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onSelected(option)
                        expanded = false
                    }
                )
            }
        }
    }
}
