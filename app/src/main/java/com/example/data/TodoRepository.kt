package com.example.data

import kotlinx.coroutines.flow.Flow

class TodoRepository(private val todoDao: TodoDao) {
    val allTodos: Flow<List<TodoItem>> = todoDao.getAllTodosFlow()

    suspend fun insert(todo: TodoItem): Long = todoDao.insertTodo(todo)

    suspend fun update(todo: TodoItem) = todoDao.updateTodo(todo)

    suspend fun delete(todo: TodoItem) = todoDao.deleteTodo(todo)

    suspend fun deleteById(id: Int) = todoDao.deleteTodoById(id)

    suspend fun clearCompleted() = todoDao.clearCompletedTodos()
}
