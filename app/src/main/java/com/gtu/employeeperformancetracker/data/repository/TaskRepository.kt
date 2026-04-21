package com.gtu.employeeperformancetracker.data.repository

import com.gtu.employeeperformancetracker.data.local.entity.Task
import com.gtu.employeeperformancetracker.data.remote.FirebaseStore
import com.google.firebase.firestore.DocumentSnapshot
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class TaskRepository(
    private val store: FirebaseStore = FirebaseStore()
) {

    private val tasks = store.firestore().collection(TASKS_COLLECTION)

    fun getAllTasks(): Flow<List<Task>> = store.observeCollection(tasks) { it.toTask() }

    fun getTasksByEmployee(employeeId: Int): Flow<List<Task>> = getAllTasks()
        .map { items -> items.filter { it.employeeId == employeeId } }

    suspend fun insert(task: Task) {
        val taskId = store.nextId(TASK_COUNTER_KEY)
        tasks.document(taskId.toString())
            .set(task.copy(id = taskId).toMap())
            .await()
    }

    suspend fun update(task: Task) {
        tasks.document(task.id.toString()).set(task.toMap()).await()
    }

    suspend fun delete(task: Task) {
        tasks.document(task.id.toString()).delete().await()
    }

    private fun Task.toMap(): Map<String, Any?> = mapOf(
        "id" to id,
        "employeeId" to employeeId,
        "description" to description,
        "deadline" to deadline,
        "priority" to priority,
        "assignedDate" to assignedDate,
        "status" to status
    )

    private fun DocumentSnapshot.toTask(): Task? {
        val employeeId = getLong("employeeId")?.toInt() ?: return null
        return Task(
            id = (getLong("id") ?: id.toLongOrNull() ?: 0L).toInt(),
            employeeId = employeeId,
            description = getString("description").orEmpty(),
            deadline = getString("deadline").orEmpty(),
            priority = getString("priority").orEmpty(),
            assignedDate = getString("assignedDate").orEmpty(),
            status = getString("status").orEmpty()
        )
    }

    companion object {
        private const val TASKS_COLLECTION = "tasks"
        private const val TASK_COUNTER_KEY = "tasks"
    }
}
