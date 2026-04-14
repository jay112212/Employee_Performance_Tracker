package com.gtu.employeeperformancetracker.ui.screens.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.gtu.employeeperformancetracker.ui.navigation.sharedAuthViewModel
import com.gtu.employeeperformancetracker.viewmodel.AuthViewModel

@Composable
fun ChangePasswordScreen() {
    val authViewModel: AuthViewModel = sharedAuthViewModel()
    val currentUser by authViewModel.currentUser.collectAsState()
    val passwordChangeError by authViewModel.passwordChangeError.collectAsState()

    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "Change Password",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Welcome ${currentUser?.displayName.orEmpty()}. You must change your temporary password before using the app.",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            OutlinedTextField(
                value = newPassword,
                onValueChange = {
                    newPassword = it
                    authViewModel.clearPasswordChangeError()
                },
                label = { Text("New Password") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = confirmPassword,
                onValueChange = {
                    confirmPassword = it
                    authViewModel.clearPasswordChangeError()
                },
                label = { Text("Confirm Password") },
                modifier = Modifier.fillMaxWidth()
            )
            if (passwordChangeError != null) {
                Text(passwordChangeError.orEmpty(), color = MaterialTheme.colorScheme.error)
            }
            Button(
                onClick = {
                    authViewModel.changePassword(
                        newPassword = newPassword,
                        confirmPassword = confirmPassword
                    )
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save New Password")
            }
        }
    }
}
