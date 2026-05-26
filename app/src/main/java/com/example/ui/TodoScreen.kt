package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.TodoItem
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun TodoScreen(
    viewModel: TodoViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val sortOption by viewModel.sortOption.collectAsState()
    val isAddEditOpen by viewModel.isAddEditOpen.collectAsState()
    val todoToEdit by viewModel.todoToEdit.collectAsState()

    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    // Navigation state (0: Tasks checklist, 1: Tic Tac Toe Game)
    var selectedTab by remember { mutableStateOf(0) }

    val categories = listOf("All", "Personal", "Work", "Social", "Shopping", "Ideas")

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        // Draw the FAB beautifully inside Scaffold, but only under Tasks tab
        floatingActionButton = {
            if (selectedTab == 0) {
                FloatingActionButton(
                    onClick = { viewModel.openAddDialog() },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier
                        .padding(8.dp)
                        .testTag("add_todo_fab")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add Task"
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "New Task",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            }
        },
        bottomBar = {
            NaturalBottomNavBar(
                selectedTab = selectedTab,
                onTabSelect = { selectedTab = it }
            )
        },
        contentWindowInsets = WindowInsets.safeDrawing
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (selectedTab == 0) {
                // Elegant Background Gradient Aura (Subtle Top Gradient Overlay)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f),
                                    Color.Transparent
                                )
                            )
                        )
                )

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp)
                ) {
                    Spacer(modifier = Modifier.height(16.dp))

                    // Beautiful Header with Dynamic Greeting
                    HeaderSection(
                        totalCount = uiState.totalCount,
                        completedCount = uiState.completedCount
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Visual Completion Progress Module
                    ProgressCard(
                        percentage = uiState.completionPercentage,
                        completed = uiState.completedCount,
                        total = uiState.totalCount,
                        onClearCompleted = { viewModel.clearCompleted() }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Search & Filter Block
                    SearchAndSortBar(
                        searchQuery = searchQuery,
                        onSearchQueryChange = { viewModel.setSearchQuery(it) },
                        sortOption = sortOption,
                        onSortOptionChange = { viewModel.setSortOption(it) },
                        focusManager = focusManager
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Category Selector Scrollable Row
                    CategoryChipsRow(
                        categories = categories,
                        selectedCategory = selectedCategory,
                        onCategorySelect = { viewModel.setSelectedCategory(it) }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Result Stats Indicator
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (uiState.searchResultsCount == 1) "1 task found" else "${uiState.searchResultsCount} tasks found",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Scrollable List of Todos
                    if (uiState.todos.isEmpty()) {
                        EmptyStateSection(
                            hasQuery = searchQuery.isNotEmpty() || selectedCategory != "All"
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(uiState.todos, key = { it.id }) { item ->
                                TodoRowItem(
                                    item = item,
                                    onToggleCompleted = { viewModel.toggleTodoCompleted(item) },
                                    onDelete = { viewModel.deleteTodo(item) },
                                    onEdit = { viewModel.openEditDialog(item) }
                                )
                            }
                            // Bottom spacer inside column to prevent FAB overlapping checklist items
                            item {
                                Spacer(modifier = Modifier.height(84.dp))
                            }
                        }
                    }
                }

                // Dialog for Creating / Editing Task
                if (isAddEditOpen) {
                    AddEditTaskDialog(
                        todoItem = todoToEdit,
                        onDismiss = { viewModel.closeAddEditDialog() },
                        onSave = { title, desc, cat, prio, due ->
                            viewModel.saveTodo(title, desc, cat, prio, due)
                        }
                    )
                }
            } else {
                // Tic Tac Toe screen
                val tttViewModel: TicTacToeViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
                TicTacToeScreen(viewModel = tttViewModel)
            }
        }
    }
}

