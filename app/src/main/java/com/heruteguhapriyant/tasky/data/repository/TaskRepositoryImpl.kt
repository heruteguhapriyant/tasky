package com.heruteguhapriyant.tasky.data.repository

import com.heruteguhapriyant.tasky.data.local.dao.TaskDao
import com.heruteguhapriyant.tasky.data.mapper.toDomain
import com.heruteguhapriyant.tasky.data.mapper.toEntity
import com.heruteguhapriyant.tasky.domain.model.DashboardSummary
import com.heruteguhapriyant.tasky.domain.model.Task
import com.heruteguhapriyant.tasky.domain.model.TaskStatus
import com.heruteguhapriyant.tasky.domain.repository.TaskRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.Calendar

class TaskRepositoryImpl(
    private val taskDao: TaskDao
) : TaskRepository {

    override fun getTasks(
        status: TaskStatus?,
        categoryId: Int?,
        searchQuery: String?
    ): Flow<List<Task>> {
        val statusStr = status?.name
        val query = searchQuery?.trim()?.ifEmpty { null }
        return taskDao.getTasksFiltered(statusStr, categoryId, query).map { list ->
            list.map { it.toDomain() }
        }
    }

    override fun getTaskById(id: Int): Flow<Task?> {
        return taskDao.getTaskById(id).map { it?.toDomain() }
    }

    override suspend fun getTaskByIdDirect(id: Int): Task? {
        return taskDao.getTaskByIdDirect(id)?.toDomain()
    }

    override suspend fun saveTask(task: Task): Result<Long> {
        val trimmedTitle = task.title.trim()
        if (trimmedTitle.isEmpty() || trimmedTitle.length > 100) {
            return Result.failure(IllegalArgumentException("Judul task harus 1-100 karakter."))
        }
        if (task.description != null && task.description.length > 1000) {
            return Result.failure(IllegalArgumentException("Deskripsi task maksimal 1000 karakter."))
        }

        val currentTime = System.currentTimeMillis()
        val entityToSave = task.copy(
            title = trimmedTitle,
            updatedAt = currentTime
        ).toEntity()

        val id = taskDao.insertTask(entityToSave)
        return Result.success(id)
    }

    override suspend fun deleteTask(id: Int): Result<Unit> {
        taskDao.deleteTaskById(id)
        return Result.success(Unit)
    }

    override suspend fun toggleTaskCompleted(id: Int): Result<Unit> {
        val existing = taskDao.getTaskByIdDirect(id)
            ?: return Result.failure(IllegalArgumentException("Task tidak ditemukan."))

        val isCompleted = existing.status == TaskStatus.COMPLETED
        val newStatus = if (isCompleted) TaskStatus.TODO else TaskStatus.COMPLETED
        val completedAt = if (isCompleted) null else System.currentTimeMillis()

        val updated = existing.copy(
            status = newStatus,
            completedAt = completedAt,
            updatedAt = System.currentTimeMillis()
        )
        taskDao.updateTask(updated)
        return Result.success(Unit)
    }

    override fun getDashboardSummary(): Flow<DashboardSummary> {
        return taskDao.getAllTasks().map { tasks ->
            val now = System.currentTimeMillis()
            var completedCount = 0
            var overdueCount = 0
            var pendingCount = 0

            for (task in tasks) {
                if (task.status == TaskStatus.COMPLETED) {
                    completedCount++
                } else {
                    val isOverdue = task.dueDate != null && isTaskOverdue(task.dueDate, task.hasDueTime, now)
                    if (isOverdue) {
                        overdueCount++
                    } else {
                        pendingCount++
                    }
                }
            }

            val totalCount = completedCount + pendingCount + overdueCount
            DashboardSummary(
                total = totalCount,
                completed = completedCount,
                pending = pendingCount,
                overdue = overdueCount
            )
        }
    }

    private fun isTaskOverdue(dueDate: Long, hasDueTime: Boolean, currentTime: Long): Boolean {
        if (!hasDueTime) {
            val calendar = Calendar.getInstance().apply {
                timeInMillis = dueDate
                set(Calendar.HOUR_OF_DAY, 23)
                set(Calendar.MINUTE, 59)
                set(Calendar.SECOND, 59)
                set(Calendar.MILLISECOND, 999)
            }
            return currentTime > calendar.timeInMillis
        }
        return currentTime > dueDate
    }
}
