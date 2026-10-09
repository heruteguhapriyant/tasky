package com.heruteguhapriyant.tasky.ui.taskform

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.heruteguhapriyant.tasky.domain.model.Category
import com.heruteguhapriyant.tasky.domain.model.Priority
import com.heruteguhapriyant.tasky.domain.model.Task
import com.heruteguhapriyant.tasky.domain.model.TaskStatus
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

data class TaskFormUiState(
    val taskId: Int? = null,
    val isEditMode: Boolean = false,
    val title: String = "",
    val description: String = "",
    val selectedCategory: Category? = null,
    val availableCategories: List<Category> = emptyList(),
    val priority: Priority = Priority.MEDIUM,
    val dueDate: Long? = null,
    val hasDueTime: Boolean = false,
    val status: TaskStatus = TaskStatus.TODO,
    val taskType: String = "Task",
    val titleError: String? = null,
    val isLoading: Boolean = false
)

sealed class TaskFormUiEvent {
    data object SaveSuccess : TaskFormUiEvent()
    data class ShowError(val message: String) : TaskFormUiEvent()
}

@HiltViewModel
class TaskFormViewModel @Inject constructor(
    private val taskRepository: TaskRepository,
    private val categoryRepository: CategoryRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val taskId: Int? = savedStateHandle.get<String>("taskId")?.toIntOrNull()

    private val _uiState = MutableStateFlow(TaskFormUiState(taskId = taskId, isEditMode = taskId != null))
    val uiState: StateFlow<TaskFormUiState> = _uiState.asStateFlow()

    private val _eventFlow = MutableSharedFlow<TaskFormUiEvent>()
    val eventFlow: SharedFlow<TaskFormUiEvent> = _eventFlow.asSharedFlow()

    init {
        loadCategoriesAndTask()
    }

    private fun loadCategoriesAndTask() {
        viewModelScope.launch {
            val categories = categoryRepository.getCategories().firstOrNull() ?: emptyList()
            val defaultCategory = categories.firstOrNull { it.isSystem } ?: categories.firstOrNull()

            _uiState.update {
                it.copy(
                    availableCategories = categories,
                    selectedCategory = it.selectedCategory ?: defaultCategory
                )
            }

            if (taskId != null) {
                val existingTask = taskRepository.getTaskByIdDirect(taskId)
                if (existingTask != null) {
                    val matchingCategory = categories.firstOrNull { it.id == existingTask.categoryId }
                    _uiState.update {
                        it.copy(
                            title = existingTask.title,
                            description = existingTask.description ?: "",
                            selectedCategory = matchingCategory ?: defaultCategory,
                            priority = existingTask.priority,
                            dueDate = existingTask.dueDate,
                            hasDueTime = existingTask.hasDueTime,
                            status = existingTask.status
                        )
                    }
                }
            }
        }
    }

    fun onTitleChanged(newTitle: String) {
        _uiState.update {
            it.copy(
                title = newTitle,
                titleError = if (newTitle.trim().isEmpty()) "Judul tidak boleh kosong" else null
            )
        }
    }

    fun onDescriptionChanged(newDesc: String) {
        _uiState.update { it.copy(description = newDesc) }
    }

    fun onCategorySelected(category: Category) {
        _uiState.update { it.copy(selectedCategory = category) }
    }

    fun onPrioritySelected(priority: Priority) {
        _uiState.update { it.copy(priority = priority) }
    }

    fun onDeadlineSelected(dateMillis: Long?, hasDueTime: Boolean) {
        _uiState.update { it.copy(dueDate = dateMillis, hasDueTime = hasDueTime) }
    }

    fun onStatusSelected(status: TaskStatus) {
        _uiState.update { it.copy(status = status) }
    }

    fun onTaskTypeSelected(type: String) {
        _uiState.update { it.copy(taskType = type) }
    }

    fun saveTask() {
        val currentState = _uiState.value
        val trimmedTitle = currentState.title.trim()

        if (trimmedTitle.isEmpty()) {
            _uiState.update { it.copy(titleError = "Judul task wajib diisi (1-100 karakter)") }
            return
        }

        val categoryId = currentState.selectedCategory?.id ?: 1

        val taskToSave = Task(
            id = currentState.taskId ?: 0,
            title = trimmedTitle,
            description = currentState.description.trim().ifEmpty { null },
            categoryId = categoryId,
            status = currentState.status,
            priority = currentState.priority,
            dueDate = currentState.dueDate,
            hasDueTime = currentState.hasDueTime,
            completedAt = if (currentState.status == TaskStatus.COMPLETED) System.currentTimeMillis() else null
        )

        viewModelScope.launch {
            val result = taskRepository.saveTask(taskToSave)
            if (result.isSuccess) {
                _eventFlow.emit(TaskFormUiEvent.SaveSuccess)
            } else {
                _eventFlow.emit(TaskFormUiEvent.ShowError(result.exceptionOrNull()?.message ?: "Gagal menyimpan task"))
            }
        }
    }
}
