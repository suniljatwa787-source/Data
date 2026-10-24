package com.example.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DataRecord
import com.example.ui.model.SaveStatus
import com.example.util.RichTextFormatter
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DocumentWorkspaceView(
    activeRecord: DataRecord?,
    allRecords: List<DataRecord>,
    saveStatus: SaveStatus,
    onSelectRecord: (Long) -> Unit,
    onAutoSave: (recordId: Long, title: String, notes: String) -> Unit,
    onManualSave: (recordId: Long, title: String, notes: String) -> Unit,
    onAddFileUris: (recordId: Long, uris: List<Uri>) -> Unit,
    onRemoveAttachment: (recordId: Long, attachmentId: String) -> Unit,
    onAddTag: (recordId: Long, tag: String) -> Unit,
    onRemoveTag: (recordId: Long, tag: String) -> Unit,
    onAddCustomField: (recordId: Long, key: String, value: String) -> Unit,
    onRemoveCustomField: (recordId: Long, key: String) -> Unit,
    isDark: Boolean,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) }

    var recordDropdownExpanded by remember { mutableStateOf(false) }

    if (activeRecord == null) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "No Record Selected",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Select a record from the spreadsheet to open its document workspace.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        return
    }

    val tabs = listOf(
        "Rich Editor",
        "Document Dossier",
        "Files (${activeRecord.attachments.size})",
        "Specs & Tags"
    )

    var titleText by remember(activeRecord.id) { mutableStateOf(activeRecord.documentTitle) }
    var notesFieldValue by remember(activeRecord.id) {
        mutableStateOf(TextFieldValue(activeRecord.notesRichText))
    }

    fun handleTextChange(newTfv: TextFieldValue) {
        notesFieldValue = newTfv
        onAutoSave(activeRecord.id, titleText, newTfv.text)
    }

    fun handleTitleChange(newTitle: String) {
        titleText = newTitle
        onAutoSave(activeRecord.id, newTitle, notesFieldValue.text)
    }

    val stats = remember(notesFieldValue.text) {
        RichTextFormatter.calculateStats(notesFieldValue.text)
    }

    val currencyFormatter = remember { NumberFormat.getCurrencyInstance(Locale.US).apply { maximumFractionDigits = 0 } }
    val dateFormatter = remember { SimpleDateFormat("MMM d, yyyy 'at' h:mm a", Locale.getDefault()) }
    val shortDateFormatter = remember { SimpleDateFormat("MMM d, yyyy", Locale.getDefault()) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("document_workspace_view")
    ) {
        // Workspace Header Bar with Record Switcher & Save Status
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 2.dp
        ) {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Record Selector Pill
                    Box {
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f))
                                .clickable { recordDropdownExpanded = true }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                                .testTag("workspace_record_selector"),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${activeRecord.code} • ${activeRecord.name}",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = "Switch record",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        DropdownMenu(
                            expanded = recordDropdownExpanded,
                            onDismissRequest = { recordDropdownExpanded = false }
                        ) {
                            allRecords.forEach { record ->
                                DropdownMenuItem(
                                    text = {
                                        Column {
                                            Text(
                                                text = "${record.code} • ${record.name}",
                                                fontWeight = if (record.id == activeRecord.id) FontWeight.Bold else FontWeight.Normal
                                            )
                                            Text(
                                                text = record.roleOrTitle,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    },
                                    onClick = {
                                        onSelectRecord(record.id)
                                        recordDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    // Save Status Indicator & Manual Save
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        when (saveStatus) {
                            is SaveStatus.Saving -> {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(14.dp),
                                    strokeWidth = 2.dp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Saving...",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            is SaveStatus.Saved -> {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Auto-saved",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            is SaveStatus.Unsaved -> {
                                Text(
                                    text = "Unsaved",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        // Manual Save Button
                        Button(
                            onClick = {
                                onManualSave(activeRecord.id, titleText, notesFieldValue.text)
                            },
                            modifier = Modifier
                                .height(32.dp)
                                .testTag("manual_save_button"),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 0.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Save,
                                contentDescription = "Save now",
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Save", fontSize = 12.sp)
                        }

                        Spacer(modifier = Modifier.width(6.dp))

                        // Share Action
                        IconButton(
                            onClick = {
                                val shareText = buildString {
                                    appendLine("=== ${titleText.ifBlank { "Document Dossier" }} ===")
                                    appendLine("Subject: ${activeRecord.name} (${activeRecord.code})")
                                    appendLine("Role: ${activeRecord.roleOrTitle} | Category: ${activeRecord.category}")
                                    appendLine("Status: ${activeRecord.status} | Value: ${currencyFormatter.format(activeRecord.valueAmount)}")
                                    appendLine("Date: ${shortDateFormatter.format(Date(activeRecord.entryDate))}")
                                    if (activeRecord.tagsList.isNotEmpty()) {
                                        appendLine("Tags: ${activeRecord.tagsList.joinToString(", ")}")
                                    }
                                    if (activeRecord.customFields.isNotEmpty()) {
                                        appendLine("Fields: ${activeRecord.customFields.map { "${it.key}: ${it.value}" }.joinToString(" | ")}")
                                    }
                                    appendLine("\n--- Document Notes ---")
                                    appendLine(notesFieldValue.text)
                                }
                                val sendIntent = Intent().apply {
                                    action = Intent.ACTION_SEND
                                    putExtra(Intent.EXTRA_TEXT, shareText)
                                    putExtra(Intent.EXTRA_TITLE, titleText)
                                    type = "text/plain"
                                }
                                val shareIntent = Intent.createChooser(sendIntent, "Share Document Dossier")
                                context.startActivity(shareIntent)
                            },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "Share dossier",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Metadata Subtitle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CategoryBadge(category = activeRecord.category)
                        Spacer(modifier = Modifier.width(8.dp))
                        StatusBadge(status = activeRecord.status, isDark = isDark)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = shortDateFormatter.format(Date(activeRecord.entryDate)),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 11.sp
                        )
                    }
                    Text(
                        text = "${stats.first} words • ~${stats.third} min read",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp
                    )
                }
            }
        }

        // Segmented Tabs
        ScrollableTabRow(
            selectedTabIndex = selectedTab,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary,
            edgePadding = 12.dp
        ) {
            tabs.forEachIndexed { index, tabTitle ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = {
                        Text(
                            text = tabTitle,
                            fontSize = 13.sp,
                            fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    modifier = Modifier.testTag("workspace_tab_$index")
                )
            }
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

        // Tab Content
        Box(modifier = Modifier.fillMaxSize()) {
            when (selectedTab) {
                0 -> {
                    // TAB 0: Rich Text Editor
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(14.dp)
                    ) {
                        OutlinedTextField(
                            value = titleText,
                            onValueChange = { handleTitleChange(it) },
                            label = { Text("Document Title") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("doc_title_input"),
                            singleLine = true,
                            shape = RoundedCornerShape(8.dp)
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Formatting Toolbar
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("rich_text_toolbar"),
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState())
                                    .padding(horizontal = 6.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                ToolbarButton(
                                    label = "B",
                                    fontWeight = FontWeight.Bold,
                                    onClick = {
                                        handleTextChange(
                                            RichTextFormatter.applyWrapFormatting(notesFieldValue, "**", "**", "bold text")
                                        )
                                    }
                                )

                                ToolbarButton(
                                    label = "I",
                                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                                    onClick = {
                                        handleTextChange(
                                            RichTextFormatter.applyWrapFormatting(notesFieldValue, "*", "*", "italic text")
                                        )
                                    }
                                )

                                ToolbarButton(
                                    label = "U",
                                    textDecoration = androidx.compose.ui.text.style.TextDecoration.Underline,
                                    onClick = {
                                        handleTextChange(
                                            RichTextFormatter.applyWrapFormatting(notesFieldValue, "<u>", "</u>", "underlined text")
                                        )
                                    }
                                )

                                Spacer(modifier = Modifier.width(4.dp))
                                Box(
                                    modifier = Modifier
                                        .height(18.dp)
                                        .width(1.dp)
                                        .background(MaterialTheme.colorScheme.outlineVariant)
                                )
                                Spacer(modifier = Modifier.width(4.dp))

                                ToolbarButton(
                                    label = "H1",
                                    fontWeight = FontWeight.Bold,
                                    onClick = {
                                        handleTextChange(
                                            RichTextFormatter.applyLinePrefix(notesFieldValue, "# ")
                                        )
                                    }
                                )

                                ToolbarButton(
                                    label = "H2",
                                    fontWeight = FontWeight.SemiBold,
                                    onClick = {
                                        handleTextChange(
                                            RichTextFormatter.applyLinePrefix(notesFieldValue, "## ")
                                        )
                                    }
                                )

                                ToolbarButton(
                                    label = "• List",
                                    onClick = {
                                        handleTextChange(
                                            RichTextFormatter.applyLinePrefix(notesFieldValue, "- ")
                                        )
                                    }
                                )

                                ToolbarButton(
                                    label = "” Quote",
                                    onClick = {
                                        handleTextChange(
                                            RichTextFormatter.applyLinePrefix(notesFieldValue, "> ")
                                        )
                                    }
                                )

                                ToolbarButton(
                                    label = "</> Code",
                                    fontFamily = FontFamily.Monospace,
                                    onClick = {
                                        handleTextChange(
                                            RichTextFormatter.applyWrapFormatting(notesFieldValue, "`", "`", "code")
                                        )
                                    }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = notesFieldValue,
                            onValueChange = { handleTextChange(it) },
                            placeholder = { Text("Write notes, documentation, project scopes or memos here...") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .testTag("rich_text_editor_input"),
                            shape = RoundedCornerShape(8.dp)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Markdown tags supported: **bold**, *italic*, <u>underline</u>, # headings, - bullets",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 11.sp
                            )
                            Text(
                                text = "${stats.second} chars",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 11.sp
                            )
                        }
                    }
                }

                1 -> {
                    // TAB 1: Document Dossier (Rendered Executive Preview)
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp)
                            .testTag("document_dossier_preview")
                    ) {
                        // Executive Document Header Card
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(modifier = Modifier.padding(18.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = titleText.ifBlank { "Executive Workspace Dossier" },
                                            style = MaterialTheme.typography.headlineSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "${activeRecord.name} • ${activeRecord.roleOrTitle}",
                                            style = MaterialTheme.typography.titleMedium,
                                            color = MaterialTheme.colorScheme.primary,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                    StatusBadge(status = activeRecord.status, isDark = isDark)
                                }

                                Spacer(modifier = Modifier.height(14.dp))
                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                                Spacer(modifier = Modifier.height(14.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text(
                                            text = "RECORD ID",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = activeRecord.code,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                    Column {
                                        Text(
                                            text = "CATEGORY",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = activeRecord.category,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                    Column {
                                        Text(
                                            text = "VALUE / BUDGET",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = currencyFormatter.format(activeRecord.valueAmount),
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                    Column {
                                        Text(
                                            text = "RECORD DATE",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = shortDateFormatter.format(Date(activeRecord.entryDate)),
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }

                                // Tags pills in header
                                if (activeRecord.tagsList.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        activeRecord.tagsList.forEach { tag ->
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                                                    .padding(horizontal = 7.dp, vertical = 2.dp)
                                            ) {
                                                Text(
                                                    text = "#$tag",
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Medium,
                                                    color = MaterialTheme.colorScheme.primary
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Formatted Rendered Notes Sheet
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(modifier = Modifier.padding(20.dp)) {
                                Text(
                                    text = "DOCUMENT MEMORANDUM & SCOPE",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(12.dp))

                                val renderedAnnotated = RichTextFormatter.parseMarkdownToAnnotatedString(
                                    rawText = notesFieldValue.text,
                                    primaryColor = MaterialTheme.colorScheme.primary,
                                    onSurfaceColor = MaterialTheme.colorScheme.onSurface,
                                    surfaceVariantColor = MaterialTheme.colorScheme.surfaceVariant
                                )

                                Text(
                                    text = renderedAnnotated,
                                    style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 22.sp),
                                    color = MaterialTheme.colorScheme.onSurface
                                )

                                Spacer(modifier = Modifier.height(20.dp))
                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                                Spacer(modifier = Modifier.height(10.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "Last saved: ${dateFormatter.format(Date(activeRecord.updatedAt))}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 11.sp
                                    )
                                    Text(
                                        text = "${activeRecord.attachments.size} attachments",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                }

                2 -> {
                    // TAB 2: Files & Attachments Section
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp)
                    ) {
                        AttachmentSection(
                            attachments = activeRecord.attachments,
                            onAddFileUris = { uris -> onAddFileUris(activeRecord.id, uris) },
                            onRemoveAttachment = { attId -> onRemoveAttachment(activeRecord.id, attId) }
                        )
                    }
                }

                3 -> {
                    // TAB 3: Specs, Custom Fields & Tags Manager
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp)
                    ) {
                        CustomFieldsAndTagsSection(
                            tags = activeRecord.tagsList,
                            customFields = activeRecord.customFields,
                            onAddTag = { tag -> onAddTag(activeRecord.id, tag) },
                            onRemoveTag = { tag -> onRemoveTag(activeRecord.id, tag) },
                            onAddCustomField = { k, v -> onAddCustomField(activeRecord.id, k, v) },
                            onRemoveCustomField = { k -> onRemoveCustomField(activeRecord.id, k) }
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(modifier = Modifier.padding(18.dp)) {
                                Text(
                                    text = "SYSTEM IDENTIFICATION",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(12.dp))

                                SpecRow(label = "Internal Database ID", value = "#${activeRecord.id}")
                                SpecRow(label = "System Code", value = activeRecord.code)
                                SpecRow(label = "Full Name", value = activeRecord.name)
                                SpecRow(label = "Job Title / Role", value = activeRecord.roleOrTitle)
                                SpecRow(label = "Email Address", value = activeRecord.email)
                                SpecRow(label = "Phone Contact", value = activeRecord.phone)
                                SpecRow(label = "Organizational Category", value = activeRecord.category)
                                SpecRow(label = "Operational Status", value = activeRecord.status)
                                SpecRow(label = "Budget Allocation", value = currencyFormatter.format(activeRecord.valueAmount))
                                SpecRow(label = "Priority Level", value = activeRecord.priority)
                                SpecRow(label = "Record Date", value = shortDateFormatter.format(Date(activeRecord.entryDate)))
                                SpecRow(label = "Created Timestamp", value = dateFormatter.format(Date(activeRecord.createdAt)))
                                SpecRow(label = "Last Modified", value = dateFormatter.format(Date(activeRecord.updatedAt)))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ToolbarButton(
    label: String,
    fontWeight: FontWeight = FontWeight.Normal,
    fontStyle: androidx.compose.ui.text.font.FontStyle = androidx.compose.ui.text.font.FontStyle.Normal,
    fontFamily: FontFamily? = null,
    textDecoration: androidx.compose.ui.text.style.TextDecoration? = null,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .clickable(onClick = onClick),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 9.dp, vertical = 6.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = fontWeight,
                fontStyle = fontStyle,
                fontFamily = fontFamily,
                textDecoration = textDecoration,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun SpecRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f), thickness = 0.5.dp)
}
