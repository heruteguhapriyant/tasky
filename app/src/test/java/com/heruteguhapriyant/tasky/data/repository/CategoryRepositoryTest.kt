package com.heruteguhapriyant.tasky.data.repository

import com.heruteguhapriyant.tasky.data.local.dao.CategoryDao
import com.heruteguhapriyant.tasky.data.local.dao.CategoryWithTaskCountTuple
import com.heruteguhapriyant.tasky.data.local.dao.TaskDao
import com.heruteguhapriyant.tasky.data.local.entity.CategoryEntity
import com.heruteguhapriyant.tasky.data.local.entity.TaskEntity
import com.heruteguhapriyant.tasky.domain.model.Category
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class CategoryRepositoryTest {

    private lateinit var fakeCategoryDao: FakeCategoryDao
    private lateinit var fakeTaskDao: FakeTaskDao
    private lateinit var repository: CategoryRepositoryImpl

    @Before
    fun setUp() {
        fakeCategoryDao = FakeCategoryDao()
        fakeTaskDao = FakeTaskDao()

        // Setup default system category "Other"
        fakeCategoryDao.categories.add(
            CategoryEntity(id = 99, name = "Other", icon = "📁", isSystem = true)
        )

        repository = CategoryRepositoryImpl(null, fakeCategoryDao, fakeTaskDao)
    }

    @Test
    fun addCategory_withValidName_insertsSuccessfully() = runBlocking {
        val result = repository.addCategory("Belajar", "📚")
        assertTrue(result.isSuccess)
        assertEquals(2, fakeCategoryDao.categories.size)
        assertEquals("Belajar", fakeCategoryDao.categories.last().name)
    }

    @Test
    fun addCategory_withDuplicateNameCaseInsensitive_returnsFailure() = runBlocking {
        repository.addCategory("Work", "💼")
        val duplicateResult = repository.addCategory("work", "💼")

        assertTrue(duplicateResult.isFailure)
        assertEquals(2, fakeCategoryDao.categories.size) // Only "Other" + 1 "Work"
    }

    @Test
    fun addCategory_withNameOver30Chars_returnsFailure() = runBlocking {
        val longName = "A".repeat(31)
        val result = repository.addCategory(longName, "📁")

        assertTrue(result.isFailure)
    }

    @Test
    fun updateCategory_systemCategory_returnsFailure() = runBlocking {
        val systemCategory = Category(id = 99, name = "New Name", icon = "📁", isSystem = true)
        val result = repository.updateCategory(systemCategory)

        assertTrue(result.isFailure)
    }

    @Test
    fun updateCategory_normalCategory_updatesSuccessfully() = runBlocking {
        val id = repository.addCategory("Music", "🎵").getOrThrow().toInt()
        val toUpdate = Category(id = id, name = "Audio", icon = "🎧", isSystem = false)

        val result = repository.updateCategory(toUpdate)
        assertTrue(result.isSuccess)
        assertEquals("Audio", fakeCategoryDao.getCategoryById(id)?.name)
    }

    @Test
    fun deleteCategory_systemCategory_returnsFailure() = runBlocking {
        val result = repository.deleteCategory(99)
        assertTrue(result.isFailure)
    }

    @Test
    fun deleteCategory_normalCategory_movesTasksToOtherAndDeletes() = runBlocking {
        val catId = repository.addCategory("Project", "🚀").getOrThrow().toInt()

        // Add 2 tasks under "Project" category
        fakeTaskDao.tasks.add(TaskEntity(id = 1, title = "Task 1", categoryId = catId))
        fakeTaskDao.tasks.add(TaskEntity(id = 2, title = "Task 2", categoryId = catId))

        assertEquals(2, repository.getAffectedTaskCount(catId))

        val deleteResult = repository.deleteCategory(catId)
        assertTrue(deleteResult.isSuccess)

        // Verify tasks moved to category 99 ("Other")
        assertEquals(99, fakeTaskDao.tasks[0].categoryId)
        assertEquals(99, fakeTaskDao.tasks[1].categoryId)
        assertEquals(null, fakeCategoryDao.getCategoryById(catId))
    }
}

class FakeCategoryDao : CategoryDao {
    val categories = mutableListOf<CategoryEntity>()
    private var idCounter = 1

    override fun getAllCategories(): Flow<List<CategoryEntity>> = flowOf(categories)

    override fun getCategoriesWithTaskCount(): Flow<List<CategoryWithTaskCountTuple>> {
        return flowOf(categories.map {
            CategoryWithTaskCountTuple(
                id = it.id,
                name = it.name,
                icon = it.icon,
                isSystem = it.isSystem,
                createdAt = it.createdAt,
                taskCount = 0
            )
        })
    }

    override suspend fun getCategoryById(id: Int): CategoryEntity? = categories.find { it.id == id }

    override suspend fun getCategoryByName(name: String): CategoryEntity? {
        return categories.find { it.name.equals(name, ignoreCase = true) }
    }

    override suspend fun getSystemCategory(): CategoryEntity? = categories.find { it.isSystem }

    override suspend fun insertCategory(category: CategoryEntity): Long {
        val id = if (category.id == 0) idCounter++ else category.id
        val newEntity = category.copy(id = id)
        categories.removeAll { it.id == id }
        categories.add(newEntity)
        return id.toLong()
    }

    override suspend fun insertCategories(categories: List<CategoryEntity>) {
        this.categories.addAll(categories)
    }

    override suspend fun updateCategory(category: CategoryEntity) {
        categories.removeAll { it.id == category.id }
        categories.add(category)
    }

    override suspend fun deleteCategory(category: CategoryEntity) {
        categories.removeAll { it.id == category.id }
    }

    override fun getCategoryTaskCount(categoryId: Int): Flow<Int> = flowOf(0)

    override suspend fun countTasksByCategoryId(categoryId: Int): Int {
        return 2 // returns count of affected tasks
    }

    override suspend fun getCategoryCount(): Int = categories.size
}
