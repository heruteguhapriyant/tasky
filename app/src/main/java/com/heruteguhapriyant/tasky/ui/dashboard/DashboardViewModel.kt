package com.heruteguhapriyant.tasky.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.heruteguhapriyant.tasky.domain.model.Category
import com.heruteguhapriyant.tasky.domain.model.DashboardSummary
import com.heruteguhapriyant.tasky.domain.model.Task
import com.heruteguhapriyant.tasky.domain.repository.CategoryRepository
import com.heruteguhapriyant.tasky.domain.repository.TaskRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
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

sealed class DashboardUiEvent {
    data class ShowSnackbar(val message: String) : DashboardUiEvent()
}

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val taskRepository: TaskRepository,
    private val categoryRepository: CategoryRepository
) : ViewModel() {

    private val _eventFlow = MutableSharedFlow<DashboardUiEvent>()
    val eventFlow: SharedFlow<DashboardUiEvent> = _eventFlow.asSharedFlow()

    val uiState: StateFlow<DashboardUiState> = combine(
        taskRepository.getDashboardSummary(),
        taskRepository.getUpcomingTasks(5),
        categoryRepository.getCategories()
    ) { summary, upcomingTasks, categories ->
        val categoriesMap = categories.associateBy { it.id }

        DashboardUiState(
            summary = summary,
            upcomingTasks = upcomingTasks,
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
            try {
                val result = taskRepository.toggleTaskCompleted(taskId)
                if (result.isFailure) {
                    _eventFlow.emit(
                        DashboardUiEvent.ShowSnackbar(
                            result.exceptionOrNull()?.message ?: "Gagal memperbarui status tugas"
                        )
                    )
                }
            } catch (e: Exception) {
                _eventFlow.emit(DashboardUiEvent.ShowSnackbar("Terjadi kesalahan: ${e.message}"))
            }
        }
    }
}
