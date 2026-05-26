package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.TodoDatabase
import com.example.data.TodoItem
import com.example.data.TodoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class SortOption {
    CREATED_DESC,
    CREATED_ASC,
    PRIORITY_HIGH_TO_LOW,
    DUE_DATE_ASC,
    TITLE_ASC
}

data class TodoUiState(
    val todos: List<TodoItem> = emptyList(),
    val totalCount: Int = 0,
    val completedCount: Int = 0,
    val activeCount: Int = 0,
    val searchResultsCount: Int = 0,
    val completionPercentage: Float = 0f
)

class TodoViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: TodoRepository

    init {
        val database = TodoDatabase.getDatabase(application)
        repository = TodoRepository(database.todoDao())
    }

    // Filters and sorting state
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow("All")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    private val _sortOption = MutableStateFlow(SortOption.CREATED_DESC)
    val sortOption: StateFlow<SortOption> = _sortOption.asStateFlow()

    // Combined UI State
    val uiState: StateFlow<TodoUiState> = combine(
        repository.allTodos,
        _searchQuery,
        _selectedCategory,
        _sortOption
    ) { rawTodos, query, category, sort ->
        val total = rawTodos.size
        val completed = rawTodos.count { it.isCompleted }
        val active = total - completed
        val pct = if (total > 0) completed.toFloat() / total else 0f

        // Apply filters
        var filteredList = rawTodos.filter { item ->
            val matchesSearch = item.title.contains(query, ignoreCase = true) ||
                    item.description.contains(query, ignoreCase = true)
            val matchesCategory = category == "All" || item.category == category
            matchesSearch && matchesCategory
        }

        // Apply sorting
        filteredList = when (sort) {
            SortOption.CREATED_DESC -> filteredList.sortedByDescending { it.createdTimestamp }
            SortOption.CREATED_ASC -> filteredList.sortedBy { it.createdTimestamp }
            SortOption.PRIORITY_HIGH_TO_LOW -> filteredList.sortedByDescending { getPriorityWeight(it.priority) }
            SortOption.DUE_DATE_ASC -> filteredList.sortedWith(compareBy<TodoItem> { it.dueTimestamp == null }.thenBy { it.dueTimestamp })
            SortOption.TITLE_ASC -> filteredList.sortedBy { it.title.lowercase() }
        }

        TodoUiState(
            todos = filteredList,
            totalCount = total,
            completedCount = completed,
            activeCount = active,
            searchResultsCount = filteredList.size,
            completionPercentage = pct
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = TodoUiState()
    )

    // Form editing state (to pre-fill dialog or edit form)
    private val _todoToEdit = MutableStateFlow<TodoItem?>(null)
    val todoToEdit: StateFlow<TodoItem?> = _todoToEdit.asStateFlow()

    // Dialog state
    private val _isAddEditOpen = MutableStateFlow(false)
    val isAddEditOpen: StateFlow<Boolean> = _isAddEditOpen.asStateFlow()

    fun openAddDialog() {
        _todoToEdit.value = null
        _isAddEditOpen.value = true
    }

    fun openEditDialog(todo: TodoItem) {
        _todoToEdit.value = todo
        _isAddEditOpen.value = true
    }

    fun closeAddEditDialog() {
        _isAddEditOpen.value = false
        _todoToEdit.value = null
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSelectedCategory(category: String) {
        _selectedCategory.value = category
    }

    fun setSortOption(sort: SortOption) {
        _sortOption.value = sort
    }

    // Database Actions
    fun saveTodo(
        title: String,
        description: String,
        category: String,
        priority: String,
        dueTimestamp: Long?
    ) {
        viewModelScope.launch {
            val currentEditItem = _todoToEdit.value
            if (currentEditItem != null) {
                // Update
                val updated = currentEditItem.copy(
                    title = title,
                    description = description,
                    category = category,
                    priority = priority,
                    dueTimestamp = dueTimestamp
                )
                repository.update(updated)
            } else {
                // Insert
                val newItem = TodoItem(
                    title = title,
                    description = description,
                    category = category,
                    priority = priority,
                    dueTimestamp = dueTimestamp
                )
                repository.insert(newItem)
            }
            closeAddEditDialog()
        }
    }

    fun toggleTodoCompleted(todo: TodoItem) {
        viewModelScope.launch {
            repository.update(todo.copy(isCompleted = !todo.isCompleted))
        }
    }

    fun deleteTodo(todo: TodoItem) {
        viewModelScope.launch {
            repository.delete(todo)
        }
    }

    fun clearCompleted() {
        viewModelScope.launch {
            repository.clearCompleted()
        }
    }

    private fun getPriorityWeight(priority: String): Int {
        return when (priority.lowercase()) {
            "high" -> 3
            "medium" -> 2
            "low" -> 1
            else -> 0
        }
    }

    // Factory Class for AndroidViewModel
    class Factory(private val application: Application) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(TodoViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return TodoViewModel(application) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
