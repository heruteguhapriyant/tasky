package com.heruteguhapriyant.tasky.domain.repository

import com.heruteguhapriyant.tasky.domain.model.DashboardSummary
import com.heruteguhapriyant.tasky.domain.model.Task
import com.heruteguhapriyant.tasky.domain.model.TaskStatus
import kotlinx.coroutines.flow.Flow

interface TaskRepository {
    fun getTasks(status: TaskStatus? = null, categoryId: Int? = null, searchQuery: String? = null): Flow<List<Task>>
    fun getTaskById(id: Int): Flow<Task?>
    suspend fun getTaskByIdDirect(id: Int): Task?
    suspend fun saveTask(task: Task): Result<Long>
    suspend fun deleteTask(id: Int): Result<Unit>
    suspend fun toggleTaskCompleted(id: Int): Result<Unit>
    fun getDashboardSummary(): Flow<DashboardSummary>
}
