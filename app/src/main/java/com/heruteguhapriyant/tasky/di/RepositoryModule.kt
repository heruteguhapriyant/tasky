package com.heruteguhapriyant.tasky.di

import com.heruteguhapriyant.tasky.data.local.TaskyDatabase
import com.heruteguhapriyant.tasky.data.local.dao.CategoryDao
import com.heruteguhapriyant.tasky.data.local.dao.TaskDao
import com.heruteguhapriyant.tasky.data.repository.CategoryRepositoryImpl
import com.heruteguhapriyant.tasky.data.repository.TaskRepositoryImpl
import com.heruteguhapriyant.tasky.domain.repository.CategoryRepository
import com.heruteguhapriyant.tasky.domain.repository.TaskRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideCategoryRepository(
        db: TaskyDatabase,
        categoryDao: CategoryDao,
        taskDao: TaskDao
    ): CategoryRepository {
        return CategoryRepositoryImpl(db, categoryDao, taskDao)
    }

    @Provides
    @Singleton
    fun provideTaskRepository(
        taskDao: TaskDao
    ): TaskRepository {
        return TaskRepositoryImpl(taskDao)
    }
}
