package com.gtu.employeeperformancetracker.ui.screens.employee

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.gtu.employeeperformancetracker.viewmodel.EmployeeViewModel

@Composable
fun AddEmployeeScreen(
    navController: NavController,
    viewModel: EmployeeViewModel = viewModel()
) {

    var name by remember { mutableStateOf("") }
    var role by remember { mutableStateOf("") }
    var dept by remember { mutableStateOf("") }

    var error by remember { mutableStateOf("") }

    Scaffold { padding ->

        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
        ) {

            Text(
                text = "Add Employee",
                style = MaterialTheme.typography.headlineMedium
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 🔹 Name
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Employee Name") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(10.dp))

            // 🔹 Role
            OutlinedTextField(
                value = role,
                onValueChange = { role = it },
                label = { Text("Role") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(10.dp))

            // 🔹 Department
            OutlinedTextField(
                value = dept,
                onValueChange = { dept = it },
                label = { Text("Department") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // ❌ Error Message
            if (error.isNotEmpty()) {
                Text(text = error, color = MaterialTheme.colorScheme.error)
                Spacer(modifier = Modifier.height(8.dp))
            }

            // ✅ Save Button
            Button(
                onClick = {
                    if (name.isBlank() || role.isBlank() || dept.isBlank()) {
                        error = "All fields are required"
                    } else {
                        viewModel.addEmployee(name, role, dept)
                        navController.popBackStack() // go back after save
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save Employee")
            }
        }
    }
}