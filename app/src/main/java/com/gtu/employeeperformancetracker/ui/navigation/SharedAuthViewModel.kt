package com.gtu.employeeperformancetracker.ui.navigation

import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gtu.employeeperformancetracker.viewmodel.AuthViewModel

@Composable
fun sharedAuthViewModel(): AuthViewModel {
    val activity = LocalContext.current as ComponentActivity
    return viewModel(viewModelStoreOwner = activity)
}
