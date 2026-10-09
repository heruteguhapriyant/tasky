package com.heruteguhapriyant.tasky.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.heruteguhapriyant.tasky.domain.model.Category
import com.heruteguhapriyant.tasky.domain.model.DashboardSummary
import com.heruteguhapriyant.tasky.domain.model.Task
import com.heruteguhapriyant.tasky.domain.model.TaskStatus
import com.heruteguhapriyant.tasky.domain.repository.CategoryRepository
import com.heruteguhapriyant.tasky.domain.repository.TaskRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DashboardUiState(
    val summary: DashboardSummary = DashboardSummary(),
    val upcomingTasks: List<Task> = emptyList(),
    val categoriesMap: Map<Int, Category> = emptyMap(),
    val isLoading: Boolean = false
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val taskRepository: TaskRepository,
    private val categoryRepository: CategoryRepository
) : ViewModel() {

    val uiState: StateFlow<DashboardUiState> = combine(
        taskRepository.getDashboardSummary(),
        taskRepository.getTasks(),
        categoryRepository.getCategories()
    ) { summary, tasks, categories ->
        val categoriesMap = categories.associateBy { it.id }
        val upcoming = tasks
            .filter { it.status == TaskStatus.TODO && it.dueDate != null }
            .sortedBy { it.dueDate }
            .take(5)

        DashboardUiState(
            summary = summary,
            upcomingTasks = upcoming,
            categoriesMap = categoriesMap,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DashboardUiState(isLoading = true)
    )

    fun toggleTaskCompletion(taskId: Int) {
        viewModelScope.launch {
            taskRepository.toggleTaskCompleted(taskId)
        }
    }
}
