package com.heruteguhapriyant.tasky.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.heruteguhapriyant.tasky.domain.model.Category
import com.heruteguhapriyant.tasky.domain.model.Task
import com.heruteguhapriyant.tasky.domain.model.TaskStatus
import com.heruteguhapriyant.tasky.ui.theme.StatusOverdue
import com.heruteguhapriyant.tasky.util.DateTimeUtils

@Composable
fun TaskCard(
    task: Task,
    category: Category?,
    onToggleCompleted: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isCompleted = task.status == TaskStatus.COMPLETED
    val isOverdue = !isCompleted && DateTimeUtils.isOverdue(task.dueDate, task.hasDueTime)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Checkbox
            Checkbox(
                checked = isCompleted,
                onCheckedChange = { onToggleCompleted() },
                modifier = Modifier.size(24.dp),
                colors = CheckboxDefaults.colors(
                    checkedColor = MaterialTheme.colorScheme.primary,
                    uncheckedColor = MaterialTheme.colorScheme.outline
                )
            )

            Spacer(modifier = Modifier.width(12.dp))

            // Title & Category Pill
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp,
                        textDecoration = if (isCompleted) TextDecoration.LineThrough else TextDecoration.None
                    ),
                    color = if (isCompleted) {
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    },
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                CategoryPill(
                    name = category?.name ?: "Other",
                    icon = category?.icon ?: ""
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Time & Priority Pill
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.Center
            ) {
                val dueTimeText = if (task.dueDate != null) {
                    if (task.hasDueTime) DateTimeUtils.formatTime(task.dueDate) else DateTimeUtils.formatDate(task.dueDate)
                } else {
                    ""
                }

                if (dueTimeText.isNotBlank()) {
                    Text(
                        text = dueTimeText,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Medium,
                            fontSize = 11.sp
                        ),
                        color = if (isOverdue) StatusOverdue else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                }

                PriorityPill(priority = task.priority)
            }
        }
    }
}
