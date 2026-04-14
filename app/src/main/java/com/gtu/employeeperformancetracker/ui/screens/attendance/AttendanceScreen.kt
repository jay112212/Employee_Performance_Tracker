package com.gtu.employeeperformancetracker.ui.screens.attendance

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import com.gtu.employeeperformancetracker.ui.navigation.sharedAuthViewModel
import com.gtu.employeeperformancetracker.utils.Roles
import com.gtu.employeeperformancetracker.viewmodel.AttendanceViewModel
import com.gtu.employeeperformancetracker.viewmodel.AuthViewModel

@Composable
fun AttendanceScreen(
    attendanceViewModel: AttendanceViewModel = viewModel()
) {
    val authViewModel: AuthViewModel = sharedAuthViewModel()
    val currentUser by authViewModel.currentUser.collectAsState()
    val employees by authViewModel.employees.collectAsState()
    val attendanceRecords by attendanceViewModel.attendanceRecords.collectAsState()

    var location by remember { mutableStateOf("") }

    val visibleRecords = if (currentUser?.role == Roles.EMPLOYEE) {
        attendanceRecords.filter { it.employeeId == currentUser?.employeeId }
    } else {
        attendanceRecords
    }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text(
                if (currentUser?.role == Roles.EMPLOYEE) "My Attendance" else "Attendance Management",
                style = MaterialTheme.typography.headlineMedium
            )
            Text(
                if (currentUser?.role == Roles.EMPLOYEE) {
                    "Mark your check-in and check-out for today."
                } else {
                    "Track daily attendance and check-in activity across the workforce."
                },
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        if (currentUser?.role == Roles.EMPLOYEE && currentUser?.employeeId != null) {
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text("Mark Today", fontWeight = FontWeight.SemiBold)
                        OutlinedTextField(
                            value = location,
                            onValueChange = { location = it },
                            label = { Text("Location / Office Branch") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                            Button(
                                onClick = {
                                    attendanceViewModel.checkIn(
                                        employeeId = currentUser!!.employeeId!!,
                                        location = location,
                                        status = "Present"
                                    )
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Check In")
                            }
                            Button(
                                onClick = {
                                    attendanceViewModel.checkOut(currentUser!!.employeeId!!)
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Check Out")
                            }
                        }
                    }
                }
            }
        }

        item {
            Text(
                if (currentUser?.role == Roles.EMPLOYEE) "Attendance History" else "Team Attendance",
                style = MaterialTheme.typography.titleLarge
            )
        }

        if (visibleRecords.isEmpty()) {
            item {
                Text("No attendance records found.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            items(visibleRecords, key = { it.id }) { record ->
                val employee = employees.find { it.id == record.employeeId }
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(employee?.name ?: "Unknown Employee", fontWeight = FontWeight.SemiBold)
                        Text("Date: ${record.date}")
                        Text("Status: ${record.status}")
                        Text("Check In: ${record.checkInTime ?: "-"}")
                        Text("Check Out: ${record.checkOutTime ?: "-"}")
                        Text("Location: ${record.location ?: "-"}")
                    }
                }
            }
        }
    }
}
