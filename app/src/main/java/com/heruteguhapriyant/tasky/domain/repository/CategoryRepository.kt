package com.heruteguhapriyant.tasky.domain.repository

import com.heruteguhapriyant.tasky.domain.model.Category
import kotlinx.coroutines.flow.Flow

interface CategoryRepository {
    fun getCategories(): Flow<List<Category>>
    suspend fun getCategoryById(id: Int): Category?
    suspend fun addCategory(name: String, icon: String): Result<Long>
    suspend fun updateCategory(category: Category): Result<Unit>
    suspend fun deleteCategory(categoryId: Int): Result<Unit>
    fun getCategoryTaskCount(categoryId: Int): Flow<Int>
}
