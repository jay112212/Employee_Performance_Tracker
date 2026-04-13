package com.gtu.employeeperformancetracker.ui.screens.employee

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.gtu.employeeperformancetracker.data.local.entity.Employee
import com.gtu.employeeperformancetracker.viewmodel.EmployeeViewModel

@Composable
fun EmployeeDetailScreen(
    navController: NavController,
    employeeId: Int?,
    viewModel: EmployeeViewModel = viewModel()
) {

    // 🔥 TEMP SAMPLE (later we pass real ID)
    val employees by viewModel.employees.collectAsState(initial = emptyList())
    val employee = employees.find { it.id == employeeId }

    if (employee == null) {
        Text("No Employee Found", modifier = Modifier.padding(16.dp))
        return
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    viewModel.deleteEmployee(employee)
                    navController.popBackStack()
                }
            ) {
                Icon(Icons.Default.Delete, contentDescription = "Delete")
            }
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
        ) {

            Text(
                text = "Employee Detail",
                style = MaterialTheme.typography.headlineMedium
            )

            Spacer(modifier = Modifier.height(20.dp))

            EmployeeInfoCard(employee)

            Spacer(modifier = Modifier.height(20.dp))

            RatingSection(employee = employee)

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = {
                    // future: navigate to performance screen
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Add Performance Review")
            }
        }
    }
}

@Composable
fun EmployeeInfoCard(employee: Employee) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Name: ${employee.name}", style = MaterialTheme.typography.titleMedium)
            Text("Role: ${employee.role}")
            Text("Department: ${employee.department}")
        }
    }
}

@Composable
fun RatingSection(employee: Employee) {
    Column {
        Text("Performance Rating", style = MaterialTheme.typography.titleMedium)

        Spacer(modifier = Modifier.height(8.dp))

        Text("Rating: ${employee.rating} ⭐")
    }
}