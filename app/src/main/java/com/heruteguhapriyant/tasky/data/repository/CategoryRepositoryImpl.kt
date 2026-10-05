package com.heruteguhapriyant.tasky.data.repository

import androidx.room.withTransaction
import com.heruteguhapriyant.tasky.data.local.TaskyDatabase
import com.heruteguhapriyant.tasky.data.local.dao.CategoryDao
import com.heruteguhapriyant.tasky.data.local.dao.TaskDao
import com.heruteguhapriyant.tasky.data.local.entity.CategoryEntity
import com.heruteguhapriyant.tasky.data.mapper.toDomain
import com.heruteguhapriyant.tasky.data.mapper.toEntity
import com.heruteguhapriyant.tasky.domain.model.Category
import com.heruteguhapriyant.tasky.domain.repository.CategoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class CategoryRepositoryImpl(
    private val db: TaskyDatabase,
    private val categoryDao: CategoryDao,
    private val taskDao: TaskDao
) : CategoryRepository {

    override fun getCategories(): Flow<List<Category>> {
        return categoryDao.getAllCategories().map { list ->
            list.map { it.toDomain() }
        }
    }

    override suspend fun getCategoryById(id: Int): Category? {
        return categoryDao.getCategoryById(id)?.toDomain()
    }

    override suspend fun addCategory(name: String, icon: String): Result<Long> {
        val trimmedName = name.trim()
        if (trimmedName.isEmpty() || trimmedName.length > 30) {
            return Result.failure(IllegalArgumentException("Nama kategori harus 1-30 karakter."))
        }
        val existing = categoryDao.getCategoryByName(trimmedName)
        if (existing != null) {
            return Result.failure(IllegalArgumentException("Kategori dengan nama '$trimmedName' sudah ada."))
        }
        val newCategory = CategoryEntity(
            name = trimmedName,
            icon = icon.ifEmpty { "📁" },
            isSystem = false
        )
        val id = categoryDao.insertCategory(newCategory)
        return Result.success(id)
    }

    override suspend fun updateCategory(category: Category): Result<Unit> {
        val trimmedName = category.name.trim()
        if (trimmedName.isEmpty() || trimmedName.length > 30) {
            return Result.failure(IllegalArgumentException("Nama kategori harus 1-30 karakter."))
        }
        val existing = categoryDao.getCategoryById(category.id)
            ?: return Result.failure(IllegalArgumentException("Kategori tidak ditemukan."))

        if (existing.isSystem) {
            return Result.failure(IllegalArgumentException("Kategori sistem tidak dapat diubah."))
        }

        categoryDao.updateCategory(category.toEntity().copy(name = trimmedName))
        return Result.success(Unit)
    }

    override suspend fun deleteCategory(categoryId: Int): Result<Unit> {
        val category = categoryDao.getCategoryById(categoryId)
            ?: return Result.failure(IllegalArgumentException("Kategori tidak ditemukan."))

        if (category.isSystem) {
            return Result.failure(IllegalArgumentException("Kategori sistem tidak dapat dihapus."))
        }

        val systemCategory = categoryDao.getSystemCategory()
            ?: return Result.failure(IllegalStateException("Kategori sistem 'Other' tidak ditemukan."))

        db.withTransaction {
            taskDao.moveTasksToCategory(
                fromCategoryId = categoryId,
                toCategoryId = systemCategory.id
            )
            categoryDao.deleteCategory(category)
        }

        return Result.success(Unit)
    }

    override fun getCategoryTaskCount(categoryId: Int): Flow<Int> {
        return categoryDao.getCategoryTaskCount(categoryId)
    }
}
