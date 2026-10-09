package com.heruteguhapriyant.tasky.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.heruteguhapriyant.tasky.domain.model.Priority
import com.heruteguhapriyant.tasky.ui.theme.PriorityHighBg
import com.heruteguhapriyant.tasky.ui.theme.PriorityHighText
import com.heruteguhapriyant.tasky.ui.theme.PriorityLowBg
import com.heruteguhapriyant.tasky.ui.theme.PriorityLowText
import com.heruteguhapriyant.tasky.ui.theme.PriorityMediumBg
import com.heruteguhapriyant.tasky.ui.theme.PriorityMediumText

@Composable
fun PriorityPill(
    priority: Priority,
    modifier: Modifier = Modifier
) {
    val (bgColor, textColor, label) = when (priority) {
        Priority.HIGH -> Triple(PriorityHighBg, PriorityHighText, "High")
        Priority.MEDIUM -> Triple(PriorityMediumBg, PriorityMediumText, "Medium")
        Priority.LOW -> Triple(PriorityLowBg, PriorityLowText, "Low")
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bgColor)
            .padding(horizontal = 8.dp, vertical = 2.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold
            ),
            color = textColor
        )
    }
}
