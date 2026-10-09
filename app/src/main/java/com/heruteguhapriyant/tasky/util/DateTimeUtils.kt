package com.heruteguhapriyant.tasky.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object DateTimeUtils {
    private val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    private val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    private val fullDateTimeFormat = SimpleDateFormat("MMM dd, yyyy - HH:mm", Locale.getDefault())

    fun formatTime(epochMillis: Long?): String {
        if (epochMillis == null) return ""
        return timeFormat.format(Date(epochMillis))
    }

    fun formatDate(epochMillis: Long?): String {
        if (epochMillis == null) return ""
        return dateFormat.format(Date(epochMillis))
    }

    fun formatFullDateTime(epochMillis: Long?, hasDueTime: Boolean = true): String {
        if (epochMillis == null) return "No deadline"
        return if (hasDueTime) {
            fullDateTimeFormat.format(Date(epochMillis))
        } else {
            dateFormat.format(Date(epochMillis))
        }
    }

    fun isToday(epochMillis: Long?): Boolean {
        if (epochMillis == null) return false
        val calTask = Calendar.getInstance().apply { timeInMillis = epochMillis }
        val calNow = Calendar.getInstance()
        return calTask.get(Calendar.YEAR) == calNow.get(Calendar.YEAR) &&
                calTask.get(Calendar.DAY_OF_YEAR) == calNow.get(Calendar.DAY_OF_YEAR)
    }

    fun isUpcoming(epochMillis: Long?): Boolean {
        if (epochMillis == null) return false
        val calTask = Calendar.getInstance().apply { timeInMillis = epochMillis }
        val calNow = Calendar.getInstance()
        return calTask.after(calNow) && !isToday(epochMillis)
    }

    fun isOverdue(epochMillis: Long?, hasDueTime: Boolean): Boolean {
        if (epochMillis == null) return false
        val now = System.currentTimeMillis()
        if (hasDueTime) {
            return epochMillis < now
        } else {
            val endOfDay = Calendar.getInstance().apply {
                timeInMillis = epochMillis
                set(Calendar.HOUR_OF_DAY, 23)
                set(Calendar.MINUTE, 59)
                set(Calendar.SECOND, 59)
                set(Calendar.MILLISECOND, 999)
            }.timeInMillis
            return endOfDay < now
        }
    }
}
