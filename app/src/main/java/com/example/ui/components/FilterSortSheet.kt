package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.FilterListOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.model.SortDirection
import com.example.ui.model.SortField

@Composable
fun FilterChipsRow(
    selectedStatus: String?,
    selectedCategory: String?,
    onStatusSelected: (String?) -> Unit,
    onCategorySelected: (String?) -> Unit,
    onOpenAllFilters: () -> Unit,
    onClearFilters: () -> Unit,
    modifier: Modifier = Modifier
) {
    val statuses = listOf("All", "Active", "Pending", "Review", "Completed", "Archived")
    val hasActiveFilters = selectedStatus != null || selectedCategory != null

    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 14.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // Filter button
        OutlinedButton(
            onClick = onOpenAllFilters,
            modifier = Modifier
                .height(32.dp)
                .testTag("open_filters_sheet_button"),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 0.dp)
        ) {
            Icon(Icons.Default.FilterList, contentDescription = null, modifier = Modifier.size(14.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Filters", fontSize = 11.sp)
        }

        // Status chips
        statuses.forEach { st ->
            val isSelected = (st == "All" && selectedStatus == null) || (selectedStatus == st)
            FilterChip(
                selected = isSelected,
                onClick = {
                    onStatusSelected(if (st == "All") null else st)
                },
                label = { Text(st, fontSize = 11.sp) },
                modifier = Modifier
                    .height(30.dp)
                    .testTag("filter_chip_$st"),
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }

        if (hasActiveFilters) {
            TextButton(
                onClick = onClearFilters,
                modifier = Modifier
                    .height(30.dp)
                    .testTag("clear_filter_chips_button"),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 6.dp, vertical = 0.dp)
            ) {
                Icon(Icons.Default.FilterListOff, contentDescription = null, modifier = Modifier.size(13.dp))
                Spacer(modifier = Modifier.width(3.dp))
                Text("Clear", fontSize = 11.sp)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterBottomSheet(
    selectedStatus: String?,
    selectedCategory: String?,
    activeSortField: SortField,
    activeSortDirection: SortDirection,
    onStatusSelected: (String?) -> Unit,
    onCategorySelected: (String?) -> Unit,
    onSortSelected: (SortField) -> Unit,
    onClearAll: () -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState()
    val statuses = listOf("All", "Active", "Pending", "Review", "Completed", "Archived")
    val categories = listOf("All", "Client", "Partner", "Vendor", "Internal", "Project")
    val sortOptions = listOf(
        SortField.NAME,
        SortField.CODE,
        SortField.STATUS,
        SortField.CATEGORY,
        SortField.VALUE,
        SortField.UPDATED_AT
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 10.dp)
                .testTag("filter_bottom_sheet")
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Spreadsheet Filters & Sort",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                TextButton(
                    onClick = onClearAll,
                    modifier = Modifier.testTag("reset_all_filters_button")
                ) {
                    Text("Reset All")
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Status Filter Section
            Text(
                text = "STATUS COLUMN",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                statuses.forEach { st ->
                    val isSelected = (st == "All" && selectedStatus == null) || (selectedStatus == st)
                    FilterChip(
                        selected = isSelected,
                        onClick = { onStatusSelected(if (st == "All") null else st) },
                        label = { Text(st, fontSize = 12.sp) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Category Filter Section
            Text(
                text = "ORGANIZATION CATEGORY",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                categories.forEach { cat ->
                    val isSelected = (cat == "All" && selectedCategory == null) || (selectedCategory == cat)
                    FilterChip(
                        selected = isSelected,
                        onClick = { onCategorySelected(if (cat == "All") null else cat) },
                        label = { Text(cat, fontSize = 12.sp) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Sort Section
            Text(
                text = "SORT BY COLUMN",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                sortOptions.forEach { field ->
                    val isSelected = activeSortField == field
                    FilterChip(
                        selected = isSelected,
                        onClick = { onSortSelected(field) },
                        label = {
                            val dirText = if (isSelected) {
                                if (activeSortDirection == SortDirection.ASCENDING) " (A-Z)" else " (Z-A)"
                            } else ""
                            Text("${field.displayName}$dirText", fontSize = 12.sp)
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}
