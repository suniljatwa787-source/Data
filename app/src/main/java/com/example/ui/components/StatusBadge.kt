package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.StatusActiveBg
import com.example.ui.theme.StatusActiveBgDark
import com.example.ui.theme.StatusActiveColor
import com.example.ui.theme.StatusActiveColorDark
import com.example.ui.theme.StatusArchivedBg
import com.example.ui.theme.StatusArchivedBgDark
import com.example.ui.theme.StatusArchivedColor
import com.example.ui.theme.StatusArchivedColorDark
import com.example.ui.theme.StatusCompletedBg
import com.example.ui.theme.StatusCompletedBgDark
import com.example.ui.theme.StatusCompletedColor
import com.example.ui.theme.StatusCompletedColorDark
import com.example.ui.theme.StatusPendingBg
import com.example.ui.theme.StatusPendingBgDark
import com.example.ui.theme.StatusPendingColor
import com.example.ui.theme.StatusPendingColorDark
import com.example.ui.theme.StatusReviewBg
import com.example.ui.theme.StatusReviewBgDark
import com.example.ui.theme.StatusReviewColor
import com.example.ui.theme.StatusReviewColorDark

@Composable
fun StatusBadge(
    status: String,
    modifier: Modifier = Modifier,
    isDark: Boolean = false
) {
    val (textColor, bgColor) = when (status.lowercase()) {
        "active" -> if (isDark) Pair(StatusActiveColorDark, StatusActiveBgDark) else Pair(StatusActiveColor, StatusActiveBg)
        "pending" -> if (isDark) Pair(StatusPendingColorDark, StatusPendingBgDark) else Pair(StatusPendingColor, StatusPendingBg)
        "review" -> if (isDark) Pair(StatusReviewColorDark, StatusReviewBgDark) else Pair(StatusReviewColor, StatusReviewBg)
        "completed" -> if (isDark) Pair(StatusCompletedColorDark, StatusCompletedBgDark) else Pair(StatusCompletedColor, StatusCompletedBg)
        else -> if (isDark) Pair(StatusArchivedColorDark, StatusArchivedBgDark) else Pair(StatusArchivedColor, StatusArchivedBg)
    }

    Row(
        modifier = modifier
            .testTag("status_badge_${status.lowercase()}")
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .padding(horizontal = 8.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(textColor)
        )
        Spacer(modifier = Modifier.width(5.dp))
        Text(
            text = status,
            color = textColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
fun CategoryBadge(
    category: String,
    modifier: Modifier = Modifier
) {
    val colorScheme = MaterialTheme.colorScheme
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(colorScheme.secondaryContainer.copy(alpha = 0.6f))
            .padding(horizontal = 7.dp, vertical = 2.dp)
    ) {
        Text(
            text = category,
            color = colorScheme.onSecondaryContainer,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium
        )
    }
}
