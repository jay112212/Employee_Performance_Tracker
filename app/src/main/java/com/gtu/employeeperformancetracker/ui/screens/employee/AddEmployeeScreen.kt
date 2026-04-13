package com.gtu.employeeperformancetracker.ui.screens.employee

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
    var employeeCode by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var role by remember { mutableStateOf("") }
    var department by remember { mutableStateOf("") }
    var joiningDate by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var contact by remember { mutableStateOf("") }
    var profileUri by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Add Employee",
                style = MaterialTheme.typography.headlineMedium
            )
            Text(
                text = "Capture the full employee profile defined in the PRD.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            OutlinedTextField(value = employeeCode, onValueChange = { employeeCode = it }, label = { Text("Employee ID") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Name") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = role, onValueChange = { role = it }, label = { Text("Role") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = department, onValueChange = { department = it }, label = { Text("Department") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = joiningDate, onValueChange = { joiningDate = it }, label = { Text("Joining Date (DD/MM/YYYY)") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = contact, onValueChange = { contact = it }, label = { Text("Contact Number") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = profileUri, onValueChange = { profileUri = it }, label = { Text("Profile Picture URI (optional)") }, modifier = Modifier.fillMaxWidth())

            if (error.isNotBlank()) {
                Text(text = error, color = MaterialTheme.colorScheme.error)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    if (
                        employeeCode.isBlank() ||
                        name.isBlank() ||
                        role.isBlank() ||
                        department.isBlank() ||
                        joiningDate.isBlank() ||
                        email.isBlank() ||
                        contact.isBlank()
                    ) {
                        error = "Please complete all required employee fields."
                    } else {
                        viewModel.addEmployee(
                            employeeCode = employeeCode.trim(),
                            name = name.trim(),
                            role = role.trim(),
                            department = department.trim(),
                            joiningDate = joiningDate.trim(),
                            email = email.trim(),
                            contact = contact.trim(),
                            profilePictureUri = profileUri.trim()
                        )
                        navController.popBackStack()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save Employee")
            }
        }
    }
}
