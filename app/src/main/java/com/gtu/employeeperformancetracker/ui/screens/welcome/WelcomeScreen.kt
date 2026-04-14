package com.gtu.employeeperformancetracker.ui.screens.welcome

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.gtu.employeeperformancetracker.ui.navigation.sharedAuthViewModel
import com.gtu.employeeperformancetracker.utils.LoginModes
import com.gtu.employeeperformancetracker.viewmodel.AuthViewModel

@Composable
fun WelcomeScreen(
    navController: NavController
) {
    val authViewModel: AuthViewModel = sharedAuthViewModel()
    val loginError by authViewModel.loginError.collectAsState()

    var selectedMode by remember { mutableStateOf(LoginModes.ADMIN_HR) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    listOf(
                        MaterialTheme.colorScheme.primary,
                        MaterialTheme.colorScheme.secondary
                    )
                )
            )
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f)
        ) {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "Employee Performance Tracker",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Login with your assigned account. Employees are onboarded only by Admin or HR.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    ModeCard(
                        title = "Admin / HR",
                        selected = selectedMode == LoginModes.ADMIN_HR,
                        modifier = Modifier.weight(1f)
                    ) {
                        selectedMode = LoginModes.ADMIN_HR
                        email = ""
                        password = ""
                        authViewModel.clearLoginError()
                    }
                    ModeCard(
                        title = "Employee",
                        selected = selectedMode == LoginModes.EMPLOYEE,
                        modifier = Modifier.weight(1f)
                    ) {
                        selectedMode = LoginModes.EMPLOYEE
                        email = ""
                        password = ""
                        authViewModel.clearLoginError()
                    }
                }

                OutlinedTextField(
                    value = email,
                    onValueChange = {
                        email = it
                        authViewModel.clearLoginError()
                    },
                    label = { Text("Email") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = password,
                    onValueChange = {
                        password = it
                        authViewModel.clearLoginError()
                    },
                    label = { Text("Password") },
                    modifier = Modifier.fillMaxWidth()
                )

                if (loginError != null) {
                    Text(loginError.orEmpty(), color = MaterialTheme.colorScheme.error)
                }

                Button(
                    onClick = {
                        authViewModel.login(
                            email = email,
                            password = password,
                            mode = selectedMode
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Login")
                }
                Text(
                    text = if (selectedMode == LoginModes.EMPLOYEE) {
                        "Use the email and temporary password sent during onboarding."
                    } else {
                        "Use your assigned management account credentials."
                    },
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun ModeCard(
    title: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier,
        onClick = onClick
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(title, fontWeight = FontWeight.SemiBold)
            Text(
                if (selected) "Selected" else "Tap to switch",
                color = if (selected) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
        }
    }
}
