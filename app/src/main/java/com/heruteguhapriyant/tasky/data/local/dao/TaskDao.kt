package com.heruteguhapriyant.tasky.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.heruteguhapriyant.tasky.data.local.entity.TaskEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
    @Query("SELECT * FROM tasks ORDER BY createdAt DESC")
    fun getAllTasks(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE id = :id")
    fun getTaskById(id: Int): Flow<TaskEntity?>

    @Query("SELECT * FROM tasks WHERE id = :id")
    suspend fun getTaskByIdDirect(id: Int): TaskEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: TaskEntity): Long

    @Update
    suspend fun updateTask(task: TaskEntity)

    @Delete
    suspend fun deleteTask(task: TaskEntity)

    @Query("DELETE FROM tasks WHERE id = :id")
    suspend fun deleteTaskById(id: Int)

    @Query("UPDATE tasks SET categoryId = :toCategoryId WHERE categoryId = :fromCategoryId")
    suspend fun moveTasksToCategory(fromCategoryId: Int, toCategoryId: Int)

    @Query("SELECT COUNT(*) FROM tasks WHERE categoryId = :categoryId")
    suspend fun countTasksByCategoryId(categoryId: Int): Int

    @Query("""
        SELECT * FROM tasks
        WHERE (:status IS NULL OR status = :status)
          AND (:categoryId IS NULL OR categoryId = :categoryId)
          AND (:searchQuery IS NULL OR title LIKE '%' || :searchQuery || '%')
        ORDER BY createdAt DESC
    """)
    fun getTasksFiltered(
        status: String?,
        categoryId: Int?,
        searchQuery: String?
    ): Flow<List<TaskEntity>>
}
