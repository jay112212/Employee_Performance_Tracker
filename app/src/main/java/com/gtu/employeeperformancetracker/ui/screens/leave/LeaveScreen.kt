package com.gtu.employeeperformancetracker.ui.screens.leave

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
import com.gtu.employeeperformancetracker.data.local.entity.LeaveRequest
import com.gtu.employeeperformancetracker.ui.navigation.sharedAuthViewModel
import com.gtu.employeeperformancetracker.utils.Roles
import com.gtu.employeeperformancetracker.viewmodel.AuthViewModel
import com.gtu.employeeperformancetracker.viewmodel.LeaveViewModel
import java.time.LocalDate

@Composable
fun LeaveScreen(
    leaveViewModel: LeaveViewModel = viewModel()
) {
    val authViewModel: AuthViewModel = sharedAuthViewModel()
    val currentUser by authViewModel.currentUser.collectAsState()
    val employees by authViewModel.employees.collectAsState()
    val leaveRequests by leaveViewModel.leaveRequests.collectAsState()

    var leaveType by remember { mutableStateOf("Casual Leave") }
    var startDate by remember { mutableStateOf(LocalDate.now().toString()) }
    var endDate by remember { mutableStateOf(LocalDate.now().plusDays(1).toString()) }
    var reason by remember { mutableStateOf("") }

    val visibleRequests = if (currentUser?.role == Roles.EMPLOYEE) {
        leaveRequests.filter { it.employeeId == currentUser?.employeeId }
    } else {
        leaveRequests
    }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text(
                if (currentUser?.role == Roles.EMPLOYEE) "Leave Application" else "Leave Management",
                style = MaterialTheme.typography.headlineMedium
            )
            Text(
                if (currentUser?.role == Roles.EMPLOYEE) {
                    "Apply for leave and track approval status."
                } else {
                    "Review, approve, and reject employee leave requests."
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
                        Text("Apply Leave", fontWeight = FontWeight.SemiBold)
                        OutlinedTextField(
                            value = leaveType,
                            onValueChange = { leaveType = it },
                            label = { Text("Leave Type") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = startDate,
                            onValueChange = { startDate = it },
                            label = { Text("Start Date (YYYY-MM-DD)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = endDate,
                            onValueChange = { endDate = it },
                            label = { Text("End Date (YYYY-MM-DD)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = reason,
                            onValueChange = { reason = it },
                            label = { Text("Reason") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 3
                        )
                        Button(
                            onClick = {
                                if (reason.isNotBlank()) {
                                    leaveViewModel.applyLeave(
                                        employeeId = currentUser!!.employeeId!!,
                                        leaveType = leaveType.trim(),
                                        startDate = startDate.trim(),
                                        endDate = endDate.trim(),
                                        reason = reason.trim()
                                    )
                                    reason = ""
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Submit Leave Request")
                        }
                    }
                }
            }
        }

        item {
            Text(
                if (currentUser?.role == Roles.EMPLOYEE) "My Leave Requests" else "Pending and Past Leave Requests",
                style = MaterialTheme.typography.titleLarge
            )
        }

        if (visibleRequests.isEmpty()) {
            item {
                Text("No leave requests found.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            items(visibleRequests, key = { it.id }) { request ->
                val employee = employees.find { it.id == request.employeeId }
                LeaveRequestCard(
                    request = request,
                    employeeName = employee?.name ?: "Unknown Employee",
                    showActions = currentUser?.role != Roles.EMPLOYEE,
                    onApprove = { leaveViewModel.updateStatus(request, "Approved") },
                    onReject = { leaveViewModel.updateStatus(request, "Rejected") }
                )
            }
        }
    }
}

@Composable
private fun LeaveRequestCard(
    request: LeaveRequest,
    employeeName: String,
    showActions: Boolean,
    onApprove: () -> Unit,
    onReject: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(employeeName, fontWeight = FontWeight.SemiBold)
            Text("${request.leaveType} | ${request.status}")
            Text("From ${request.startDate} to ${request.endDate}")
            Text(request.reason)
            if (showActions && request.status == "Pending") {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                    Button(onClick = onApprove, modifier = Modifier.weight(1f)) {
                        Text("Approve")
                    }
                    Button(onClick = onReject, modifier = Modifier.weight(1f)) {
                        Text("Reject")
                    }
                }
            }
        }
    }
}
