package com.heruteguhapriyant.tasky.domain.model

data class Task(
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
