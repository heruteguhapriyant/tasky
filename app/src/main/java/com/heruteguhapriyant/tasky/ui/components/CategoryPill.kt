package com.heruteguhapriyant.tasky.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
import com.heruteguhapriyant.tasky.ui.theme.CategoryPillBg
import com.heruteguhapriyant.tasky.ui.theme.CategoryPillText

@Composable
fun CategoryPill(
    name: String,
    icon: String = "",
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(CategoryPillBg)
            .padding(horizontal = 8.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (icon.isNotBlank()) {
            Text(
                text = icon,
                fontSize = 10.sp
            )
            Spacer(modifier = Modifier.width(3.dp))
        }
        Text(
            text = name,
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold
            ),
            color = CategoryPillText
        )
    }
}
