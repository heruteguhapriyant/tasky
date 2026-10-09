package com.heruteguhapriyant.tasky.ui.tasklist

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.heruteguhapriyant.tasky.domain.model.Task
import com.heruteguhapriyant.tasky.ui.components.TaskCard
import com.heruteguhapriyant.tasky.ui.theme.FilterActiveGreen
import com.heruteguhapriyant.tasky.ui.theme.OnFilterActiveGreen

@Composable
fun TaskListScreen(
    viewModel: TaskListViewModel = hiltViewModel(),
    onNavigateToCreateTask: () -> Unit = {},
    onNavigateToTaskDetail: (Int) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToCreateTask,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.size(56.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Create Task",
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Screen Title
            Text(
                text = "Tasks",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 28.sp
                ),
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Search Bar
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = viewModel::onSearchQueryChanged,
                placeholder = {
                    Text(
                        text = "Search Tasks",
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    focusedBorderColor = MaterialTheme.colorScheme.outline,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                ),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Filter Chips Row
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                item {
                    FilterChipPill(
                        label = "All",
                        selected = uiState.selectedFilter == TaskFilterTab.ALL,
                        onClick = { viewModel.onFilterSelected(TaskFilterTab.ALL) }
                    )
                }
                item {
                    FilterChipPill(
                        label = "Today",
                        selected = uiState.selectedFilter == TaskFilterTab.TODAY,
                        onClick = { viewModel.onFilterSelected(TaskFilterTab.TODAY) }
                    )
                }
                item {
                    FilterChipPill(
                        label = "Upcoming",
                        selected = uiState.selectedFilter == TaskFilterTab.UPCOMING,
                        onClick = { viewModel.onFilterSelected(TaskFilterTab.UPCOMING) }
                    )
                }
                item {
                    FilterChipPill(
                        label = "Completed",
                        selected = uiState.selectedFilter == TaskFilterTab.COMPLETED,
                        onClick = { viewModel.onFilterSelected(TaskFilterTab.COMPLETED) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            val hasNoTasks = uiState.todayTasks.isEmpty() &&
                    uiState.upcomingTasks.isEmpty() &&
                    uiState.otherTasks.isEmpty() &&
                    uiState.completedTasks.isEmpty()

            if (hasNoTasks) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (uiState.searchQuery.isNotBlank()) {
                            "Tidak ada task yang cocok dengan pencarian."
                        } else {
                            "Belum ada task.\nKetuk tombol + untuk membuat task baru."
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Today Section
                    if (uiState.todayTasks.isNotEmpty()) {
                        item {
                            SectionHeader(title = "Today")
                        }
                        items(uiState.todayTasks, key = { it.id }) { task ->
                            TaskCard(
                                task = task,
                                category = uiState.categoriesMap[task.categoryId],
                                onToggleCompleted = { viewModel.toggleTaskCompletion(task.id) },
                                onClick = { onNavigateToTaskDetail(task.id) }
                            )
                        }
                    }

                    // Upcoming Section
                    if (uiState.upcomingTasks.isNotEmpty()) {
                        item {
                            Spacer(modifier = Modifier.height(6.dp))
                            SectionHeader(title = "Upcoming")
                        }
                        items(uiState.upcomingTasks, key = { it.id }) { task ->
                            TaskCard(
                                task = task,
                                category = uiState.categoriesMap[task.categoryId],
                                onToggleCompleted = { viewModel.toggleTaskCompletion(task.id) },
                                onClick = { onNavigateToTaskDetail(task.id) }
                            )
                        }
                    }

                    // Other Tasks Section (No deadline or other dates)
                    if (uiState.otherTasks.isNotEmpty()) {
                        item {
                            Spacer(modifier = Modifier.height(6.dp))
                            SectionHeader(title = "Other Tasks")
                        }
                        items(uiState.otherTasks, key = { it.id }) { task ->
                            TaskCard(
                                task = task,
                                category = uiState.categoriesMap[task.categoryId],
                                onToggleCompleted = { viewModel.toggleTaskCompletion(task.id) },
                                onClick = { onNavigateToTaskDetail(task.id) }
                            )
                        }
                    }

                    // Completed Section
                    if (uiState.completedTasks.isNotEmpty()) {
                        item {
                            Spacer(modifier = Modifier.height(6.dp))
                            SectionHeader(title = "Completed")
                        }
                        items(uiState.completedTasks, key = { it.id }) { task ->
                            TaskCard(
                                task = task,
                                category = uiState.categoriesMap[task.categoryId],
                                onToggleCompleted = { viewModel.toggleTaskCompletion(task.id) },
                                onClick = { onNavigateToTaskDetail(task.id) }
                            )
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium.copy(
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp
        ),
        color = MaterialTheme.colorScheme.onBackground,
        modifier = Modifier.padding(vertical = 4.dp)
    )
}

@Composable
private fun FilterChipPill(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        color = if (selected) FilterActiveGreen else MaterialTheme.colorScheme.surface,
        border = BorderStroke(
            width = 1.dp,
            color = if (selected) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
        )
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                    fontSize = 13.sp
                ),
                color = if (selected) OnFilterActiveGreen else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
