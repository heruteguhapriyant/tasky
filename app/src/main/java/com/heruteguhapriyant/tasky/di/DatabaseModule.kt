package com.heruteguhapriyant.tasky.di

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.heruteguhapriyant.tasky.data.local.TaskyDatabase
import com.heruteguhapriyant.tasky.data.local.dao.CategoryDao
import com.heruteguhapriyant.tasky.data.local.dao.TaskDao
import com.heruteguhapriyant.tasky.data.local.entity.CategoryEntity
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Provider
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideTaskyDatabase(
        @ApplicationContext context: Context,
        categoryDaoProvider: Provider<CategoryDao>
    ): TaskyDatabase {
        return Room.databaseBuilder(
            context,
            TaskyDatabase::class.java,
            "tasky.db"
        ).addCallback(object : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                CoroutineScope(Dispatchers.IO).launch {
                    val defaultCategories = listOf(
                        CategoryEntity(name = "Study", icon = "📚", isSystem = false),
                        CategoryEntity(name = "Work", icon = "💼", isSystem = false),
                        CategoryEntity(name = "Programming", icon = "💻", isSystem = false),
                        CategoryEntity(name = "Personal", icon = "👤", isSystem = false),
                        CategoryEntity(name = "Finance", icon = "💰", isSystem = false),
                        CategoryEntity(name = "Health", icon = "🏥", isSystem = false),
                        CategoryEntity(name = "Other", icon = "📁", isSystem = true)
                    )
                    categoryDaoProvider.get().insertCategories(defaultCategories)
                }
            }
        }).build()
    }

    @Provides
    fun provideCategoryDao(database: TaskyDatabase): CategoryDao = database.categoryDao()

    @Provides
    fun provideTaskDao(database: TaskyDatabase): TaskDao = database.taskDao()
}
