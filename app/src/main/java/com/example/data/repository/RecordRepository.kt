package com.example.data.repository

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import com.example.data.db.RecordDao
import com.example.data.model.AttachmentConverter
import com.example.data.model.DataRecord
import com.example.data.model.RecordAttachment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

class RecordRepository(private val recordDao: RecordDao) {
    val allRecords: Flow<List<DataRecord>> = recordDao.getAllRecords()

    fun getRecordById(id: Long): Flow<DataRecord?> = recordDao.getRecordById(id)

    suspend fun insert(record: DataRecord): Long {
        return recordDao.insertRecord(record.copy(updatedAt = System.currentTimeMillis()))
    }

    suspend fun update(record: DataRecord) {
        recordDao.updateRecord(record.copy(updatedAt = System.currentTimeMillis()))
    }

    suspend fun deleteById(id: Long) {
        recordDao.deleteById(id)
    }

    suspend fun deleteMultiple(ids: List<Long>) {
        recordDao.deleteMultipleByIds(ids)
    }

    suspend fun resetToDefaultSampleData() {
        recordDao.deleteAll()
        recordDao.insertAll(sampleRecords)
    }

    suspend fun prepopulateIfEmpty() {
        val count = recordDao.getCount()
        if (count == 0) {
            recordDao.insertAll(sampleRecords)
        }
    }

    suspend fun addAttachment(context: Context, recordId: Long, uri: Uri): RecordAttachment? = withContext(Dispatchers.IO) {
        try {
            val contentResolver = context.contentResolver
            var fileName = "attachment_${System.currentTimeMillis()}"
            var fileSize = 0L

            contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                if (cursor.moveToFirst()) {
                    if (nameIndex != -1) fileName = cursor.getString(nameIndex) ?: fileName
                    if (sizeIndex != -1) fileSize = cursor.getLong(sizeIndex)
                }
            }

            val mimeType = contentResolver.getType(uri) ?: when {
                fileName.endsWith(".pdf", ignoreCase = true) -> "application/pdf"
                fileName.endsWith(".jpg", ignoreCase = true) || fileName.endsWith(".jpeg", ignoreCase = true) -> "image/jpeg"
                fileName.endsWith(".png", ignoreCase = true) -> "image/png"
                fileName.endsWith(".csv", ignoreCase = true) -> "text/csv"
                fileName.endsWith(".xlsx", ignoreCase = true) -> "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                else -> "application/octet-stream"
            }

            val attachmentsDir = File(context.filesDir, "attachments").apply { mkdirs() }
            val destFile = File(attachmentsDir, "${UUID.randomUUID()}_$fileName")

            contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(destFile).use { output ->
                    input.copyTo(output)
                }
            }

            if (fileSize == 0L) {
                fileSize = destFile.length()
            }

            val attachment = RecordAttachment(
                id = UUID.randomUUID().toString(),
                fileName = fileName,
                mimeType = mimeType,
                fileSizeBytes = fileSize,
                localPath = destFile.absolutePath,
                uriString = uri.toString(),
                addedAt = System.currentTimeMillis()
            )

            // Update record in database
            val currentRecord = recordDao.getRecordById(recordId).firstOrNull()
            if (currentRecord != null) {
                val currentAttachments = currentRecord.attachments.toMutableList()
                currentAttachments.add(attachment)
                val updatedRecord = currentRecord.copy(
                    attachmentsJson = AttachmentConverter.attachmentsToJson(currentAttachments),
                    updatedAt = System.currentTimeMillis()
                )
                recordDao.updateRecord(updatedRecord)
            }

            attachment
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun removeAttachment(recordId: Long, attachmentId: String) {
        val currentRecord = recordDao.getRecordById(recordId).firstOrNull() ?: return
        val currentAttachments = currentRecord.attachments.filterNot { it.id == attachmentId }
        val updatedRecord = currentRecord.copy(
            attachmentsJson = AttachmentConverter.attachmentsToJson(currentAttachments),
            updatedAt = System.currentTimeMillis()
        )
        recordDao.updateRecord(updatedRecord)
    }

