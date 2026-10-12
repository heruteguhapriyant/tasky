package com.heruteguhapriyant.tasky.data.repository

import com.heruteguhapriyant.tasky.data.local.dao.TaskDao
import com.heruteguhapriyant.tasky.data.local.entity.TaskEntity
import com.heruteguhapriyant.tasky.domain.model.Task
import com.heruteguhapriyant.tasky.domain.model.TaskStatus
import com.heruteguhapriyant.tasky.domain.repository.TaskStatusFilter
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
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

    @Test
    fun getTasksByFilter_withSearchQueryCaseInsensitive_returnsMatchedTasks() = runBlocking {
        fakeTaskDao.tasks.add(TaskEntity(id = 1, title = "Belajar Kotlin Android", categoryId = 1))
        fakeTaskDao.tasks.add(TaskEntity(id = 2, title = "Membeli Kopi", categoryId = 2))

        val results = repository.getTasksByFilter(
            statusFilter = TaskStatusFilter.ALL,
            searchQuery = "kotlin"
        ).first()

        assertEquals(1, results.size)
        assertEquals("Belajar Kotlin Android", results[0].title)
    }

    @Test
    fun getTasksByFilter_withCategoryFilter_returnsTasksForCategory() = runBlocking {
        fakeTaskDao.tasks.add(TaskEntity(id = 1, title = "Task A", categoryId = 1))
        fakeTaskDao.tasks.add(TaskEntity(id = 2, title = "Task B", categoryId = 2))

        val results = repository.getTasksByFilter(
            statusFilter = TaskStatusFilter.ALL,
            categoryId = 2
        ).first()

        assertEquals(1, results.size)
        assertEquals("Task B", results[0].title)
    }
    @Test
    fun getDashboardSummary_withAllCompleted_returnsCorrectCounts() = runBlocking {
        fakeTaskDao.tasks.add(TaskEntity(id = 1, title = "T1", categoryId = 1, status = TaskStatus.COMPLETED))
        fakeTaskDao.tasks.add(TaskEntity(id = 2, title = "T2", categoryId = 1, status = TaskStatus.COMPLETED))

        val summary = repository.getDashboardSummary().first()

        assertEquals(2, summary.total)
        assertEquals(2, summary.completed)
        assertEquals(0, summary.pending)
        assertEquals(0, summary.overdue)
    }

    @Test
    fun getDashboardSummary_withOverdueTask_countsCorrectly() = runBlocking {
        // Task dengan dueDate di masa lalu → overdue
        val pastDueDate = System.currentTimeMillis() - (2 * 24 * 60 * 60 * 1000L)
        fakeTaskDao.tasks.add(
            TaskEntity(id = 1, title = "Overdue task", categoryId = 1,
                status = TaskStatus.TODO, dueDate = pastDueDate, hasDueTime = true)
        )
        fakeTaskDao.tasks.add(TaskEntity(id = 2, title = "Pending task", categoryId = 1, status = TaskStatus.TODO))

        val summary = repository.getDashboardSummary().first()

        assertEquals(2, summary.total)
        assertEquals(0, summary.completed)
        assertEquals(1, summary.pending)
        assertEquals(1, summary.overdue)
    }

    @Test
    fun getDashboardSummary_emptyList_returnsAllZeros() = runBlocking {
        val summary = repository.getDashboardSummary().first()

        assertEquals(0, summary.total)
        assertEquals(0, summary.completed)
        assertEquals(0, summary.pending)
        assertEquals(0, summary.overdue)
    }

    @Test
    fun getUpcomingTasks_returnsTasksSortedByDueDateWithLimit() = runBlocking {
        val futureBase = System.currentTimeMillis() + (24 * 60 * 60 * 1000L)
        // Tambahkan 4 upcoming task dengan dueDate berbeda
        fakeTaskDao.tasks.add(TaskEntity(id = 1, title = "Task C", categoryId = 1, dueDate = futureBase + 3000))
        fakeTaskDao.tasks.add(TaskEntity(id = 2, title = "Task A", categoryId = 1, dueDate = futureBase + 1000))
        fakeTaskDao.tasks.add(TaskEntity(id = 3, title = "Task B", categoryId = 1, dueDate = futureBase + 2000))
        fakeTaskDao.tasks.add(TaskEntity(id = 4, title = "Task D", categoryId = 1, dueDate = futureBase + 4000))
        // Task completed tidak boleh muncul
        fakeTaskDao.tasks.add(TaskEntity(id = 5, title = "Done", categoryId = 1,
            status = TaskStatus.COMPLETED, dueDate = futureBase + 500))

        val results = repository.getUpcomingTasks(limit = 3).first()

        assertEquals(3, results.size)
        assertEquals("Task A", results[0].title)
        assertEquals("Task B", results[1].title)
        assertEquals("Task C", results[2].title)
    }

    @Test
    fun getUpcomingTasks_excludesTasksWithoutDueDate() = runBlocking {
        val future = System.currentTimeMillis() + (24 * 60 * 60 * 1000L)
        fakeTaskDao.tasks.add(TaskEntity(id = 1, title = "Dengan DueDate", categoryId = 1, dueDate = future))
        fakeTaskDao.tasks.add(TaskEntity(id = 2, title = "Tanpa DueDate", categoryId = 1, dueDate = null))

        val results = repository.getUpcomingTasks(limit = 5).first()

        assertEquals(1, results.size)
        assertEquals("Dengan DueDate", results[0].title)
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
            (searchQuery == null || task.title.contains(searchQuery, ignoreCase = true) || (task.description?.contains(searchQuery, ignoreCase = true) == true))
        })
    }

    override fun getTasksByCategoryAndSearch(
        categoryId: Int?,
        searchQuery: String?
    ): Flow<List<TaskEntity>> {
        return flowOf(tasks.filter { task ->
            (categoryId == null || task.categoryId == categoryId) &&
            (searchQuery == null || task.title.contains(searchQuery, ignoreCase = true) || (task.description?.contains(searchQuery, ignoreCase = true) == true))
        })
    }

    override fun getUpcomingTasks(limit: Int): Flow<List<TaskEntity>> {
        return flowOf(tasks.filter { it.status != TaskStatus.COMPLETED && it.dueDate != null }
            .sortedBy { it.dueDate }
            .take(limit))
    }
}
