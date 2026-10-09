package com.heruteguhapriyant.tasky.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.heruteguhapriyant.tasky.domain.model.Priority
import com.heruteguhapriyant.tasky.domain.model.TaskStatus

@Entity(
    tableName = "tasks",
    indices = [
        Index(value = ["categoryId"]),
        Index(value = ["status"]),
        Index(value = ["dueDate"])
    ],
    foreignKeys = [
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.RESTRICT
        )
    ]
)
data class TaskEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String,
    val description: String? = null,
    val categoryId: Int,
    val status: TaskStatus = TaskStatus.TODO,
    val priority: Priority = Priority.MEDIUM,
    val dueDate: Long? = null,
    val hasDueTime: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val completedAt: Long? = null
)
