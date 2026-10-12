package com.heruteguhapriyant.tasky.ui.category

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.heruteguhapriyant.tasky.domain.model.Category
import com.heruteguhapriyant.tasky.domain.repository.CategoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CategoryItemUiState(
    val category: Category,
    val taskCount: Int = 0
)

data class CategoryManagementUiState(
    val categories: List<CategoryItemUiState> = emptyList(),
    val isLoading: Boolean = false,
    val showAddDialog: Boolean = false,
    val categoryToEdit: Category? = null,
    val categoryToDelete: Category? = null,
    val tasksAffectedByDelete: Int = 0
)

sealed class CategoryUiEvent {
    data class ShowSnackbar(val message: String) : CategoryUiEvent()
}

@HiltViewModel
class CategoryViewModel @Inject constructor(
    private val categoryRepository: CategoryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CategoryManagementUiState(isLoading = true))
    val uiState: StateFlow<CategoryManagementUiState> = _uiState.asStateFlow()

    private val _eventFlow = MutableSharedFlow<CategoryUiEvent>()
    val eventFlow: SharedFlow<CategoryUiEvent> = _eventFlow.asSharedFlow()

    init {
        loadCategoriesWithCounts()
    }

    private fun loadCategoriesWithCounts() {
        viewModelScope.launch {
            categoryRepository.getCategoriesWithTaskCount().collect { items ->
                val uiItems = items.map {
                    CategoryItemUiState(category = it.category, taskCount = it.taskCount)
                }
                _uiState.update {
                    it.copy(
                        categories = uiItems,
                        isLoading = false
                    )
                }
            }
        }
    }

    fun onAddCategoryClicked() {
        _uiState.update { it.copy(showAddDialog = true) }
    }

    fun onDismissAddDialog() {
        _uiState.update { it.copy(showAddDialog = false) }
    }

    fun onEditCategoryClicked(category: Category) {
        if (!category.isSystem) {
            _uiState.update { it.copy(categoryToEdit = category) }
        }
    }

    fun onDismissEditDialog() {
        _uiState.update { it.copy(categoryToEdit = null) }
    }

    fun onDeleteCategoryClicked(category: Category) {
        if (category.isSystem) return
        viewModelScope.launch {
            val affected = categoryRepository.getAffectedTaskCount(category.id)
            _uiState.update {
                it.copy(
                    categoryToDelete = category,
                    tasksAffectedByDelete = affected
                )
            }
        }
    }

    fun onDismissDeleteDialog() {
        _uiState.update { it.copy(categoryToDelete = null, tasksAffectedByDelete = 0) }
    }

    fun addCategory(name: String, icon: String) {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) {
            emitSnackbar("Nama kategori tidak boleh kosong.")
            return
        }
        if (trimmed.length > 30) {
            emitSnackbar("Nama kategori maksimal 30 karakter.")
            return
        }

        val existing = _uiState.value.categories.any { it.category.name.equals(trimmed, ignoreCase = true) }
        if (existing) {
            emitSnackbar("Kategori dengan nama '$trimmed' sudah ada.")
            return
        }

        viewModelScope.launch {
            try {
                val result = categoryRepository.addCategory(trimmed, icon.ifEmpty { "📁" })
                if (result.isSuccess) {
                    _uiState.update { it.copy(showAddDialog = false) }
                    emitSnackbar("Kategori '$trimmed' berhasil ditambahkan.")
                } else {
                    emitSnackbar(result.exceptionOrNull()?.message ?: "Gagal menambahkan kategori.")
                }
            } catch (e: Exception) {
                emitSnackbar("Terjadi kesalahan: ${e.message}")
            }
        }
    }

    fun updateCategory(name: String, icon: String) {
        val cat = _uiState.value.categoryToEdit ?: return
        val trimmed = name.trim()
        if (trimmed.isEmpty()) {
            emitSnackbar("Nama kategori tidak boleh kosong.")
            return
        }
        if (trimmed.length > 30) {
            emitSnackbar("Nama kategori maksimal 30 karakter.")
            return
        }

        val existing = _uiState.value.categories.any {
            it.category.id != cat.id && it.category.name.equals(trimmed, ignoreCase = true)
        }
        if (existing) {
            emitSnackbar("Kategori dengan nama '$trimmed' sudah ada.")
            return
        }

        val updated = cat.copy(name = trimmed, icon = icon.ifEmpty { cat.icon })
        viewModelScope.launch {
            try {
                val result = categoryRepository.updateCategory(updated)
                if (result.isSuccess) {
                    _uiState.update { it.copy(categoryToEdit = null) }
                    emitSnackbar("Kategori berhasil diperbarui.")
                } else {
                    emitSnackbar(result.exceptionOrNull()?.message ?: "Gagal memperbarui kategori.")
                }
            } catch (e: Exception) {
                emitSnackbar("Terjadi kesalahan: ${e.message}")
            }
        }
    }

    fun confirmDeleteCategory() {
        val cat = _uiState.value.categoryToDelete ?: return
        viewModelScope.launch {
            try {
                val result = categoryRepository.deleteCategory(cat.id)
                if (result.isSuccess) {
                    _uiState.update { it.copy(categoryToDelete = null, tasksAffectedByDelete = 0) }
                    emitSnackbar("Kategori '${cat.name}' dihapus. Task dipindahkan ke Other.")
                } else {
                    emitSnackbar(result.exceptionOrNull()?.message ?: "Gagal menghapus kategori.")
                }
            } catch (e: Exception) {
                emitSnackbar("Terjadi kesalahan: ${e.message}")
            }
        }
    }

    private fun emitSnackbar(msg: String) {
        viewModelScope.launch {
            _eventFlow.emit(CategoryUiEvent.ShowSnackbar(msg))
        }
    }
}
