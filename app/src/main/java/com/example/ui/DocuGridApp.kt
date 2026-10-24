package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.filled.ViewAgenda
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
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.DataRecord
import com.example.ui.components.BulkActionBar
import com.example.ui.components.CardsView
import com.example.ui.components.DeleteConfirmDialog
import com.example.ui.components.DocumentWorkspaceView
import com.example.ui.components.FilterBottomSheet
import com.example.ui.components.FilterChipsRow
import com.example.ui.components.ImportExportDialog
import com.example.ui.components.InlineCellEditDialog
import com.example.ui.components.PaginationBar
import com.example.ui.components.RecordEditorDialog
import com.example.ui.components.SpreadsheetGrid
import com.example.ui.model.DocuGridViewMode
import com.example.ui.theme.DocuGridTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DocuGridApp(
    viewModel: DocuGridViewModel
) {
    val context = LocalContext.current
    val isDark by viewModel.isDarkMode.collectAsStateWithLifecycle()

    DocuGridTheme(darkTheme = isDark) {
        val allRecords by viewModel.allRecords.collectAsStateWithLifecycle()
        val filteredRecords by viewModel.filteredRecords.collectAsStateWithLifecycle()
        val pagedRecords by viewModel.pagedRecords.collectAsStateWithLifecycle()
        val activeRecord by viewModel.activeRecord.collectAsStateWithLifecycle()
        val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
        val sortField by viewModel.sortField.collectAsStateWithLifecycle()
        val sortDirection by viewModel.sortDirection.collectAsStateWithLifecycle()
        val statusFilter by viewModel.statusFilter.collectAsStateWithLifecycle()
        val categoryFilter by viewModel.categoryFilter.collectAsStateWithLifecycle()
        val pageSizeOption by viewModel.pageSize.collectAsStateWithLifecycle()
        val currentPage by viewModel.currentPage.collectAsStateWithLifecycle()
        val selectedIds by viewModel.selectedIds.collectAsStateWithLifecycle()
        val viewMode by viewModel.viewMode.collectAsStateWithLifecycle()
        val saveStatus by viewModel.saveStatus.collectAsStateWithLifecycle()

        // Dialog States
        var showAddEditDialog by remember { mutableStateOf(false) }
        var recordToEdit by remember { mutableStateOf<DataRecord?>(null) }

        var showDeleteDialog by remember { mutableStateOf(false) }
        var recordToDelete by remember { mutableStateOf<DataRecord?>(null) }
        var isBulkDelete by remember { mutableStateOf(false) }

        var showFilterSheet by remember { mutableStateOf(false) }
        var showImportExportDialog by remember { mutableStateOf(false) }

        var inlineEditCellState by remember {
            mutableStateOf<Triple<Long, String, String>?>(null)
        }

        var moreMenuExpanded by remember { mutableStateOf(false) }
        var isSearchExpanded by remember { mutableStateOf(false) }

        val totalPages = viewModel.totalPages(filteredRecords.size, pageSizeOption.count)

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface)
                ) {
                    TopAppBar(
                        title = {
                            if (isSearchExpanded) {
                                OutlinedTextField(
                                    value = searchQuery,
                                    onValueChange = { viewModel.onSearchQueryChange(it) },
                                    placeholder = { Text("Search name, code, email, role...", fontSize = 13.sp) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("instant_search_input"),
                                    singleLine = true,
                                    trailingIcon = {
                                        if (searchQuery.isNotEmpty()) {
                                            IconButton(onClick = { viewModel.onSearchQueryChange("") }) {
                                                Icon(Icons.Default.Clear, contentDescription = "Clear search", modifier = Modifier.size(18.dp))
                                            }
                                        }
                                    },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                                        unfocusedBorderColor = Color.Transparent
                                    ),
                                    shape = RoundedCornerShape(8.dp)
                                )
                            } else {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    // Custom Logo Icon
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(MaterialTheme.colorScheme.primary),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.TableChart,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onPrimary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = "DocuGrid",
                                            style = MaterialTheme.typography.titleLarge,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "Data & Document Workspace",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            fontSize = 10.sp
                                        )
                                    }
                                }
                            }
                        },
                        actions = {
                            // Search toggle button
                            IconButton(
                                onClick = {
                                    isSearchExpanded = !isSearchExpanded
                                    if (!isSearchExpanded) viewModel.onSearchQueryChange("")
                                },
                                modifier = Modifier.testTag("toggle_search_button")
                            ) {
                                Icon(
                                    imageVector = if (isSearchExpanded) Icons.Default.Clear else Icons.Default.Search,
                                    contentDescription = "Search records"
                                )
                            }

                            // Dark / Light Theme Toggle
                            IconButton(
                                onClick = { viewModel.toggleDarkMode() },
                                modifier = Modifier.testTag("toggle_dark_mode_button")
                            ) {
                                Icon(
                                    imageVector = if (isDark) Icons.Default.LightMode else Icons.Default.DarkMode,
                                    contentDescription = "Toggle theme"
                                )
                            }

                            // More Options Menu
                            Box {
                                IconButton(
                                    onClick = { moreMenuExpanded = true },
                                    modifier = Modifier.testTag("more_menu_button")
                                ) {
                                    Icon(Icons.Default.MoreVert, contentDescription = "More options")
                                }

                                DropdownMenu(
                                    expanded = moreMenuExpanded,
                                    onDismissRequest = { moreMenuExpanded = false }
                                ) {
                                    DropdownMenuItem(
                                        text = { Text("Import / Export CSV") },
                                        onClick = {
                                            moreMenuExpanded = false
                                            showImportExportDialog = true
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Reset Sample Dataset") },
                                        onClick = {
                                            moreMenuExpanded = false
                                            viewModel.resetToSampleData()
                                        }
                                    )
                                }
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    )

                    // View Mode Switcher Row (Spreadsheet, Document, Cards)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .testTag("view_mode_switcher"),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        ) {
                            Row(
                                modifier = Modifier.padding(2.dp),
                                horizontalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                ViewModeTab(
                                    title = "Sheet Grid",
                                    icon = Icons.Default.TableChart,
                                    isSelected = viewMode == DocuGridViewMode.SPREADSHEET,
                                    onClick = { viewModel.setViewMode(DocuGridViewMode.SPREADSHEET) }
                                )
                                ViewModeTab(
                                    title = "Document",
                                    icon = Icons.Default.Description,
                                    isSelected = viewMode == DocuGridViewMode.DOCUMENT,
                                    onClick = { viewModel.setViewMode(DocuGridViewMode.DOCUMENT) }
                                )
                                ViewModeTab(
                                    title = "Cards",
                                    icon = Icons.Default.ViewAgenda,
                                    isSelected = viewMode == DocuGridViewMode.CARDS,
                                    onClick = { viewModel.setViewMode(DocuGridViewMode.CARDS) }
                                )
                            }
                        }

                        // Add Entry Action Button
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    recordToEdit = null
                                    showAddEditDialog = true
                                }
                                .testTag("quick_add_record_button"),
                            color = MaterialTheme.colorScheme.primary
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Add Entry",
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    // Column Filter Chips Bar (Shown in Spreadsheet & Cards modes)
                    if (viewMode != DocuGridViewMode.DOCUMENT) {
                        FilterChipsRow(
                            selectedStatus = statusFilter,
                            selectedCategory = categoryFilter,
                            onStatusSelected = { viewModel.setStatusFilter(it) },
                            onCategorySelected = { viewModel.setCategoryFilter(it) },
                            onOpenAllFilters = { showFilterSheet = true },
                            onClearFilters = {
                                viewModel.setStatusFilter(null)
                                viewModel.setCategoryFilter(null)
                            }
                        )
                    }
                }
            },
            bottomBar = {
                // In Spreadsheet or Cards view, render Pagination Bar
                if (viewMode != DocuGridViewMode.DOCUMENT) {
                    PaginationBar(
                        totalRecords = filteredRecords.size,
                        currentPage = currentPage,
                        totalPages = totalPages,
                        pageSizeOption = pageSizeOption,
                        selectedCount = selectedIds.size,
                        onPageChange = { viewModel.setCurrentPage(it) },
                        onPageSizeChange = { viewModel.setPageSize(it) }
                    )
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                // Main View Switching
                when (viewMode) {
                    DocuGridViewMode.SPREADSHEET -> {
                        SpreadsheetGrid(
                            records = pagedRecords,
                            selectedIds = selectedIds,
                            activeSortField = sortField,
                            activeSortDirection = sortDirection,
                            onSortClick = { viewModel.onSortFieldClick(it) },
                            onToggleSelect = { viewModel.toggleSelection(it) },
                            onSelectAll = { viewModel.selectAll(pagedRecords) },
                            onRowClick = {
                                viewModel.setActiveRecord(it.id)
                                viewModel.setViewMode(DocuGridViewMode.DOCUMENT)
                            },
                            onEditClick = {
                                recordToEdit = it
                                showAddEditDialog = true
                            },
                            onOpenDocumentClick = {
                                viewModel.setActiveRecord(it.id)
                                viewModel.setViewMode(DocuGridViewMode.DOCUMENT)
                            },
                            onDeleteClick = {
                                recordToDelete = it
                                isBulkDelete = false
                                showDeleteDialog = true
                            },
                            onInlineEditCell = { recordId, field, currentValue ->
                                inlineEditCellState = Triple(recordId, field, currentValue)
                            },
                            isDark = isDark
                        )
                    }
                    DocuGridViewMode.DOCUMENT -> {
                        DocumentWorkspaceView(
                            activeRecord = activeRecord,
                            allRecords = allRecords,
                            saveStatus = saveStatus,
                            onSelectRecord = { viewModel.setActiveRecord(it) },
                            onAutoSave = { id, title, notes ->
                                viewModel.autoSaveNotes(id, title, notes)
                            },
                            onManualSave = { id, title, notes ->
                                viewModel.manualSaveRecord(id, title, notes)
                            },
                            onAddFileUris = { id, uris ->
                                uris.forEach { uri -> viewModel.attachFile(context, id, uri) }
                            },
                            onRemoveAttachment = { recId, attId ->
                                viewModel.removeAttachment(recId, attId)
                            },
                            onAddTag = { recId, tag ->
                                viewModel.addTag(recId, tag)
                            },
                            onRemoveTag = { recId, tag ->
                                viewModel.removeTag(recId, tag)
                            },
                            onAddCustomField = { recId, k, v ->
                                viewModel.addCustomField(recId, k, v)
                            },
                            onRemoveCustomField = { recId, k ->
                                viewModel.removeCustomField(recId, k)
                            },
                            isDark = isDark
                        )
                    }
                    DocuGridViewMode.CARDS -> {
                        CardsView(
                            records = pagedRecords,
                            selectedIds = selectedIds,
                            onToggleSelect = { viewModel.toggleSelection(it) },
                            onRowClick = {
                                viewModel.setActiveRecord(it.id)
                                viewModel.setViewMode(DocuGridViewMode.DOCUMENT)
                            },
                            onEditClick = {
                                recordToEdit = it
                                showAddEditDialog = true
                            },
                            onOpenDocumentClick = {
                                viewModel.setActiveRecord(it.id)
                                viewModel.setViewMode(DocuGridViewMode.DOCUMENT)
                            },
                            onDeleteClick = {
                                recordToDelete = it
                                isBulkDelete = false
                                showDeleteDialog = true
                            },
                            isDark = isDark
                        )
                    }
                }

                // Floating Bulk Action Bar (Overlay at bottom)
                BulkActionBar(
                    selectedCount = selectedIds.size,
                    onDeleteSelected = {
                        isBulkDelete = true
                        recordToDelete = null
                        showDeleteDialog = true
                    },
                    onBulkStatusChange = { newStatus ->
                        viewModel.bulkUpdateStatus(newStatus)
                    },
                    onExportSelected = {
                        showImportExportDialog = true
                    },
                    onClearSelection = {
                        viewModel.clearSelection()
                    },
                    modifier = Modifier.align(Alignment.BottomCenter)
                )
            }
        }

        // Modals & Sheets
        if (showAddEditDialog) {
            RecordEditorDialog(
                initialRecord = recordToEdit,
                onDismiss = {
                    showAddEditDialog = false
                    recordToEdit = null
                },
                onSave = { record ->
                    if (recordToEdit != null) {
                        viewModel.updateRecord(record)
                    } else {
                        viewModel.addRecord(record)
                    }
                }
            )
        }

        if (showDeleteDialog) {
            DeleteConfirmDialog(
                itemCount = if (isBulkDelete) selectedIds.size else 1,
                singleItemName = recordToDelete?.name,
                onDismiss = {
                    showDeleteDialog = false
                    recordToDelete = null
                    isBulkDelete = false
                },
                onConfirm = {
                    if (isBulkDelete) {
                        viewModel.deleteSelectedRecords()
                    } else {
                        recordToDelete?.let { viewModel.deleteRecord(it.id) }
                    }
                }
            )
        }

        if (showFilterSheet) {
            FilterBottomSheet(
                selectedStatus = statusFilter,
                selectedCategory = categoryFilter,
                activeSortField = sortField,
                activeSortDirection = sortDirection,
                onStatusSelected = { viewModel.setStatusFilter(it) },
                onCategorySelected = { viewModel.setCategoryFilter(it) },
                onSortSelected = { viewModel.onSortFieldClick(it) },
                onClearAll = {
                    viewModel.setStatusFilter(null)
                    viewModel.setCategoryFilter(null)
                },
                onDismiss = { showFilterSheet = false }
            )
        }

        if (showImportExportDialog) {
            ImportExportDialog(
                csvContentToExport = viewModel.exportCsv(),
                onDismiss = { showImportExportDialog = false },
                onImportCsv = { csv ->
                    viewModel.importCsv(csv) { count ->
                        // updated
                    }
                },
                onResetSampleData = {
                    viewModel.resetToSampleData()
                }
            )
        }

        inlineEditCellState?.let { (recId, field, curVal) ->
            InlineCellEditDialog(
                fieldName = field,
                currentValue = curVal,
                onDismiss = { inlineEditCellState = null },
                onSave = { updatedVal ->
                    viewModel.inlineUpdateCell(recId, field, updatedVal)
                    inlineEditCellState = null
                }
            )
        }
    }
}

@Composable
private fun ViewModeTab(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .clickable(onClick = onClick),
        color = if (isSelected) MaterialTheme.colorScheme.surface else Color.Transparent,
        tonalElevation = if (isSelected) 2.dp else 0.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = title,
                fontSize = 11.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
