package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.UnfoldMore
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DataRecord
import com.example.ui.model.SortDirection
import com.example.ui.model.SortField
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SpreadsheetGrid(
    records: List<DataRecord>,
    selectedIds: Set<Long>,
    activeSortField: SortField,
    activeSortDirection: SortDirection,
    onSortClick: (SortField) -> Unit,
    onToggleSelect: (Long) -> Unit,
    onSelectAll: () -> Unit,
    onRowClick: (DataRecord) -> Unit,
    onEditClick: (DataRecord) -> Unit,
    onOpenDocumentClick: (DataRecord) -> Unit,
    onDeleteClick: (DataRecord) -> Unit,
    onInlineEditCell: (recordId: Long, field: String, currentValue: String) -> Unit,
    isDark: Boolean,
    modifier: Modifier = Modifier
) {
    val horizontalScrollState = rememberScrollState()
    val currencyFormatter = remember { NumberFormat.getCurrencyInstance(Locale.US).apply { maximumFractionDigits = 0 } }
    val dateFormatter = remember { SimpleDateFormat("MMM d, yyyy", Locale.getDefault()) }

    // Column widths in dp for excel-like columns
    val checkColWidth = 48.dp
    val codeColWidth = 100.dp
    val nameColWidth = 180.dp
    val roleColWidth = 160.dp
    val catColWidth = 110.dp
    val statusColWidth = 120.dp
    val valueColWidth = 120.dp
    val updatedColWidth = 120.dp
    val actionsColWidth = 120.dp

    val totalWidth = checkColWidth + codeColWidth + nameColWidth + roleColWidth + catColWidth + statusColWidth + valueColWidth + updatedColWidth + actionsColWidth

    Surface(
        modifier = modifier
            .fillMaxSize()
            .testTag("spreadsheet_grid"),
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .horizontalScroll(horizontalScrollState)
                .width(totalWidth)
        ) {
            // Excel-like Sticky Header Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f))
                    .padding(vertical = 10.dp)
                    .testTag("grid_header_row"),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Select all checkbox
                Box(
                    modifier = Modifier.width(checkColWidth),
                    contentAlignment = Alignment.Center
                ) {
                    val allSelected = records.isNotEmpty() && records.all { it.id in selectedIds }
                    Checkbox(
                        checked = allSelected,
                        onCheckedChange = { onSelectAll() },
                        modifier = Modifier.testTag("select_all_checkbox")
                    )
                }

                // Code Header
                HeaderCell(
                    title = "ID / Code",
                    width = codeColWidth,
                    sortField = SortField.CODE,
                    activeField = activeSortField,
                    activeDir = activeSortDirection,
                    onClick = { onSortClick(SortField.CODE) }
                )

                // Name Header
                HeaderCell(
                    title = "Name / Subject",
                    width = nameColWidth,
                    sortField = SortField.NAME,
                    activeField = activeSortField,
                    activeDir = activeSortDirection,
                    onClick = { onSortClick(SortField.NAME) }
                )

                // Role Header
                HeaderCell(
                    title = "Role / Title",
                    width = roleColWidth,
                    sortField = null,
                    activeField = activeSortField,
                    activeDir = activeSortDirection,
                    onClick = {}
                )

                // Category Header
                HeaderCell(
                    title = "Category",
                    width = catColWidth,
                    sortField = SortField.CATEGORY,
                    activeField = activeSortField,
                    activeDir = activeSortDirection,
                    onClick = { onSortClick(SortField.CATEGORY) }
                )

                // Status Header
                HeaderCell(
                    title = "Status",
                    width = statusColWidth,
                    sortField = SortField.STATUS,
                    activeField = activeSortField,
                    activeDir = activeSortDirection,
                    onClick = { onSortClick(SortField.STATUS) }
                )

                // Value Header
                HeaderCell(
                    title = "Value ($)",
                    width = valueColWidth,
                    sortField = SortField.VALUE,
                    activeField = activeSortField,
                    activeDir = activeSortDirection,
                    onClick = { onSortClick(SortField.VALUE) }
                )

                // Updated Header
                HeaderCell(
                    title = "Updated",
                    width = updatedColWidth,
                    sortField = SortField.UPDATED_AT,
                    activeField = activeSortField,
                    activeDir = activeSortDirection,
                    onClick = { onSortClick(SortField.UPDATED_AT) }
                )

                // Actions Header
                Box(
                    modifier = Modifier
                        .width(actionsColWidth)
                        .padding(horizontal = 8.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Text(
                        text = "Actions",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                thickness = 1.dp
            )

            // Table Rows
            if (records.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 60.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "No matching records found",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Try adjusting your search query or reset column filters.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    itemsIndexed(records, key = { _, r -> r.id }) { index, record ->
                        val isSelected = record.id in selectedIds
                        val rowBg = when {
                            isSelected -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
                            index % 2 == 1 -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f)
                            else -> MaterialTheme.colorScheme.surface
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(rowBg)
                                .clickable { onRowClick(record) }
                                .padding(vertical = 8.dp)
                                .testTag("grid_row_${record.id}"),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Checkbox
                            Box(
                                modifier = Modifier.width(checkColWidth),
                                contentAlignment = Alignment.Center
                            ) {
                                Checkbox(
                                    checked = isSelected,
                                    onCheckedChange = { onToggleSelect(record.id) },
                                    modifier = Modifier.testTag("row_checkbox_${record.id}")
                                )
                            }

                            // Code (Clickable inline edit)
                            Box(
                                modifier = Modifier
                                    .width(codeColWidth)
                                    .padding(horizontal = 6.dp)
                                    .clickable { onInlineEditCell(record.id, "code", record.code) }
                            ) {
                                Text(
                                    text = record.code,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.primary,
                                    maxLines = 1
                                )
                            }

                            // Name & Avatar
                            Row(
                                modifier = Modifier
                                    .width(nameColWidth)
                                    .padding(horizontal = 6.dp)
                                    .clickable { onInlineEditCell(record.id, "name", record.name) },
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val initials = record.name.split(" ")
                                    .take(2)
                                    .mapNotNull { it.firstOrNull()?.uppercase() }
                                    .joinToString("")
                                    .ifEmpty { "R" }

                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = initials,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = record.name,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        if (record.attachments.isNotEmpty()) {
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = "📎${record.attachments.size}",
                                                fontSize = 10.sp,
                                                color = MaterialTheme.colorScheme.primary,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                    Text(
                                        text = record.email,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 11.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }

                            // Role
                            Box(
                                modifier = Modifier
                                    .width(roleColWidth)
                                    .padding(horizontal = 6.dp)
                                    .clickable { onInlineEditCell(record.id, "roleOrTitle", record.roleOrTitle) }
                            ) {
                                Text(
                                    text = record.roleOrTitle,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            // Category Badge
                            Box(
                                modifier = Modifier
                                    .width(catColWidth)
                                    .padding(horizontal = 6.dp)
                            ) {
                                CategoryBadge(category = record.category)
                            }

                            // Status Badge
                            Box(
                                modifier = Modifier
                                    .width(statusColWidth)
                                    .padding(horizontal = 6.dp)
                            ) {
                                StatusBadge(status = record.status, isDark = isDark)
                            }

                            // Value Amount
                            Box(
                                modifier = Modifier
                                    .width(valueColWidth)
                                    .padding(horizontal = 6.dp)
                                    .clickable { onInlineEditCell(record.id, "valueAmount", record.valueAmount.toString()) }
                            ) {
                                Text(
                                    text = currencyFormatter.format(record.valueAmount),
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1
                                )
                            }

                            // Last Updated Date
                            Box(
                                modifier = Modifier
                                    .width(updatedColWidth)
                                    .padding(horizontal = 6.dp)
                            ) {
                                Text(
                                    text = dateFormatter.format(Date(record.updatedAt)),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 12.sp,
                                    maxLines = 1
                                )
                            }

                            // Actions
                            Row(
                                modifier = Modifier
                                    .width(actionsColWidth)
                                    .padding(horizontal = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconButton(
                                    onClick = { onEditClick(record) },
                                    modifier = Modifier
                                        .size(34.dp)
                                        .testTag("row_edit_${record.id}")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = "Edit record",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(17.dp)
                                    )
                                }
                                IconButton(
                                    onClick = { onOpenDocumentClick(record) },
                                    modifier = Modifier
                                        .size(34.dp)
                                        .testTag("row_doc_${record.id}")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Description,
                                        contentDescription = "Open document workspace",
                                        tint = MaterialTheme.colorScheme.tertiary,
                                        modifier = Modifier.size(17.dp)
                                    )
                                }
                                IconButton(
                                    onClick = { onDeleteClick(record) },
                                    modifier = Modifier
                                        .size(34.dp)
                                        .testTag("row_delete_${record.id}")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.DeleteOutline,
                                        contentDescription = "Delete record",
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(17.dp)
                                    )
                                }
                            }
                        }

                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f),
                            thickness = 0.5.dp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HeaderCell(
    title: String,
    width: androidx.compose.ui.unit.Dp,
    sortField: SortField?,
    activeField: SortField,
    activeDir: SortDirection,
    onClick: () -> Unit
) {
    val isSorted = sortField != null && activeField == sortField

    Row(
        modifier = Modifier
            .width(width)
            .clickable(enabled = sortField != null, onClick = onClick)
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = if (isSorted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        if (sortField != null) {
            Icon(
                imageVector = when {
                    !isSorted -> Icons.Default.UnfoldMore
                    activeDir == SortDirection.ASCENDING -> Icons.Default.ArrowUpward
                    else -> Icons.Default.ArrowDownward
                },
                contentDescription = "Sort by $title",
                tint = if (isSorted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.size(14.dp)
            )
        }
    }
}
