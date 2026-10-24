package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.DataRecord
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordEditorDialog(
    initialRecord: DataRecord? = null,
    onDismiss: () -> Unit,
    onSave: (DataRecord) -> Unit
) {
    val isEditing = initialRecord != null
    val dateFormatter = remember { SimpleDateFormat("yyyy-MM-dd", Locale.US) }

    var name by remember { mutableStateOf(initialRecord?.name ?: "") }
    var code by remember { mutableStateOf(initialRecord?.code ?: "REC-${System.currentTimeMillis() % 10000}") }
    var roleOrTitle by remember { mutableStateOf(initialRecord?.roleOrTitle ?: "") }
    var category by remember { mutableStateOf(initialRecord?.category ?: "Client") }
    var email by remember { mutableStateOf(initialRecord?.email ?: "") }
    var phone by remember { mutableStateOf(initialRecord?.phone ?: "") }
    var status by remember { mutableStateOf(initialRecord?.status ?: "Active") }
    var dateString by remember {
        mutableStateOf(
            if (initialRecord != null) dateFormatter.format(Date(initialRecord.entryDate)) else dateFormatter.format(Date())
        )
    }
    var tagsInput by remember { mutableStateOf(initialRecord?.customTags ?: "") }
    var valueAmountText by remember { mutableStateOf(initialRecord?.valueAmount?.toInt()?.toString() ?: "50000") }
    var priority by remember { mutableStateOf(initialRecord?.priority ?: "Medium") }
    var documentTitle by remember { mutableStateOf(initialRecord?.documentTitle ?: "Executive Project Brief") }
    var notesRichText by remember {
        mutableStateOf(
            initialRecord?.notesRichText ?: "# Overview & Objectives\n- Document notes entered here.\n- Support for **bold**, *italic*, and bullet lists."
        )
    }

    var categoryExpanded by remember { mutableStateOf(false) }
    var statusExpanded by remember { mutableStateOf(false) }
    var priorityExpanded by remember { mutableStateOf(false) }

    val categories = listOf("Client", "Partner", "Vendor", "Internal", "Project")
    val statuses = listOf("Active", "Pending", "Review", "Completed", "Archived")
    val priorities = listOf("High", "Medium", "Low")

    var nameError by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .clip(RoundedCornerShape(16.dp))
                .testTag("record_editor_dialog"),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (isEditing) "Edit Record" else "New Record Entry",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (isEditing) "Update entry details in the workspace" else "Add a new row to the spreadsheet database",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_editor_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close dialog"
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Basic Info Section
                Text(
                    text = "IDENTIFICATION & CONTACT",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = code,
                        onValueChange = { code = it },
                        label = { Text("Record ID / Code") },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("input_code"),
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    OutlinedTextField(
                        value = name,
                        onValueChange = {
                            name = it
                            if (it.isNotBlank()) nameError = false
                        },
                        label = { Text("Title / Name *") },
                        isError = nameError,
                        supportingText = if (nameError) { { Text("Name is required") } } else null,
                        modifier = Modifier
                            .weight(2f)
                            .testTag("input_name"),
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = roleOrTitle,
                        onValueChange = { roleOrTitle = it },
                        label = { Text("Role / Job Title / Designation") },
                        modifier = Modifier
                            .weight(1.2f)
                            .testTag("input_role"),
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    OutlinedTextField(
                        value = dateString,
                        onValueChange = { dateString = it },
                        label = { Text("Entry Date (YYYY-MM-DD)") },
                        modifier = Modifier
                            .weight(0.8f)
                            .testTag("input_date"),
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email Address") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("input_email"),
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("Phone Number") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("input_phone"),
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Categorization & Financials
                Text(
                    text = "CLASSIFICATION & VALUE",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth()) {
                    // Category Dropdown
                    Box(modifier = Modifier.weight(1f)) {
                        OutlinedTextField(
                            value = category,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Category") },
                            trailingIcon = {
                                Icon(Icons.Default.ArrowDropDown, contentDescription = "Select category")
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { categoryExpanded = true }
                                .testTag("select_category"),
                            shape = RoundedCornerShape(8.dp)
                        )
                        DropdownMenu(
                            expanded = categoryExpanded,
                            onDismissRequest = { categoryExpanded = false }
                        ) {
                            categories.forEach { cat ->
                                DropdownMenuItem(
                                    text = { Text(cat) },
                                    onClick = {
                                        category = cat
                                        categoryExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Status Dropdown
                    Box(modifier = Modifier.weight(1f)) {
                        OutlinedTextField(
                            value = status,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Status") },
                            trailingIcon = {
                                Icon(Icons.Default.ArrowDropDown, contentDescription = "Select status")
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { statusExpanded = true }
                                .testTag("select_status"),
                            shape = RoundedCornerShape(8.dp)
                        )
                        DropdownMenu(
                            expanded = statusExpanded,
                            onDismissRequest = { statusExpanded = false }
                        ) {
                            statuses.forEach { st ->
                                DropdownMenuItem(
                                    text = { Text(st) },
                                    onClick = {
                                        status = st
                                        statusExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = valueAmountText,
                        onValueChange = { valueAmountText = it },
                        label = { Text("Value / Budget ($)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("input_value"),
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(modifier = Modifier.weight(1f)) {
                        OutlinedTextField(
                            value = priority,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Priority") },
                            trailingIcon = {
                                Icon(Icons.Default.ArrowDropDown, contentDescription = "Select priority")
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { priorityExpanded = true }
                                .testTag("select_priority"),
                            shape = RoundedCornerShape(8.dp)
                        )
                        DropdownMenu(
                            expanded = priorityExpanded,
                            onDismissRequest = { priorityExpanded = false }
                        ) {
                            priorities.forEach { prio ->
                                DropdownMenuItem(
                                    text = { Text(prio) },
                                    onClick = {
                                        priority = prio
                                        priorityExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Custom Tags Input
                OutlinedTextField(
                    value = tagsInput,
                    onValueChange = { tagsInput = it },
                    label = { Text("Tags & Labels (comma-separated, e.g. Q4-Target, Urgent)") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_tags"),
                    singleLine = true,
                    shape = RoundedCornerShape(8.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Document / Word Processing Notes
                Text(
                    text = "DOCUMENT DOSSIER & NOTES",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = documentTitle,
                    onValueChange = { documentTitle = it },
                    label = { Text("Document / Dossier Title") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_doc_title"),
                    singleLine = true,
                    shape = RoundedCornerShape(8.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = notesRichText,
                    onValueChange = { notesRichText = it },
                    label = { Text("Rich Formatted Notes (Markdown supported)") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp)
                        .testTag("input_notes"),
                    shape = RoundedCornerShape(8.dp)
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("cancel_record_button")
                    ) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Button(
                        onClick = {
                            if (name.isBlank()) {
                                nameError = true
                                return@Button
                            }
                            val parsedValue = valueAmountText.replace("$", "").replace(",", "").toDoubleOrNull() ?: 0.0
                            val parsedDate = try {
                                dateFormatter.parse(dateString)?.time ?: System.currentTimeMillis()
                            } catch (_: Exception) {
                                System.currentTimeMillis()
                            }

                            val record = initialRecord?.copy(
                                code = code.ifBlank { "REC-${System.currentTimeMillis() % 10000}" },
                                name = name.trim(),
                                roleOrTitle = roleOrTitle.trim(),
                                category = category,
                                email = email.trim(),
                                phone = phone.trim(),
                                status = status,
                                valueAmount = parsedValue,
                                priority = priority,
                                entryDate = parsedDate,
                                customTags = tagsInput.trim(),
                                documentTitle = documentTitle.trim(),
                                notesRichText = notesRichText
                            ) ?: DataRecord(
                                code = code.ifBlank { "REC-${System.currentTimeMillis() % 10000}" },
                                name = name.trim(),
                                roleOrTitle = roleOrTitle.trim(),
                                category = category,
                                email = email.trim(),
                                phone = phone.trim(),
                                status = status,
                                valueAmount = parsedValue,
                                priority = priority,
                                entryDate = parsedDate,
                                customTags = tagsInput.trim(),
                                documentTitle = documentTitle.trim(),
                                notesRichText = notesRichText
                            )
                            onSave(record)
                            onDismiss()
                        },
                        modifier = Modifier.testTag("save_record_button")
                    ) {
                        Text(if (isEditing) "Save Changes" else "Add Entry")
                    }
                }
            }
        }
    }
}
