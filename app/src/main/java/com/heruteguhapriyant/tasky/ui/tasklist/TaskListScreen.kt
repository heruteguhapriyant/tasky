package com.heruteguhapriyant.tasky.ui.tasklist

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.heruteguhapriyant.tasky.ui.components.TaskCard
import com.heruteguhapriyant.tasky.ui.theme.AccentSkyBlue
import com.heruteguhapriyant.tasky.ui.theme.CategoryPillBg
import com.heruteguhapriyant.tasky.ui.theme.CategoryPillText
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
                trailingIcon = {
                    if (uiState.searchQuery.isNotBlank()) {
                        IconButton(onClick = { viewModel.onSearchQueryChanged("") }) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Clear search",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
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

            // Status Filter Chips Row
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

            Spacer(modifier = Modifier.height(8.dp))

            // Category Filter Chips Row (Scrollable)
            if (uiState.categories.isNotEmpty()) {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    item {
                        val isAllCat = uiState.selectedCategoryId == null
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(14.dp))
                                .clickable { viewModel.onCategoryFilterSelected(null) },
                            shape = RoundedCornerShape(14.dp),
                            color = if (isAllCat) AccentSkyBlue.copy(alpha = 0.35f) else MaterialTheme.colorScheme.surface,
                            border = BorderStroke(
                                width = 1.dp,
                                color = if (isAllCat) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
                            )
                        ) {
                            Text(
                                text = "Semua Kategori",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = if (isAllCat) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 11.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }
                    }

                    items(uiState.categories, key = { it.id }) { cat ->
                        val isSelected = uiState.selectedCategoryId == cat.id
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(14.dp))
                                .clickable { viewModel.onCategoryFilterSelected(cat.id) },
                            shape = RoundedCornerShape(14.dp),
                            color = if (isSelected) CategoryPillBg else MaterialTheme.colorScheme.surface,
                            border = BorderStroke(
                                width = 1.dp,
                                color = if (isSelected) CategoryPillText else MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = cat.icon, fontSize = 11.sp)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = cat.name,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        fontSize = 11.sp
                                    ),
                                    color = if (isSelected) CategoryPillText else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

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
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = if (uiState.searchQuery.isNotBlank() || uiState.selectedCategoryId != null) {
                                "🔍 Tidak ada tugas yang cocok"
                            } else {
                                "📝 Belum ada tugas"
                            },
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (uiState.searchQuery.isNotBlank() || uiState.selectedCategoryId != null) {
                                "Coba ubah kata kunci pencarian atau reset filter kategori."
                            } else {
                                "Ketuk tombol + di kanan bawah untuk membuat tugas pertama Anda."
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
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
