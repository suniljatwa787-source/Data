package com.example.data.model

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey

@Entity(tableName = "data_records")
data class DataRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val code: String,
    val name: String,
    val roleOrTitle: String,
    val category: String, // "Client", "Partner", "Vendor", "Internal", "Project"
    val email: String,
    val phone: String,
    val status: String, // "Active", "Pending", "Review", "Completed", "Archived"
    val valueAmount: Double,
    val documentTitle: String,
    val notesRichText: String,
    val priority: String = "Medium", // "High", "Medium", "Low"
    val entryDate: Long = System.currentTimeMillis(),
    val customTags: String = "", // comma-separated tags
    val customFieldsJson: String = "{}", // JSON map of custom key-value pairs
    val attachmentsJson: String = "[]", // JSON list of RecordAttachment
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    @get:Ignore
    val attachments: List<RecordAttachment>
        get() = AttachmentConverter.jsonToAttachments(attachmentsJson)

    @get:Ignore
    val customFields: Map<String, String>
        get() = AttachmentConverter.jsonToCustomFields(customFieldsJson)

    @get:Ignore
    val tagsList: List<String>
        get() = if (customTags.isBlank()) emptyList() else customTags.split(",").map { it.trim() }.filter { it.isNotBlank() }
}
