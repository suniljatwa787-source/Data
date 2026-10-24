package com.example.data.model

data class RecordAttachment(
    val id: String = java.util.UUID.randomUUID().toString(),
    val fileName: String,
    val mimeType: String,
    val fileSizeBytes: Long,
    val localPath: String,
    val uriString: String = "",
    val addedAt: Long = System.currentTimeMillis()
) {
    val isImage: Boolean
        get() = mimeType.startsWith("image/", ignoreCase = true) ||
            fileName.endsWith(".jpg", ignoreCase = true) ||
            fileName.endsWith(".jpeg", ignoreCase = true) ||
            fileName.endsWith(".png", ignoreCase = true) ||
            fileName.endsWith(".webp", ignoreCase = true)

    val isPdf: Boolean
        get() = mimeType.contains("pdf", ignoreCase = true) || fileName.endsWith(".pdf", ignoreCase = true)

    val isSheetOrCsv: Boolean
        get() = mimeType.contains("sheet", ignoreCase = true) ||
            mimeType.contains("excel", ignoreCase = true) ||
            mimeType.contains("csv", ignoreCase = true) ||
            fileName.endsWith(".csv", ignoreCase = true) ||
            fileName.endsWith(".xlsx", ignoreCase = true) ||
            fileName.endsWith(".xls", ignoreCase = true)

    val formattedSize: String
        get() {
            if (fileSizeBytes <= 0) return "0 KB"
            val kb = fileSizeBytes / 1024.0
            val mb = kb / 1024.0
            return if (mb >= 1.0) {
                String.format(java.util.Locale.US, "%.1f MB", mb)
            } else {
                String.format(java.util.Locale.US, "%.0f KB", kb)
            }
        }
}
