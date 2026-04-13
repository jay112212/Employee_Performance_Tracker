package com.gtu.employeeperformancetracker.data.repository

import com.gtu.employeeperformancetracker.data.local.dao.TaskDao
import com.gtu.employeeperformancetracker.data.local.entity.Task
import kotlinx.coroutines.flow.Flow

class TaskRepository(private val dao: TaskDao) {

    suspend fun insert(task: Task) = dao.insertTask(task)

    suspend fun update(task: Task) = dao.updateTask(task)

    suspend fun delete(task: Task) = dao.deleteTask(task)

    fun getAllTasks(): Flow<List<Task>> = dao.getAllTasks()

    fun getTasksByEmployee(employeeId: Int): Flow<List<Task>> = dao.getTasksByEmployee(employeeId)
}
