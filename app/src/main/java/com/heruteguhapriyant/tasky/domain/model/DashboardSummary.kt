package com.heruteguhapriyant.tasky.domain.model

data class DashboardSummary(
    val total: Int = 0,
    val completed: Int = 0,
    val pending: Int = 0,
    val overdue: Int = 0
)
