package com.heruteguhapriyant.tasky.ui.tasklist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.heruteguhapriyant.tasky.domain.model.Category
import com.heruteguhapriyant.tasky.domain.model.Task
import com.heruteguhapriyant.tasky.domain.model.TaskStatus
import com.heruteguhapriyant.tasky.domain.repository.CategoryRepository
import com.heruteguhapriyant.tasky.domain.repository.TaskRepository
import com.heruteguhapriyant.tasky.util.DateTimeUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class TaskFilterTab {
    ALL, TODAY, UPCOMING, COMPLETED
}

data class TaskListUiState(
    val todayTasks: List<Task> = emptyList(),
    val upcomingTasks: List<Task> = emptyList(),
    val otherTasks: List<Task> = emptyList(),
    val completedTasks: List<Task> = emptyList(),
    val categoriesMap: Map<Int, Category> = emptyMap(),
    val selectedFilter: TaskFilterTab = TaskFilterTab.ALL,
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val message: String? = null
)

@HiltViewModel
class TaskListViewModel @Inject constructor(
    private val taskRepository: TaskRepository,
    private val categoryRepository: CategoryRepository
) : ViewModel() {

    private val _selectedFilter = MutableStateFlow(TaskFilterTab.ALL)
    private val _searchQuery = MutableStateFlow("")

    val uiState: StateFlow<TaskListUiState> = combine(
        taskRepository.getTasks(),
        categoryRepository.getCategories(),
        _selectedFilter,
        _searchQuery
    ) { tasks, categories, filter, query ->
        val categoriesMap = categories.associateBy { it.id }

        // Filter by Search Query
        val filteredByQuery = if (query.isBlank()) {
            tasks
        } else {
            tasks.filter {
                it.title.contains(query, ignoreCase = true) ||
                        (it.description?.contains(query, ignoreCase = true) == true)
            }
        }

        // Filter by Tab
        val filteredByTab = when (filter) {
            TaskFilterTab.ALL -> filteredByQuery
            TaskFilterTab.TODAY -> filteredByQuery.filter {
                it.status == TaskStatus.TODO && DateTimeUtils.isToday(it.dueDate)
            }
            TaskFilterTab.UPCOMING -> filteredByQuery.filter {
                it.status == TaskStatus.TODO && DateTimeUtils.isUpcoming(it.dueDate)
            }
            TaskFilterTab.COMPLETED -> filteredByQuery.filter {
                it.status == TaskStatus.COMPLETED
            }
        }

        val today = filteredByTab.filter {
            it.status == TaskStatus.TODO && DateTimeUtils.isToday(it.dueDate)
        }
        val upcoming = filteredByTab.filter {
            it.status == TaskStatus.TODO && DateTimeUtils.isUpcoming(it.dueDate)
        }
        val others = filteredByTab.filter {
            it.status == TaskStatus.TODO && !DateTimeUtils.isToday(it.dueDate) && !DateTimeUtils.isUpcoming(it.dueDate)
        }
        val completed = filteredByTab.filter {
            it.status == TaskStatus.COMPLETED
        }

        TaskListUiState(
            todayTasks = today,
            upcomingTasks = upcoming,
            otherTasks = others,
            completedTasks = completed,
            categoriesMap = categoriesMap,
            selectedFilter = filter,
            searchQuery = query,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = TaskListUiState(isLoading = true)
    )

    fun onFilterSelected(filter: TaskFilterTab) {
        _selectedFilter.value = filter
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun toggleTaskCompletion(taskId: Int) {
        viewModelScope.launch {
            taskRepository.toggleTaskCompleted(taskId)
        }
    }
}
