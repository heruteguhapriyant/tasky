package com.heruteguhapriyant.tasky.ui.taskdetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.heruteguhapriyant.tasky.domain.model.Category
import com.heruteguhapriyant.tasky.domain.model.Task
import com.heruteguhapriyant.tasky.domain.repository.CategoryRepository
import com.heruteguhapriyant.tasky.domain.repository.TaskRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TaskDetailUiState(
    val task: Task? = null,
    val category: Category? = null,
    val isLoading: Boolean = true,
    val showDeleteDialog: Boolean = false
)

sealed class TaskDetailUiEvent {
    data object TaskDeleted : TaskDetailUiEvent()
    data class ShowError(val message: String) : TaskDetailUiEvent()
}

@HiltViewModel
class TaskDetailViewModel @Inject constructor(
    private val taskRepository: TaskRepository,
    private val categoryRepository: CategoryRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val taskId: Int = checkNotNull(savedStateHandle.get<String>("taskId")?.toIntOrNull())

    private val _uiState = MutableStateFlow(TaskDetailUiState())
    val uiState: StateFlow<TaskDetailUiState> = _uiState.asStateFlow()

    private val _eventFlow = MutableSharedFlow<TaskDetailUiEvent>()
    val eventFlow: SharedFlow<TaskDetailUiEvent> = _eventFlow.asSharedFlow()

    // Cache recently deleted task for Undo support in memory
    private var recentlyDeletedTask: Task? = null

    init {
        loadTaskDetail()
    }

    private fun loadTaskDetail() {
        viewModelScope.launch {
            taskRepository.getTaskById(taskId).collect { task ->
                if (task != null) {
                    val category = categoryRepository.getCategoryById(task.categoryId)
                    _uiState.update {
                        it.copy(
                            task = task,
                            category = category,
                            isLoading = false
                        )
                    }
                } else {
                    _uiState.update { it.copy(isLoading = false) }
                }
            }
        }
    }

    fun onDeleteClicked() {
        _uiState.update { it.copy(showDeleteDialog = true) }
    }

    fun onDismissDeleteDialog() {
        _uiState.update { it.copy(showDeleteDialog = false) }
    }

    fun onConfirmDelete() {
        val currentTask = _uiState.value.task ?: return
        recentlyDeletedTask = currentTask
        _uiState.update { it.copy(showDeleteDialog = false) }

        viewModelScope.launch {
            val result = taskRepository.deleteTask(currentTask.id)
            if (result.isSuccess) {
                _eventFlow.emit(TaskDetailUiEvent.TaskDeleted)
            } else {
                _eventFlow.emit(TaskDetailUiEvent.ShowError("Gagal menghapus task"))
            }
        }
    }

    fun undoDelete() {
        viewModelScope.launch {
            taskRepository.restoreRecentlyDeletedTask()
        }
    }
}
