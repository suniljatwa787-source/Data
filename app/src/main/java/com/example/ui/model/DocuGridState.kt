package com.example.ui.model

enum class DocuGridViewMode(val label: String) {
    SPREADSHEET("Sheet Grid"),
    DOCUMENT("Document Workspace"),
    CARDS("Compact Cards")
}

enum class SortField(val displayName: String) {
    NAME("Name"),
    CODE("ID / Code"),
    STATUS("Status"),
    CATEGORY("Category"),
    VALUE("Value ($)"),
    ENTRY_DATE("Date"),
    UPDATED_AT("Last Updated")
}

enum class SortDirection {
    ASCENDING,
    DESCENDING;

    fun toggle(): SortDirection = if (this == ASCENDING) DESCENDING else ASCENDING
}

enum class PageSizeOption(val count: Int, val label: String) {
    TEN(10, "10 rows"),
    TWENTY_FIVE(25, "25 rows"),
    FIFTY(50, "50 rows"),
    ALL(Int.MAX_VALUE, "All rows")
}

sealed class SaveStatus(val message: String) {
    data object Saved : SaveStatus("All changes saved")
    data object Saving : SaveStatus("Saving changes...")
    data object Unsaved : SaveStatus("Unsaved changes")
}
