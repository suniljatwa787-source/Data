package com.example.ui

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.model.DataRecord
import com.example.data.repository.RecordRepository
import com.example.ui.model.DocuGridViewMode
import com.example.ui.model.PageSizeOption
import com.example.ui.model.SaveStatus
import com.example.ui.model.SortDirection
import com.example.ui.model.SortField
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.math.ceil
import kotlin.math.max

data class FilterParams(
    val query: String = "",
    val sortField: SortField = SortField.UPDATED_AT,
    val sortDirection: SortDirection = SortDirection.DESCENDING,
    val status: String? = null,
    val category: String? = null
)

class DocuGridViewModel(
    private val repository: RecordRepository
) : ViewModel() {

    val allRecords: StateFlow<List<DataRecord>> = repository.allRecords
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _sortField = MutableStateFlow(SortField.UPDATED_AT)
    val sortField = _sortField.asStateFlow()

    private val _sortDirection = MutableStateFlow(SortDirection.DESCENDING)
    val sortDirection = _sortDirection.asStateFlow()

    private val _statusFilter = MutableStateFlow<String?>(null)
    val statusFilter = _statusFilter.asStateFlow()

    private val _categoryFilter = MutableStateFlow<String?>(null)
    val categoryFilter = _categoryFilter.asStateFlow()

    private val _pageSize = MutableStateFlow(PageSizeOption.TEN)
    val pageSize = _pageSize.asStateFlow()

    private val _currentPage = MutableStateFlow(1)
    val currentPage = _currentPage.asStateFlow()

    private val _selectedIds = MutableStateFlow<Set<Long>>(emptySet())
    val selectedIds = _selectedIds.asStateFlow()

    private val _activeRecordId = MutableStateFlow<Long?>(null)
    val activeRecordId = _activeRecordId.asStateFlow()

    private val _viewMode = MutableStateFlow(DocuGridViewMode.SPREADSHEET)
    val viewMode = _viewMode.asStateFlow()

    private val _saveStatus = MutableStateFlow<SaveStatus>(SaveStatus.Saved)
    val saveStatus = _saveStatus.asStateFlow()

    private val _isDarkMode = MutableStateFlow(false)
    val isDarkMode = _isDarkMode.asStateFlow()

    private var autoSaveJob: Job? = null

    init {
        viewModelScope.launch {
            repository.prepopulateIfEmpty()
        }
    }

    private val filterParams = combine(
        _searchQuery,
        _sortField,
        _sortDirection,
        _statusFilter,
        _categoryFilter
    ) { query, field, dir, status, category ->
        FilterParams(query, field, dir, status, category)
    }

    // Filtered and Sorted Records Flow
    val filteredRecords: StateFlow<List<DataRecord>> = combine(
        allRecords,
        filterParams
    ) { records, params ->
        val q = params.query.trim().lowercase()

        var filtered = records.filter { record ->
            val matchesQuery = q.isEmpty() ||
                record.name.lowercase().contains(q) ||
                record.code.lowercase().contains(q) ||
                record.email.lowercase().contains(q) ||
                record.phone.lowercase().contains(q) ||
                record.roleOrTitle.lowercase().contains(q) ||
                record.category.lowercase().contains(q) ||
                record.status.lowercase().contains(q) ||
                record.customTags.lowercase().contains(q) ||
                record.customFieldsJson.lowercase().contains(q) ||
                record.documentTitle.lowercase().contains(q)

            val matchesStatus = params.status == null || record.status.equals(params.status, ignoreCase = true)
            val matchesCategory = params.category == null || record.category.equals(params.category, ignoreCase = true)

            matchesQuery && matchesStatus && matchesCategory
        }

        filtered = when (params.sortField) {
            SortField.NAME -> if (params.sortDirection == SortDirection.ASCENDING) filtered.sortedBy { it.name.lowercase() } else filtered.sortedByDescending { it.name.lowercase() }
            SortField.CODE -> if (params.sortDirection == SortDirection.ASCENDING) filtered.sortedBy { it.code.lowercase() } else filtered.sortedByDescending { it.code.lowercase() }
            SortField.STATUS -> if (params.sortDirection == SortDirection.ASCENDING) filtered.sortedBy { it.status.lowercase() } else filtered.sortedByDescending { it.status.lowercase() }
            SortField.CATEGORY -> if (params.sortDirection == SortDirection.ASCENDING) filtered.sortedBy { it.category.lowercase() } else filtered.sortedByDescending { it.category.lowercase() }
            SortField.VALUE -> if (params.sortDirection == SortDirection.ASCENDING) filtered.sortedBy { it.valueAmount } else filtered.sortedByDescending { it.valueAmount }
            SortField.ENTRY_DATE -> if (params.sortDirection == SortDirection.ASCENDING) filtered.sortedBy { it.entryDate } else filtered.sortedByDescending { it.entryDate }
            SortField.UPDATED_AT -> if (params.sortDirection == SortDirection.ASCENDING) filtered.sortedBy { it.updatedAt } else filtered.sortedByDescending { it.updatedAt }
        }

        filtered
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Paginated Records
    val pagedRecords: StateFlow<List<DataRecord>> = combine(
        filteredRecords,
        _pageSize,
        _currentPage
    ) { records, sizeOpt, page ->
        if (sizeOpt == PageSizeOption.ALL) {
            records
        } else {
            val fromIndex = (page - 1) * sizeOpt.count
            if (fromIndex >= records.size) {
                emptyList()
            } else {
                val toIndex = minOf(fromIndex + sizeOpt.count, records.size)
                records.subList(fromIndex, toIndex)
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Currently active record for Document view
    val activeRecord: StateFlow<DataRecord?> = combine(
        allRecords,
        _activeRecordId
    ) { records, id ->
        if (id != null) {
            records.find { it.id == id } ?: records.firstOrNull()
        } else {
            records.firstOrNull()
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    fun totalPages(filteredCount: Int, pageSizeCount: Int): Int {
        if (pageSizeCount >= Int.MAX_VALUE || pageSizeCount <= 0) return 1
        return max(1, ceil(filteredCount.toDouble() / pageSizeCount).toInt())
    }

    fun onSearchQueryChange(newQuery: String) {
        _searchQuery.value = newQuery
        _currentPage.value = 1
    }

    fun onSortFieldClick(field: SortField) {
        if (_sortField.value == field) {
            _sortDirection.value = _sortDirection.value.toggle()
        } else {
            _sortField.value = field
            _sortDirection.value = SortDirection.ASCENDING
        }
        _currentPage.value = 1
    }

    fun setStatusFilter(status: String?) {
        _statusFilter.value = status
        _currentPage.value = 1
    }

    fun setCategoryFilter(category: String?) {
        _categoryFilter.value = category
        _currentPage.value = 1
    }

    fun setPageSize(option: PageSizeOption) {
        _pageSize.value = option
        _currentPage.value = 1
    }

    fun setCurrentPage(page: Int) {
        _currentPage.value = page
    }

    fun setViewMode(mode: DocuGridViewMode) {
        _viewMode.value = mode
    }

    fun setActiveRecord(id: Long) {
        _activeRecordId.value = id
    }

    fun toggleDarkMode() {
        _isDarkMode.value = !_isDarkMode.value
    }

    fun toggleSelection(id: Long) {
        val current = _selectedIds.value
        _selectedIds.value = if (current.contains(id)) current - id else current + id
    }

    fun selectAll(records: List<DataRecord>) {
        val allIds = records.map { it.id }.toSet()
        if (_selectedIds.value.containsAll(allIds)) {
            _selectedIds.value = emptySet()
        } else {
            _selectedIds.value = allIds
        }
    }

    fun clearSelection() {
        _selectedIds.value = emptySet()
    }

    fun addRecord(record: DataRecord) {
        viewModelScope.launch {
            _saveStatus.value = SaveStatus.Saving
            val newId = repository.insert(record)
            _activeRecordId.value = newId
            delay(250)
            _saveStatus.value = SaveStatus.Saved
        }
    }

    fun updateRecord(record: DataRecord) {
        viewModelScope.launch {
            _saveStatus.value = SaveStatus.Saving
            repository.update(record)
            delay(250)
            _saveStatus.value = SaveStatus.Saved
        }
    }

    fun inlineUpdateCell(recordId: Long, field: String, newValue: String) {
        viewModelScope.launch {
            val record = allRecords.value.find { it.id == recordId } ?: return@launch
            _saveStatus.value = SaveStatus.Saving

            val updated = when (field) {
                "name" -> record.copy(name = newValue)
                "code" -> record.copy(code = newValue)
                "roleOrTitle" -> record.copy(roleOrTitle = newValue)
                "category" -> record.copy(category = newValue)
                "email" -> record.copy(email = newValue)
                "phone" -> record.copy(phone = newValue)
                "status" -> record.copy(status = newValue)
                "customTags" -> record.copy(customTags = newValue)
                "valueAmount" -> {
                    val parsed = newValue.replace("$", "").replace(",", "").toDoubleOrNull() ?: record.valueAmount
                    record.copy(valueAmount = parsed)
                }
                "documentTitle" -> record.copy(documentTitle = newValue)
                "notes" -> record.copy(notesRichText = newValue)
                else -> record
            }

            repository.update(updated)
            delay(200)
            _saveStatus.value = SaveStatus.Saved
        }
    }

    fun autoSaveNotes(recordId: Long, updatedTitle: String, updatedNotes: String) {
        _saveStatus.value = SaveStatus.Saving
        autoSaveJob?.cancel()
        autoSaveJob = viewModelScope.launch {
            delay(600) // Debounce auto-save
            val record = allRecords.value.find { it.id == recordId }
            if (record != null) {
                repository.update(record.copy(documentTitle = updatedTitle, notesRichText = updatedNotes))
                _saveStatus.value = SaveStatus.Saved
            }
        }
    }

    fun manualSaveRecord(recordId: Long, updatedTitle: String, updatedNotes: String) {
        viewModelScope.launch {
            _saveStatus.value = SaveStatus.Saving
            val record = allRecords.value.find { it.id == recordId }
            if (record != null) {
                repository.update(record.copy(documentTitle = updatedTitle, notesRichText = updatedNotes))
                delay(300)
                _saveStatus.value = SaveStatus.Saved
            }
        }
    }

    fun addCustomField(recordId: Long, key: String, value: String) {
        viewModelScope.launch {
            val record = allRecords.value.find { it.id == recordId } ?: return@launch
            val fields = record.customFields.toMutableMap()
            fields[key.trim()] = value.trim()
            _saveStatus.value = SaveStatus.Saving
            repository.updateCustomFields(recordId, fields)
            delay(200)
            _saveStatus.value = SaveStatus.Saved
        }
    }

    fun removeCustomField(recordId: Long, key: String) {
        viewModelScope.launch {
            val record = allRecords.value.find { it.id == recordId } ?: return@launch
            val fields = record.customFields.toMutableMap()
            fields.remove(key)
            _saveStatus.value = SaveStatus.Saving
            repository.updateCustomFields(recordId, fields)
            delay(200)
            _saveStatus.value = SaveStatus.Saved
        }
    }

    fun addTag(recordId: Long, tag: String) {
        viewModelScope.launch {
            val record = allRecords.value.find { it.id == recordId } ?: return@launch
            val tags = record.tagsList.toMutableList()
            val cleanTag = tag.trim().replace(",", "")
            if (cleanTag.isNotBlank() && cleanTag !in tags) {
                tags.add(cleanTag)
                _saveStatus.value = SaveStatus.Saving
                repository.updateTags(recordId, tags)
                delay(200)
                _saveStatus.value = SaveStatus.Saved
            }
        }
    }

    fun removeTag(recordId: Long, tag: String) {
        viewModelScope.launch {
            val record = allRecords.value.find { it.id == recordId } ?: return@launch
            val tags = record.tagsList.filterNot { it.equals(tag.trim(), ignoreCase = true) }
            _saveStatus.value = SaveStatus.Saving
            repository.updateTags(recordId, tags)
            delay(200)
            _saveStatus.value = SaveStatus.Saved
        }
    }

    fun attachFile(context: Context, recordId: Long, uri: Uri) {
        viewModelScope.launch {
            _saveStatus.value = SaveStatus.Saving
            repository.addAttachment(context, recordId, uri)
            delay(300)
            _saveStatus.value = SaveStatus.Saved
        }
    }

    fun removeAttachment(recordId: Long, attachmentId: String) {
        viewModelScope.launch {
            _saveStatus.value = SaveStatus.Saving
            repository.removeAttachment(recordId, attachmentId)
            delay(200)
            _saveStatus.value = SaveStatus.Saved
        }
    }

    fun deleteRecord(id: Long) {
        viewModelScope.launch {
            repository.deleteById(id)
            _selectedIds.value = _selectedIds.value - id
            if (_activeRecordId.value == id) {
                _activeRecordId.value = allRecords.value.firstOrNull { it.id != id }?.id
            }
        }
    }

    fun deleteSelectedRecords() {
        val idsToDelete = _selectedIds.value.toList()
        if (idsToDelete.isEmpty()) return

        viewModelScope.launch {
            repository.deleteMultiple(idsToDelete)
            _selectedIds.value = emptySet()
            if (_activeRecordId.value in idsToDelete) {
                _activeRecordId.value = allRecords.value.firstOrNull { it.id !in idsToDelete }?.id
            }
        }
    }

    fun bulkUpdateStatus(newStatus: String) {
        val ids = _selectedIds.value
        if (ids.isEmpty()) return

        viewModelScope.launch {
            _saveStatus.value = SaveStatus.Saving
            for (record in allRecords.value) {
                if (record.id in ids) {
                    repository.update(record.copy(status = newStatus))
                }
            }
            delay(300)
            _saveStatus.value = SaveStatus.Saved
            _selectedIds.value = emptySet()
        }
    }

    fun exportCsv(): String {
        val recordsToExport = if (_selectedIds.value.isNotEmpty()) {
            filteredRecords.value.filter { it.id in _selectedIds.value }
        } else {
            filteredRecords.value
        }
        return repository.exportToCsv(recordsToExport)
    }

    fun importCsv(content: String, onComplete: (Int) -> Unit) {
        viewModelScope.launch {
            val count = repository.importCsv(content)
            onComplete(count)
        }
    }

    fun resetToSampleData() {
        viewModelScope.launch {
            repository.resetToDefaultSampleData()
            _selectedIds.value = emptySet()
        }
    }
}

class DocuGridViewModelFactory(
    private val repository: RecordRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DocuGridViewModel::class.java)) {
            return DocuGridViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