@Composable
fun HeaderSection(totalCount: Int, completedCount: Int) {
    val dateString = remember {
        val formatter = SimpleDateFormat("EEEE, d MMMM", Locale.getDefault())
        formatter.format(Date())
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "My Spaces",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = dateString,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.55f)
            )
        }

        // Display current local space status info bubble
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f))
                .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Text(
                text = "Ready to build",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
fun ProgressCard(
    percentage: Float,
    completed: Int,
    total: Int,
    onClearCompleted: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
    ) {
        Row(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Circle Radial Progress Indicator
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(72.dp)
            ) {
                Canvas(modifier = Modifier.size(72.dp)) {
                    val strokeWidth = 8.dp.toPx()
                    // Background Ring Path
                    drawCircle(
                        color = Color.LightGray.copy(alpha = 0.25f),
                        radius = (size.minDimension - strokeWidth) / 2,
                        style = Stroke(width = strokeWidth)
                    )
                    // Active Completed Progress Segment Arc
                    drawArc(
                        color = Color(0xFF4CAF50), // Healthy Green
                        startAngle = -90f,
                        sweepAngle = percentage * 360f,
                        useCenter = false,
                        size = Size(size.width - strokeWidth, size.height - strokeWidth),
                        topLeft = Offset(strokeWidth / 2, strokeWidth / 2),
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )
                }
                
                Text(
                    text = "${(percentage * 100).toInt()}%",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Weekly Focus",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (total == 0) "No tasks yet. Create one below!" else "$completed of $total tasks completed",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                )

                // Prompt user if some tasks are pending or show motivational statement
                if (total > 0 && percentage == 1.0f) {
                    Text(
                        text = "Everything is clean! Awesome job.",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF4CAF50),
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            // Quick Clear completed items button
            if (completed > 0) {
                IconButton(
                    onClick = onClearCompleted,
                    modifier = Modifier
                        .background(
                            MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f),
                            shape = CircleShape
                        )
                        .size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Clear Completed tasks",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun SearchAndSortBar(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    sortOption: SortOption,
    onSortOptionChange: (SortOption) -> Unit,
    focusManager: androidx.compose.ui.focus.FocusManager
) {
    var isSortMenuExpanded by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Search text field
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            placeholder = { Text("Search tasks...") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { onSearchQueryChange("") }) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Clear search",
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
                focusedContainerColor = MaterialTheme.colorScheme.surface
            ),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(
                onSearch = { focusManager.clearFocus() }
            ),
            modifier = Modifier
                .weight(1f)
                .height(56.dp)
                .testTag("search_field")
        )

        Spacer(modifier = Modifier.width(8.dp))

        // Sort Dialog Menu Trigger
        Box {
            IconButton(
                onClick = { isSortMenuExpanded = true },
                modifier = Modifier
                    .size(56.dp)
                    .background(
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(16.dp)
                    )
            ) {
                Icon(
                    imageVector = Icons.Default.Info, // Generic info/sorting indicator icon
                    contentDescription = "Sort Menu Option",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            DropdownMenu(
                expanded = isSortMenuExpanded,
                onDismissRequest = { isSortMenuExpanded = false },
                modifier = Modifier.background(MaterialTheme.colorScheme.surface)
            ) {
                DropdownMenuItem(
                    text = { Text("Newest first") },
                    onClick = {
                        onSortOptionChange(SortOption.CREATED_DESC)
                        isSortMenuExpanded = false
                    },
                    leadingIcon = {
                        if (sortOption == SortOption.CREATED_DESC) Icon(Icons.Default.Check, "Active")
                    }
                )
                DropdownMenuItem(
                    text = { Text("Oldest first") },
                    onClick = {
                        onSortOptionChange(SortOption.CREATED_ASC)
                        isSortMenuExpanded = false
                    },
                    leadingIcon = {
                        if (sortOption == SortOption.CREATED_ASC) Icon(Icons.Default.Check, "Active")
                    }
                )
                DropdownMenuItem(
                    text = { Text("Highest Priority first") },
                    onClick = {
                        onSortOptionChange(SortOption.PRIORITY_HIGH_TO_LOW)
                        isSortMenuExpanded = false
                    },
                    leadingIcon = {
                        if (sortOption == SortOption.PRIORITY_HIGH_TO_LOW) Icon(Icons.Default.Check, "Active")
                    }
                )
                DropdownMenuItem(
                    text = { Text("By Due Date") },
                    onClick = {
                        onSortOptionChange(SortOption.DUE_DATE_ASC)
                        isSortMenuExpanded = false
                    },
                    leadingIcon = {
                        if (sortOption == SortOption.DUE_DATE_ASC) Icon(Icons.Default.Check, "Active")
                    }
                )
                DropdownMenuItem(
                    text = { Text("By Title (A-Z)") },
                    onClick = {
                        onSortOptionChange(SortOption.TITLE_ASC)
                        isSortMenuExpanded = false
                    },
                    leadingIcon = {
                        if (sortOption == SortOption.TITLE_ASC) Icon(Icons.Default.Check, "Active")
                    }
                )
            }
        }
    }
}

@Composable
fun CategoryChipsRow(
    categories: List<String>,
    selectedCategory: String,
    onCategorySelect: (String) -> Unit
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        items(categories) { category ->
            val isSelected = category == selectedCategory
            val backgroundChipColor by animateColorAsState(
                targetValue = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                label = "chipColor"
            )
            val textChipColor by animateColorAsState(
                targetValue = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                label = "chipTextColor"
            )

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(14.dp))
                    .background(backgroundChipColor)
                    .clickable { onCategorySelect(category) }
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .testTag("category_chip_$category")
            ) {
                Text(
                    text = category,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = textChipColor
                )
            }
        }
    }
}

@Composable
fun TodoRowItem(
    item: TodoItem,
    onToggleCompleted: () -> Unit,
    onDelete: () -> Unit,
    onEdit: () -> Unit
) {
    // Dynamic styling based on Priority
    val priorityColor = when (item.priority.lowercase()) {
        "high" -> Color(0xFFEF5350) // Coral red
        "medium" -> Color(0xFFFFB74D) // Amber warning yellow
        "low" -> Color(0xFF81C784) // Cool mint green
        else -> MaterialTheme.colorScheme.outline
    }

    // Completion states
    val itemBgColor by animateColorAsState(
        targetValue = if (item.isCompleted) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f) else MaterialTheme.colorScheme.surface,
        label = "itemBgColor"
    )

    val contentAlpha = if (item.isCompleted) 0.5f else 1.0f

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = itemBgColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
            .testTag("todo_item_${item.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Priority Line Stripe Indicator
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(48.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(priorityColor)
            )

            Spacer(modifier = Modifier.width(12.dp))

            // Highly Tactile Animated Checkbox
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(
                        if (item.isCompleted) Color(0xFF4CAF50) else Color.Transparent,
                        CircleShape
                    )
                    .border(
                        2.dp,
                        if (item.isCompleted) Color(0xFF4CAF50) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                        CircleShape
                    )
                    .clickable { onToggleCompleted() }
                    .testTag("checkbox_${item.id}"),
                contentAlignment = Alignment.Center
            ) {
                if (item.isCompleted) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Task complete check",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Content Block
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clickable(
                        onClick = onEdit,
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    )
            ) {
                // Title
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = contentAlpha),
                    textDecoration = if (item.isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                if (item.description.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = item.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f * contentAlpha),
                        textDecoration = if (item.isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Metadata Rows (Category Pill and Due date indicator)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Category small tag
                    Box(
                        modifier = Modifier
                            .background(
                                MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f * contentAlpha),
                                RoundedCornerShape(6.dp)
                            )
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = item.category,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = contentAlpha),
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    // Due date badge if exists
                    if (item.dueTimestamp != null) {
                        val formattedDate = remember(item.dueTimestamp) {
                            val sdf = SimpleDateFormat("dd MMM", Locale.getDefault())
                            sdf.format(Date(item.dueTimestamp))
                        }
                        
                        val isOverdue = !item.isCompleted && item.dueTimestamp < System.currentTimeMillis()
                        val badgeColor = if (isOverdue) Color(0xFFEF5350).copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant
                        val textBadgeColor = if (isOverdue) Color(0xFFEF5350) else MaterialTheme.colorScheme.onSurfaceVariant

                        Box(
                            modifier = Modifier
                                .background(badgeColor, RoundedCornerShape(6.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Text(
                                    text = if (isOverdue) "Overdue • $formattedDate" else "Due • $formattedDate",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = textBadgeColor.copy(alpha = contentAlpha),
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }

            // Actions Block for this item
            Row {
                IconButton(
                    onClick = onEdit,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit task text",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f * contentAlpha),
                        modifier = Modifier.size(16.dp)
                    )
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete task permanently",
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f * contentAlpha),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun EmptyStateSection(hasQuery: Boolean) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 80.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Canvas(modifier = Modifier.size(100.dp)) {
            // Draw a stylish placeholder geometric circle check artwork
            drawCircle(
                color = Color.LightGray.copy(alpha = 0.15f),
                radius = size.minDimension / 2
            )
            val strokeWidth = 3.dp.toPx()
            drawCircle(
                color = Color.LightGray.copy(alpha = 0.4f),
                radius = size.minDimension / 3,
                style = Stroke(width = strokeWidth)
            )
            // Beautiful custom tick graphics inside the empty illustration circle
            val pathSize = size.minDimension / 3
            val centerX = size.width / 2
            val centerY = size.height / 2
            drawLine(
                color = Color.LightGray.copy(alpha = 0.4f),
                start = Offset(centerX - pathSize / 3, centerY),
                end = Offset(centerX - pathSize / 10, centerY + pathSize / 4),
                strokeWidth = strokeWidth,
                cap = StrokeCap.Round
            )
            drawLine(
                color = Color.LightGray.copy(alpha = 0.4f),
                start = Offset(centerX - pathSize / 10, centerY + pathSize / 4),
                end = Offset(centerX + pathSize / 2, centerY - pathSize / 4),
                strokeWidth = strokeWidth,
                cap = StrokeCap.Round
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = if (hasQuery) "No matching tasks found" else "All captured! Your dashboard is clean",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = if (hasQuery) "Try clearing the search query or changing active tags" else "Enjoy the momentum, or create a brand new list trigger using the button below.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
            modifier = Modifier.padding(horizontal = 24.dp),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddEditTaskDialog(
    todoItem: TodoItem?,
    onDismiss: () -> Unit,
    onSave: (String, String, String, String, Long?) -> Unit
) {
    var title by remember { mutableStateOf(todoItem?.title ?: "") }
    var description by remember { mutableStateOf(todoItem?.description ?: "") }
    var category by remember { mutableStateOf(todoItem?.category ?: "Personal") }
    var priority by remember { mutableStateOf(todoItem?.priority ?: "Medium") }
    var hasDueDate by remember { mutableStateOf(todoItem?.dueTimestamp != null) }
    var dueTimestamp by remember { mutableStateOf(todoItem?.dueTimestamp) }

    val isEditing = todoItem != null

    // Validation
    var showError by remember { mutableStateOf(false) }

    val categories = listOf("Personal", "Work", "Social", "Shopping", "Ideas")
    val priorities = listOf("Low", "Medium", "High")

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .padding(24.dp)
                .widthIn(max = 480.dp)
                .clip(RoundedCornerShape(28.dp))
                .background(MaterialTheme.colorScheme.surface)
                .testTag("add_edit_dialog"),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .animateContentSize()
            ) {
                // Header of dialog
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isEditing) "Edit Task" else "Create New Task",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Cancel and close"
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Title Input
                OutlinedTextField(
                    value = title,
                    onValueChange = {
                        title = it
                        if (it.isNotEmpty()) showError = false
                    },
                    label = { Text("Task Title *") },
                    placeholder = { Text("What needs to be done?") },
                    isError = showError,
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("dialog_title_field")
                )
                if (showError) {
                    Text(
                        text = "Title is required",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(start = 6.dp, top = 2.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Description Input
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    placeholder = { Text("Add descriptive notes or checklists (optional)") },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Category Selection Label
                Text(
                    text = "Category",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                Spacer(modifier = Modifier.height(6.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    categories.forEach { cat ->
                        val isSelected = cat == category
                        val chipColor = if (isSelected) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                        val contentColor = if (isSelected) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                        
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(chipColor)
                                .clickable { category = cat }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                                .testTag("dialog_category_$cat")
                        ) {
                            Text(
                                text = cat,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = contentColor
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Priority Selection Label
                Text(
                    text = "Priority",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    priorities.forEach { prio ->
                        val isSelected = prio == priority
                        val activeColor = when (prio.lowercase()) {
                            "high" -> Color(0xFFEF5350)
                            "medium" -> Color(0xFFFFB74D)
                            "low" -> Color(0xFF81C784)
                            else -> MaterialTheme.colorScheme.primary
                        }
                        
                        val bg = if (isSelected) activeColor.copy(alpha = 0.2f) else Color.Transparent
                        val borderCol = if (isSelected) activeColor else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                        val textCol = if (isSelected) activeColor else MaterialTheme.colorScheme.onSurfaceVariant

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(bg)
                                .border(1.dp, borderCol, RoundedCornerShape(10.dp))
                                .clickable { priority = prio }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = prio,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = textCol
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Quick Scheduled Due Date Preset Selector
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Set Due Date",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                    
                    // Simple Toggle switch
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .background(
                                if (hasDueDate) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            )
                            .clickable {
                                hasDueDate = !hasDueDate
                                if (hasDueDate) {
                                    dueTimestamp = System.currentTimeMillis() // Default Today
                                } else {
                                    dueTimestamp = null
                                }
                            }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = if (hasDueDate) "Active" else "Off",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (hasDueDate) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                if (hasDueDate) {
                    Spacer(modifier = Modifier.height(8.dp))

                    // Preset buttons
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Today preset
                        DueDatePresetButton(
                            text = "Today",
                            isSelected = isSameDay(dueTimestamp, System.currentTimeMillis()),
                            onClick = { dueTimestamp = getPresetTimestamp(0) }
                        )

                        // Tomorrow preset
                        DueDatePresetButton(
                            text = "Tomorrow",
                            isSelected = isSameDay(dueTimestamp, getPresetTimestamp(1)),
                            onClick = { dueTimestamp = getPresetTimestamp(1) }
                        )

                        // Next Week preset
                        DueDatePresetButton(
                            text = "Next Week",
                            isSelected = isSameDay(dueTimestamp, getPresetTimestamp(7)),
                            onClick = { dueTimestamp = getPresetTimestamp(7) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Actions Save & Cancel
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (title.isBlank()) {
                                showError = true
                            } else {
                                onSave(title, description, category, priority, if (hasDueDate) dueTimestamp else null)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("dialog_submit_button")
                    ) {
                        Text(
                            text = if (isEditing) "Save Changes" else "Add Task",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DueDatePresetButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val bg = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent
    val borderCol = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
    val textCol = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bg)
            .border(1.dp, borderCol, RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = textCol
        )
    }
}

private fun getPresetTimestamp(daysAhead: Int): Long {
    val calendar = Calendar.getInstance()
    calendar.add(Calendar.DAY_OF_YEAR, daysAhead)
    calendar.set(Calendar.HOUR_OF_DAY, 23)
    calendar.set(Calendar.MINUTE, 59)
    calendar.set(Calendar.SECOND, 59)
    calendar.set(Calendar.MILLISECOND, 999)
    return calendar.timeInMillis
}

private fun isSameDay(t1: Long?, t2: Long?): Boolean {
    if (t1 == null || t2 == null) return false
    val cal1 = Calendar.getInstance().apply { timeInMillis = t1 }
    val cal2 = Calendar.getInstance().apply { timeInMillis = t2 }
    return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
            cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
}

@Composable
fun NaturalBottomNavBar(
    selectedTab: Int,
    onTabSelect: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 4.dp,
        modifier = modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), // Matching #E0E4D6 under light, subtle variant under dark
                shape = androidx.compose.ui.graphics.RectangleShape
            )
            .navigationBarsPadding() // Safely clears the Android software back gesture pill
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(68.dp)
                .padding(bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            // Tab 0: Tasks Checklist using standard Checklist Check Icon
            BottomNavItem(
                label = "Tasks",
                icon = Icons.Default.Check,
                isActive = selectedTab == 0,
                onClick = { onTabSelect(0) }
            )

            // Tab 1: Tic Tac Toe Arena using the standard Info / Stats symbol
            BottomNavItem(
                label = "Zen Arena",
                icon = Icons.Default.Info,
                isActive = selectedTab == 1,
                onClick = { onTabSelect(1) }
            )
        }
    }
}

@Composable
fun BottomNavItem(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isActive: Boolean,
    onClick: () -> Unit
) {
    val activePillColor by animateColorAsState(
        targetValue = if (isActive) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
        label = "pillColor"
    )
    val contentColor by animateColorAsState(
        targetValue = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
        label = "contentColor"
    )

    Column(
        modifier = Modifier
            .width(100.dp)
            .fillMaxHeight()
            .clickable(
                onClick = onClick,
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(activePillColor)
                .padding(horizontal = 16.dp, vertical = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = contentColor,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label.uppercase(),
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 9.sp,
                lineHeight = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            ),
            color = contentColor
        )
    }
}
