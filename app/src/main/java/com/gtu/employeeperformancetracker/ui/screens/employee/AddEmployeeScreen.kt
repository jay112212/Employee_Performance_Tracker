package com.gtu.employeeperformancetracker.ui.screens.employee

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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.gtu.employeeperformancetracker.ui.navigation.sharedAuthViewModel
import com.gtu.employeeperformancetracker.utils.Roles
import com.gtu.employeeperformancetracker.viewmodel.AuthViewModel

@Composable
fun AddEmployeeScreen(
    navController: NavController
) {
    val authViewModel: AuthViewModel = sharedAuthViewModel()
    val currentUser by authViewModel.currentUser.collectAsState()
    val onboardingNotice by authViewModel.onboardingNotice.collectAsState()

    if (currentUser?.role == Roles.EMPLOYEE) {
        AccessDeniedText("Only Admin and HR can register employees.")
        return
    }

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
            Text("Register Employee", style = MaterialTheme.typography.headlineMedium)
            Text(
                "Admin / HR creates employee accounts. A mock onboarding email with credentials is generated automatically.",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            OutlinedTextField(value = employeeCode, onValueChange = { employeeCode = it }, label = { Text("Employee ID") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Name") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = role, onValueChange = { role = it }, label = { Text("Designation") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = department, onValueChange = { department = it }, label = { Text("Department") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = joiningDate, onValueChange = { joiningDate = it }, label = { Text("Joining Date (DD/MM/YYYY)") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Official Email") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = contact, onValueChange = { contact = it }, label = { Text("Contact Number") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = profileUri, onValueChange = { profileUri = it }, label = { Text("Profile Picture URI (optional)") }, modifier = Modifier.fillMaxWidth())

            if (error.isNotBlank()) {
                Text(error, color = MaterialTheme.colorScheme.error)
            }

            if (onboardingNotice != null) {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = onboardingNotice.orEmpty(),
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }

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
                        error = "Please complete all required fields."
                    } else {
                        error = ""
                        authViewModel.onboardEmployee(
                            employeeCode = employeeCode,
                            name = name,
                            role = role,
                            department = department,
                            joiningDate = joiningDate,
                            email = email,
                            contact = contact,
                            profilePictureUri = profileUri
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Create Employee Account")
            }

            Button(
                onClick = {
                    authViewModel.clearOnboardingNotice()
                    navController.popBackStack()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Back")
            }
        }
    }
}

@Composable
private fun AccessDeniedText(message: String) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text("Access Restricted", style = MaterialTheme.typography.headlineSmall)
        Text(message, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
