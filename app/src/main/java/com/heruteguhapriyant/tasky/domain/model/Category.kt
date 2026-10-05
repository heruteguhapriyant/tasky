package com.heruteguhapriyant.tasky.domain.model

data class Category(
    val id: Int = 0,
    val name: String,
    val icon: String,
    val isSystem: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
