package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.model.PageSizeOption

@Composable
fun PaginationBar(
    totalRecords: Int,
    currentPage: Int,
    totalPages: Int,
    pageSizeOption: PageSizeOption,
    selectedCount: Int,
    onPageChange: (Int) -> Unit,
    onPageSizeChange: (PageSizeOption) -> Unit,
    modifier: Modifier = Modifier
) {
    var pageSizeMenuExpanded by remember { mutableStateOf(false) }

    val startRecord = if (totalRecords == 0) 0 else (currentPage - 1) * pageSizeOption.count + 1
    val endRecord = if (pageSizeOption == PageSizeOption.ALL) totalRecords else minOf(currentPage * pageSizeOption.count, totalRecords)

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("pagination_bar"),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp
    ) {
        Column {
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f), thickness = 0.5.dp)

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Summary text & Page size selector
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (totalRecords > 0) "Showing $startRecord-$endRecord of $totalRecords" else "0 records",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp
                    )
                    if (selectedCount > 0) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "($selectedCount selected)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    // Rows per page picker
                    Box {
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                .clickable { pageSizeMenuExpanded = true }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                .testTag("page_size_selector"),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = pageSizeOption.label,
                                style = MaterialTheme.typography.bodySmall,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = "Select page size",
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        DropdownMenu(
                            expanded = pageSizeMenuExpanded,
                            onDismissRequest = { pageSizeMenuExpanded = false }
                        ) {
                            PageSizeOption.entries.forEach { option ->
                                DropdownMenuItem(
                                    text = { Text(option.label) },
                                    onClick = {
                                        onPageSizeChange(option)
                                        pageSizeMenuExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                // Page navigation buttons
                if (pageSizeOption != PageSizeOption.ALL && totalPages > 1) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        IconButton(
                            onClick = { if (currentPage > 1) onPageChange(currentPage - 1) },
                            enabled = currentPage > 1,
                            modifier = Modifier
                                .size(32.dp)
                                .testTag("prev_page_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Previous page",
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        Text(
                            text = "$currentPage / $totalPages",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 12.sp
                        )

                        IconButton(
                            onClick = { if (currentPage < totalPages) onPageChange(currentPage + 1) },
                            enabled = currentPage < totalPages,
                            modifier = Modifier
                                .size(32.dp)
                                .testTag("next_page_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = "Next page",
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
