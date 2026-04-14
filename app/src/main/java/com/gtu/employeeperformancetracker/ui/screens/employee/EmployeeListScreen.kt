package com.gtu.employeeperformancetracker.ui.screens.employee

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.gtu.employeeperformancetracker.data.local.entity.Employee
import com.gtu.employeeperformancetracker.ui.navigation.Screen
import com.gtu.employeeperformancetracker.ui.navigation.sharedAuthViewModel
import com.gtu.employeeperformancetracker.utils.Roles
import com.gtu.employeeperformancetracker.viewmodel.AuthViewModel
import com.gtu.employeeperformancetracker.viewmodel.EmployeeViewModel

@Composable
fun EmployeeListScreen(
    navController: NavController,
    viewModel: EmployeeViewModel = viewModel()
) {
    val authViewModel: AuthViewModel = sharedAuthViewModel()
    val currentUser by authViewModel.currentUser.collectAsState()
    val employees by viewModel.employees.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var departmentFilter by remember { mutableStateOf("") }

    if (currentUser?.role == Roles.EMPLOYEE) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Access Restricted", style = MaterialTheme.typography.headlineSmall)
            Text(
                "Employee accounts cannot access the employee management roster.",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        return
    }

    val filteredEmployees = employees.filter { employee ->
        val matchesQuery = searchQuery.isBlank() ||
            employee.name.contains(searchQuery, ignoreCase = true) ||
            employee.role.contains(searchQuery, ignoreCase = true) ||
            employee.employeeCode.contains(searchQuery, ignoreCase = true)
        val matchesDepartment = departmentFilter.isBlank() ||
            employee.department.contains(departmentFilter, ignoreCase = true)
        matchesQuery && matchesDepartment
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate(Screen.AddEmployee.route) }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Employee")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
        ) {
            Text("Employees", style = MaterialTheme.typography.headlineMedium)
            Text(
                text = "Search the roster or filter by department.",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Search by name, role, or employee ID") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(10.dp))
            OutlinedTextField(
                value = departmentFilter,
                onValueChange = { departmentFilter = it },
                label = { Text("Filter by department") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(
                contentPadding = PaddingValues(bottom = 96.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredEmployees, key = { it.id }) { employee ->
                    EmployeeItem(
                        employee = employee,
                        onDelete = { viewModel.deleteEmployee(employee) },
                        onClick = {
                            navController.navigate(Screen.EmployeeDetail.createRoute(employee.id))
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun EmployeeItem(
    employee: Employee,
    onDelete: () -> Unit,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(employee.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text("${employee.employeeCode} | ${employee.role}", color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(employee.department, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete Employee")
            }
        }
    }
}
