package com.gtu.employeeperformancetracker.ui.screens.profile

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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.gtu.employeeperformancetracker.ui.navigation.sharedAuthViewModel
import com.gtu.employeeperformancetracker.viewmodel.AuthViewModel

@Composable
fun ProfileScreen() {
    val authViewModel: AuthViewModel = sharedAuthViewModel()
    val currentUser by authViewModel.currentUser.collectAsState()
    val currentEmployee by authViewModel.currentEmployee.collectAsState()
    val emailLogs by authViewModel.emailLogs.collectAsState()

    val personalEmails = emailLogs.filter { it.recipientEmail == currentUser?.email }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text("Profile", style = MaterialTheme.typography.headlineMedium)
        }

        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(currentUser?.displayName ?: "No active user", fontWeight = FontWeight.SemiBold)
                    Text(currentUser?.email ?: "-", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("Role: ${currentUser?.role ?: "-"}")
                    currentEmployee?.let { employee ->
                        Text("Employee ID: ${employee.employeeCode}")
                        Text("Department: ${employee.department}")
                        Text("Designation: ${employee.role}")
                    }
                    Button(
                        onClick = { authViewModel.logout() },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Logout")
                    }
                }
            }
        }

        item {
            Text("Onboarding Emails", style = MaterialTheme.typography.titleLarge)
        }

        if (personalEmails.isEmpty()) {
            item {
                Text(
                    text = "No onboarding emails found for this account.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            items(personalEmails, key = { it.id }) { email ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(email.subject, fontWeight = FontWeight.SemiBold)
                        Text(email.sentAt, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(email.body)
                    }
                }
            }
        }
    }
}
