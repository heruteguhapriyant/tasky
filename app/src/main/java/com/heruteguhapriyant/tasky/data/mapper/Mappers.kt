package com.heruteguhapriyant.tasky.data.mapper

import com.heruteguhapriyant.tasky.data.local.entity.CategoryEntity
import com.heruteguhapriyant.tasky.data.local.entity.TaskEntity
import com.heruteguhapriyant.tasky.domain.model.Category
import com.heruteguhapriyant.tasky.domain.model.Task

fun CategoryEntity.toDomain(): Category = Category(
    id = id,
    name = name,
    icon = icon,
    isSystem = isSystem,
    createdAt = createdAt
)

fun Category.toEntity(): CategoryEntity = CategoryEntity(
    id = id,
    name = name,
    icon = icon,
    isSystem = isSystem,
    createdAt = createdAt
)

fun TaskEntity.toDomain(): Task = Task(
    id = id,
    title = title,
    description = description,
    categoryId = categoryId,
    status = status,
    priority = priority,
    dueDate = dueDate,
    hasDueTime = hasDueTime,
    createdAt = createdAt,
    updatedAt = updatedAt,
    completedAt = completedAt
)

fun Task.toEntity(): TaskEntity = TaskEntity(
    id = id,
    title = title,
    description = description,
    categoryId = categoryId,
    status = status,
    priority = priority,
    dueDate = dueDate,
    hasDueTime = hasDueTime,
    createdAt = createdAt,
    updatedAt = updatedAt,
    completedAt = completedAt
)