    suspend fun updateCustomFields(recordId: Long, newCustomFields: Map<String, String>) {
        val currentRecord = recordDao.getRecordById(recordId).firstOrNull() ?: return
        val updatedRecord = currentRecord.copy(
            customFieldsJson = AttachmentConverter.customFieldsToJson(newCustomFields),
            updatedAt = System.currentTimeMillis()
        )
        recordDao.updateRecord(updatedRecord)
    }

    suspend fun updateTags(recordId: Long, tags: List<String>) {
        val currentRecord = recordDao.getRecordById(recordId).firstOrNull() ?: return
        val updatedRecord = currentRecord.copy(
            customTags = tags.joinToString(", "),
            updatedAt = System.currentTimeMillis()
        )
        recordDao.updateRecord(updatedRecord)
    }

    fun exportToCsv(records: List<DataRecord>): String {
        val sb = StringBuilder()
        // CSV Header
        sb.append("ID,Code,Name,Role/Title,Category,Email,Phone,Status,Value (USD),Priority,Date,Tags,Document Title,Notes\n")
        val dateFormatter = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US)

        for (r in records) {
            val escapedName = escapeCsv(r.name)
            val escapedRole = escapeCsv(r.roleOrTitle)
            val escapedCategory = escapeCsv(r.category)
            val escapedEmail = escapeCsv(r.email)
            val escapedPhone = escapeCsv(r.phone)
            val escapedStatus = escapeCsv(r.status)
            val escapedDate = dateFormatter.format(java.util.Date(r.entryDate))
            val escapedTags = escapeCsv(r.customTags)
            val escapedDocTitle = escapeCsv(r.documentTitle)
            val escapedNotes = escapeCsv(r.notesRichText)
            sb.append("${r.id},${r.code},$escapedName,$escapedRole,$escapedCategory,$escapedEmail,$escapedPhone,$escapedStatus,${r.valueAmount},${r.priority},$escapedDate,$escapedTags,$escapedDocTitle,$escapedNotes\n")
        }
        return sb.toString()
    }

    suspend fun importCsv(csvContent: String): Int {
        val lines = csvContent.lines().filter { it.isNotBlank() }
        if (lines.isEmpty()) return 0

        val hasHeader = lines.first().contains("Name", ignoreCase = true) || lines.first().contains("Email", ignoreCase = true)
        val dataLines = if (hasHeader) lines.drop(1) else lines

        val parsedRecords = mutableListOf<DataRecord>()
        var counter = System.currentTimeMillis() % 10000

        for (line in dataLines) {
            val parts = parseCsvLine(line)
            if (parts.size >= 3) {
                counter++
                val code = if (parts.size > 1 && parts[1].isNotBlank()) parts[1].trim() else "IMP-$counter"
                val name = parts.getOrNull(2)?.trim()?.ifBlank { "Imported Entry #$counter" } ?: "Imported Entry"
                val role = parts.getOrNull(3)?.trim() ?: "Specialist"
                val category = parts.getOrNull(4)?.trim() ?: "Client"
                val email = parts.getOrNull(5)?.trim() ?: "contact-$counter@example.com"
                val phone = parts.getOrNull(6)?.trim() ?: "+1 (555) 019-$counter"
                val status = parts.getOrNull(7)?.trim() ?: "Active"
                val value = parts.getOrNull(8)?.trim()?.replace("$", "")?.replace(",", "")?.toDoubleOrNull() ?: 12500.0
                val priority = parts.getOrNull(9)?.trim() ?: "Medium"
                val tags = parts.getOrNull(11)?.trim() ?: "Imported"
                val docTitle = parts.getOrNull(12)?.trim() ?: "Document Dossier: $name"
                val notes = parts.getOrNull(13)?.trim() ?: "# Document Brief\n- Imported record notes\n- Ready for workspace editing."

                parsedRecords.add(
                    DataRecord(
                        code = code,
                        name = name,
                        roleOrTitle = role,
                        category = category,
                        email = email,
                        phone = phone,
                        status = status,
                        valueAmount = value,
                        priority = priority,
                        entryDate = System.currentTimeMillis(),
                        customTags = tags,
                        documentTitle = docTitle,
                        notesRichText = notes,
                        createdAt = System.currentTimeMillis(),
                        updatedAt = System.currentTimeMillis()
                    )
                )
            }
        }

        if (parsedRecords.isNotEmpty()) {
            recordDao.insertAll(parsedRecords)
        }
        return parsedRecords.size
    }

    private fun escapeCsv(text: String): String {
        if (text.contains(",") || text.contains("\"") || text.contains("\n")) {
            return "\"" + text.replace("\"", "\"\"") + "\""
        }
        return text
    }

    private fun parseCsvLine(line: String): List<String> {
        val tokens = mutableListOf<String>()
        var cur = StringBuilder()
        var inQuotes = false
        var i = 0
        while (i < line.length) {
            val c = line[i]
            if (c == '\"') {
                if (inQuotes && i + 1 < line.length && line[i + 1] == '\"') {
                    cur.append('\"')
                    i++
                } else {
                    inQuotes = !inQuotes
                }
            } else if (c == ',' && !inQuotes) {
                tokens.add(cur.toString().trim())
                cur = StringBuilder()
            } else {
                cur.append(c)
            }
            i++
        }
        tokens.add(cur.toString().trim())
        return tokens
    }

    companion object {
        val sampleRecords = listOf(
            DataRecord(
                code = "REC-1001",
                name = "Eleanor Vance",
                roleOrTitle = "Principal Solutions Architect",
                category = "Client",
                email = "e.vance@aurorasystems.org",
                phone = "+1 (555) 234-8901",
                status = "Active",
                valueAmount = 148500.0,
                priority = "High",
                entryDate = System.currentTimeMillis() - 86400000L * 2,
                customTags = "Cloud, SOC-2, Priority-Q4",
                customFieldsJson = """{"Department":"Enterprise Engineering","Contract ID":"CNT-4401","Billing Cycle":"Annual"}""",
                attachmentsJson = """[{"id":"att-1","fileName":"Aurora_Cloud_Architecture_v2.pdf","mimeType":"application/pdf","fileSizeBytes":1452000,"localPath":"","uriString":"","addedAt":1729700000000},{"id":"att-2","fileName":"System_Topology_Diagram.png","mimeType":"image/png","fileSizeBytes":680000,"localPath":"","uriString":"","addedAt":1729701000000}]""",
                documentTitle = "Cloud Infrastructure Master Services Agreement",
                notesRichText = """# Enterprise Cloud Migration Brief
## Client Scope & SLA
Eleanor is directing the Q4 infrastructure modernisation for **Aurora Systems**.
- Current on-prem footprint: 140 virtualized hosts
- Target SLA: 99.99% uptime with multi-region failover
- Security posture: SOC-2 Type II and FedRAMP alignment

### Key Milestones
1. Architecture verification & threat model review
2. Database cutover dry-run scheduled for **November 14**
3. Performance benchmark sign-off with executive committee

> "Ensuring zero-downtime replication across the primary cluster remains our non-negotiable prerequisite."
""",
                createdAt = System.currentTimeMillis() - 86400000L * 14,
                updatedAt = System.currentTimeMillis() - 3600000L * 2
            ),
            DataRecord(
                code = "REC-1002",
                name = "Marcus Sterling",
                roleOrTitle = "VP of Global Procurement",
                category = "Partner",
                email = "marcus.sterling@apexindustrial.com",
                phone = "+1 (555) 781-4432",
                status = "Review",
                valueAmount = 285000.0,
                priority = "High",
                entryDate = System.currentTimeMillis() - 86400000L * 5,
                customTags = "Hardware, Supply Chain, Strategic",
                customFieldsJson = """{"Warehouse Hub":"Chicago-Central","Volume Target":"50,000 units","Discount Tier":"18.5%"}""",
                attachmentsJson = """[{"id":"att-3","fileName":"Supply_Agreement_Draft_v3.pdf","mimeType":"application/pdf","fileSizeBytes":2150000,"localPath":"","uriString":"","addedAt":1729650000000}]""",
                documentTitle = "Hardware Supply Chain Framework",
                notesRichText = """# Apex Industrial Vendor Agreement
## Strategic Objectives
Negotiation round 3 regarding specialized telemetric sensor procurement.
- Volume: 50,000 units per fiscal quarter
- Unit cost discount: 18.5% at tier-2 commitment
- Warranty: 36 months comprehensive on-site replacement

### Current Action Items
- Legal counsel review on Section 14 (indemnification clauses)
- Escalation path agreed for supply bottlenecks
""",
                createdAt = System.currentTimeMillis() - 86400000L * 20,
                updatedAt = System.currentTimeMillis() - 86400000L * 1
            ),
            DataRecord(
                code = "REC-1003",
                name = "Sophia Chen",
                roleOrTitle = "Chief Data Scientist",
                category = "Internal",
                email = "sophia.chen@workplace.internal",
                phone = "+1 (555) 412-9908",
                status = "Active",
                valueAmount = 92000.0,
                priority = "High",
                entryDate = System.currentTimeMillis() - 86400000L * 3,
                customTags = "AI-Model, Neural-Forecasting, R&D",
                customFieldsJson = """{"Compute Cluster":"4x H100","Target Latency":"22ms","Sprint Cycle":"Sprint-44"}""",
                attachmentsJson = """[{"id":"att-4","fileName":"Validation_Metrics_Report.csv","mimeType":"text/csv","fileSizeBytes":45000,"localPath":"","uriString":"","addedAt":1729680000000}]""",
                documentTitle = "Predictive Analytics Engine V3",
                notesRichText = """# Internal R&D: Deep Learning Forecasting
## Summary
Sophia's team completed phase 2 calibration on the neural time-series forecast model.
- Validation MAE reduced by 14.2%
- Latency per inference under **22ms** on edge processors

### Infrastructure Requirements
- 4x H100 GPU cluster access requested for distributed hyperparameter tuning
- Next sprint demo on **Thursday at 10:00 AM EST**
""",
                createdAt = System.currentTimeMillis() - 86400000L * 8,
                updatedAt = System.currentTimeMillis() - 3600000L * 5
            ),
            DataRecord(
                code = "REC-1004",
                name = "Liam O'Connor",
                roleOrTitle = "Logistics Director",
                category = "Vendor",
                email = "l.oconnor@swiftfreight.co",
                phone = "+1 (555) 902-1134",
                status = "Pending",
                valueAmount = 64500.0,
                priority = "Medium",
                entryDate = System.currentTimeMillis() - 86400000L * 4,
                customTags = "Freight, Midwest, Transport",
                customFieldsJson = """{"Carrier Route":"Midwest-East","Fuel Surcharge Cap":"6.5%","Insurance Status":"Pending"}""",
                attachmentsJson = "[]",
                documentTitle = "Cross-Docking Carrier Agreement",
                notesRichText = """# Logistics & Route Optimization Review
- Route: Midwest Corridor to East Coast Hubs
- Fuel surcharge capped at 6.5% base
- Weekly KPI audit cadence established

### Pending Approvals
- Certificate of Insurance renewal pending submission
- Yard management system API integration testing
""",
                createdAt = System.currentTimeMillis() - 86400000L * 5,
                updatedAt = System.currentTimeMillis() - 86400000L * 2
            ),
            DataRecord(
                code = "REC-1005",
                name = "Aria Patel",
                roleOrTitle = "Product Operations Lead",
                category = "Project",
                email = "aria.patel@horizonfin.io",
                phone = "+1 (555) 633-8821",
                status = "Completed",
                valueAmount = 175000.0,
                priority = "Medium",
                entryDate = System.currentTimeMillis() - 86400000L * 15,
                customTags = "Mobile, FinTech, Launched",
                customFieldsJson = """{"Active Users":"1.2M","Crash Free Rate":"99.94%","Platform Rating":"4.7"}""",
                attachmentsJson = "[]",
                documentTitle = "Mobile Banking Re-Platforming Retrospective",
                notesRichText = """# HorizonFin Mobile App Launch Dossier
## Outcome Report
Successfully rolled out to 1.2M active users in October.
- Crash-free sessions: **99.94%**
- App Store rating increased from 3.8 to 4.7 stars
- Adoption of instant transfer feature: 62% in first 14 days
""",
                createdAt = System.currentTimeMillis() - 86400000L * 30,
                updatedAt = System.currentTimeMillis() - 86400000L * 6
            )
        )
    }
}
