package com.heruteguhapriyant.tasky.data.repository

import com.heruteguhapriyant.tasky.data.local.dao.TaskDao
import com.heruteguhapriyant.tasky.data.local.entity.TaskEntity
import com.heruteguhapriyant.tasky.domain.model.Task
import com.heruteguhapriyant.tasky.domain.model.TaskStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class TaskRepositoryTest {

    private lateinit var fakeTaskDao: FakeTaskDao
    private lateinit var repository: TaskRepositoryImpl

    @Before
    fun setUp() {
        fakeTaskDao = FakeTaskDao()
        repository = TaskRepositoryImpl(fakeTaskDao)
    }

    @Test
    fun saveTask_withValidData_insertsTaskSuccessfully() = runBlocking {
        val task = Task(title = "Belajar Kotlin", categoryId = 1)
        val result = repository.saveTask(task)

        assertTrue(result.isSuccess)
        assertEquals(1, fakeTaskDao.tasks.size)
        assertEquals("Belajar Kotlin", fakeTaskDao.tasks[0].title)
    }

    @Test
    fun saveTask_withEmptyTitle_returnsFailure() = runBlocking {
        val task = Task(title = "   ", categoryId = 1)
        val result = repository.saveTask(task)

        assertTrue(result.isFailure)
        assertEquals(0, fakeTaskDao.tasks.size)
    }

    @Test
    fun deleteTask_storesTaskInMemoryAndRemovesFromDao() = runBlocking {
        val entity = TaskEntity(id = 10, title = "Task to delete", categoryId = 1)
        fakeTaskDao.tasks.add(entity)

        val deleteResult = repository.deleteTask(10)
        assertTrue(deleteResult.isSuccess)
        assertEquals(0, fakeTaskDao.tasks.size)
        assertNotNull(repository.getRecentlyDeletedTask())
        assertEquals("Task to delete", repository.getRecentlyDeletedTask()?.title)
    }

    @Test
    fun restoreRecentlyDeletedTask_restoresTaskSuccessfully() = runBlocking {
        val entity = TaskEntity(id = 10, title = "Task to restore", categoryId = 1)
        fakeTaskDao.tasks.add(entity)

        repository.deleteTask(10)
        assertEquals(0, fakeTaskDao.tasks.size)

        val restoreResult = repository.restoreRecentlyDeletedTask()
        assertTrue(restoreResult.isSuccess)
        assertEquals(1, fakeTaskDao.tasks.size)
        assertEquals("Task to restore", fakeTaskDao.tasks[0].title)
        assertNull(repository.getRecentlyDeletedTask())
    }

    @Test
    fun toggleTaskCompleted_changesStatusFromTodoToCompleted() = runBlocking {
        val entity = TaskEntity(id = 5, title = "Task todo", categoryId = 1, status = TaskStatus.TODO)
        fakeTaskDao.tasks.add(entity)

        val result = repository.toggleTaskCompleted(5)
        assertTrue(result.isSuccess)
        assertEquals(TaskStatus.COMPLETED, fakeTaskDao.tasks[0].status)
        assertNotNull(fakeTaskDao.tasks[0].completedAt)
    }
}

class FakeTaskDao : TaskDao {
    val tasks = mutableListOf<TaskEntity>()
    private var idCounter = 1

    override fun getAllTasks(): Flow<List<TaskEntity>> = flowOf(tasks)

    override fun getTaskById(id: Int): Flow<TaskEntity?> = flowOf(tasks.find { it.id == id })

    override suspend fun getTaskByIdDirect(id: Int): TaskEntity? = tasks.find { it.id == id }

    override suspend fun insertTask(task: TaskEntity): Long {
        val id = if (task.id == 0) idCounter++ else task.id
        val newEntity = task.copy(id = id)
        tasks.removeAll { it.id == id }
        tasks.add(newEntity)
        return id.toLong()
    }

    override suspend fun updateTask(task: TaskEntity) {
        tasks.removeAll { it.id == task.id }
        tasks.add(task)
    }

    override suspend fun deleteTask(task: TaskEntity) {
        tasks.removeAll { it.id == task.id }
    }

    override suspend fun deleteTaskById(id: Int) {
        tasks.removeAll { it.id == id }
    }

    override suspend fun moveTasksToCategory(fromCategoryId: Int, toCategoryId: Int) {
        val updated = tasks.map {
            if (it.categoryId == fromCategoryId) it.copy(categoryId = toCategoryId) else it
        }
        tasks.clear()
        tasks.addAll(updated)
    }

    override suspend fun countTasksByCategoryId(categoryId: Int): Int {
        return tasks.count { it.categoryId == categoryId }
    }

    override fun getTasksFiltered(
        status: String?,
        categoryId: Int?,
        searchQuery: String?
    ): Flow<List<TaskEntity>> {
        return flowOf(tasks.filter { task ->
            (status == null || task.status.name == status) &&
            (categoryId == null || task.categoryId == categoryId) &&
            (searchQuery == null || task.title.contains(searchQuery, ignoreCase = true))
        })
    }
}
