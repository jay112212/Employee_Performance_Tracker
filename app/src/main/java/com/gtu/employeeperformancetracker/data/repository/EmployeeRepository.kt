package com.gtu.employeeperformancetracker.data.repository

import com.gtu.employeeperformancetracker.data.local.entity.Employee
import com.gtu.employeeperformancetracker.data.remote.FirebaseStore
import com.google.firebase.firestore.DocumentSnapshot
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class EmployeeRepository(
    private val store: FirebaseStore = FirebaseStore()
) {

    private val employees = store.firestore().collection(EMPLOYEES_COLLECTION)

    fun getAllEmployees(): Flow<List<Employee>> = store.observeCollection(
        employees
    ) { it.toEmployee() }

    fun getEmployeeById(id: Int): Flow<Employee?> = getAllEmployees()
        .map { employeeList -> employeeList.find { it.id == id } }

    suspend fun insert(employee: Employee): Long {
        val employeeId = store.nextId(EMPLOYEE_COUNTER_KEY)
        employees.document(employeeId.toString())
            .set(employee.copy(id = employeeId).toMap())
            .await()
        return employeeId.toLong()
    }

    suspend fun update(employee: Employee) {
        employees.document(employee.id.toString())
            .set(employee.toMap())
            .await()
    }

    suspend fun delete(employee: Employee) {
        employees.document(employee.id.toString()).delete().await()
    }

    private fun Employee.toMap(): Map<String, Any?> = mapOf(
        "id" to id,
        "employeeCode" to employeeCode,
        "name" to name,
        "role" to role,
        "department" to department,
        "joiningDate" to joiningDate,
        "email" to email,
        "contact" to contact,
        "profilePictureUri" to profilePictureUri
    )

    private fun DocumentSnapshot.toEmployee(): Employee? {
        val employeeCode = getString("employeeCode") ?: return null
        return Employee(
            id = (getLong("id") ?: id.toLongOrNull() ?: 0L).toInt(),
            employeeCode = employeeCode,
            name = getString("name").orEmpty(),
            role = getString("role").orEmpty(),
            department = getString("department").orEmpty(),
            joiningDate = getString("joiningDate").orEmpty(),
            email = getString("email").orEmpty(),
            contact = getString("contact").orEmpty(),
            profilePictureUri = getString("profilePictureUri")
        )
    }

    companion object {
        private const val EMPLOYEES_COLLECTION = "employees"
        private const val EMPLOYEE_COUNTER_KEY = "employees"
    }
}
