package com.example.util

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.sp
import kotlin.math.max

object RichTextFormatter {

    fun parseMarkdownToAnnotatedString(
        rawText: String,
        primaryColor: Color,
        onSurfaceColor: Color,
        surfaceVariantColor: Color
    ): AnnotatedString {
        if (rawText.isBlank()) return AnnotatedString("")

        return buildAnnotatedString {
            val lines = rawText.lines()
            lines.forEachIndexed { index, line ->
                val trimmed = line.trimStart()

                when {
                    // Heading 1
                    trimmed.startsWith("# ") -> {
                        val content = trimmed.removePrefix("# ")
                        val start = length
                        append(content)
                        addStyle(
                            SpanStyle(
                                color = primaryColor,
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp
                            ),
                            start,
                            length
                        )
                    }
                    // Heading 2
                    trimmed.startsWith("## ") -> {
                        val content = trimmed.removePrefix("## ")
                        val start = length
                        append(content)
                        addStyle(
                            SpanStyle(
                                color = primaryColor,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 17.sp
                            ),
                            start,
                            length
                        )
                    }
                    // Heading 3
                    trimmed.startsWith("### ") -> {
                        val content = trimmed.removePrefix("### ")
                        val start = length
                        append(content)
                        addStyle(
                            SpanStyle(
                                color = onSurfaceColor,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 15.sp
                            ),
                            start,
                            length
                        )
                    }
                    // Blockquote
                    trimmed.startsWith("> ") -> {
                        val content = trimmed.removePrefix("> ")
                        val start = length
                        append("▎ $content")
                        addStyle(
                            SpanStyle(
                                color = primaryColor.copy(alpha = 0.85f),
                                fontStyle = FontStyle.Italic,
                                fontSize = 14.sp
                            ),
                            start,
                            length
                        )
                    }
                    // Bullet Point
                    trimmed.startsWith("- ") || trimmed.startsWith("* ") -> {
                        val content = trimmed.substring(2)
                        val start = length
                        append("  • ")
                        addStyle(
                            SpanStyle(
                                color = primaryColor,
                                fontWeight = FontWeight.Bold
                            ),
                            start,
                            length
                        )
                        appendInlineStyledText(content, primaryColor, onSurfaceColor, surfaceVariantColor)
                    }
                    // Numbered List
                    trimmed.matches(Regex("""^\d+\.\s.*""")) -> {
                        val dotIndex = trimmed.indexOf('.')
                        val numberPrefix = trimmed.substring(0, dotIndex + 2)
                        val content = trimmed.substring(dotIndex + 2)
                        val start = length
                        append("  $numberPrefix")
                        addStyle(
                            SpanStyle(
                                color = primaryColor,
                                fontWeight = FontWeight.Bold
                            ),
                            start,
                            length
                        )
                        appendInlineStyledText(content, primaryColor, onSurfaceColor, surfaceVariantColor)
                    }
                    // Regular paragraph
                    else -> {
                        appendInlineStyledText(line, primaryColor, onSurfaceColor, surfaceVariantColor)
                    }
                }

                if (index < lines.size - 1) {
                    append("\n")
                }
            }
        }
    }

    private fun AnnotatedString.Builder.appendInlineStyledText(
        text: String,
        primaryColor: Color,
        onSurfaceColor: Color,
        surfaceVariantColor: Color
    ) {
        var cursor = 0
        while (cursor < text.length) {
            // Check Bold **text**
            if (cursor + 1 < text.length && text.startsWith("**", cursor)) {
                val end = text.indexOf("**", cursor + 2)
                if (end != -1) {
                    val boldContent = text.substring(cursor + 2, end)
                    val startIdx = length
                    append(boldContent)
                    addStyle(SpanStyle(fontWeight = FontWeight.Bold), startIdx, length)
                    cursor = end + 2
                    continue
                }
            }

            // Check Underline <u>text</u>
            if (cursor + 3 < text.length && text.startsWith("<u>", cursor, ignoreCase = true)) {
                val end = text.indexOf("</u>", cursor + 3, ignoreCase = true)
                if (end != -1) {
                    val underlineContent = text.substring(cursor + 3, end)
                    val startIdx = length
                    append(underlineContent)
                    addStyle(SpanStyle(textDecoration = TextDecoration.Underline), startIdx, length)
                    cursor = end + 4
                    continue
                }
            }

            // Check Italic *text*
            if (text[cursor] == '*' && cursor + 1 < text.length && text[cursor + 1] != '*') {
                val end = text.indexOf('*', cursor + 1)
                if (end != -1) {
                    val italicContent = text.substring(cursor + 1, end)
                    val startIdx = length
                    append(italicContent)
                    addStyle(SpanStyle(fontStyle = FontStyle.Italic), startIdx, length)
                    cursor = end + 1
                    continue
                }
            }

            // Check Inline Code `code`
            if (text[cursor] == '`') {
                val end = text.indexOf('`', cursor + 1)
                if (end != -1) {
                    val codeContent = text.substring(cursor + 1, end)
                    val startIdx = length
                    append(" $codeContent ")
                    addStyle(
                        SpanStyle(
                            fontFamily = FontFamily.Monospace,
                            background = surfaceVariantColor.copy(alpha = 0.6f),
                            color = primaryColor,
                            fontSize = 13.sp
                        ),
                        startIdx,
                        length
                    )
                    cursor = end + 1
                    continue
                }
            }

            append(text[cursor])
            cursor++
        }
    }

    fun applyWrapFormatting(
        tfv: TextFieldValue,
        prefix: String,
        suffix: String,
        defaultPlaceholder: String = "text"
    ): TextFieldValue {
        val text = tfv.text
        val selection = tfv.selection

        return if (selection.collapsed) {
            val inserted = "$prefix$defaultPlaceholder$suffix"
            val newText = text.substring(0, selection.start) + inserted + text.substring(selection.start)
            val newSelectionStart = selection.start + prefix.length
            val newSelectionEnd = newSelectionStart + defaultPlaceholder.length
            TextFieldValue(
                text = newText,
                selection = androidx.compose.ui.text.TextRange(newSelectionStart, newSelectionEnd)
            )
        } else {
            val selectedText = text.substring(selection.min, selection.max)
            val wrapped = "$prefix$selectedText$suffix"
            val newText = text.substring(0, selection.min) + wrapped + text.substring(selection.max)
            TextFieldValue(
                text = newText,
                selection = androidx.compose.ui.text.TextRange(selection.min + prefix.length, selection.min + prefix.length + selectedText.length)
            )
        }
    }

    fun applyLinePrefix(
        tfv: TextFieldValue,
        linePrefix: String
    ): TextFieldValue {
        val text = tfv.text
        val selection = tfv.selection

        val lineStart = text.lastIndexOf('\n', max(0, selection.min - 1)).let { if (it == -1) 0 else it + 1 }
        val newText = text.substring(0, lineStart) + linePrefix + text.substring(lineStart)
        val newCursor = selection.start + linePrefix.length

        return TextFieldValue(
            text = newText,
            selection = androidx.compose.ui.text.TextRange(newCursor)
        )
    }

    fun calculateStats(text: String): Triple<Int, Int, Int> {
        val wordCount = if (text.isBlank()) 0 else text.trim().split(Regex("""\s+""")).count { it.isNotBlank() }
        val charCount = text.length
        val readingMinutes = max(1, (wordCount / 180))
        return Triple(wordCount, charCount, readingMinutes)
    }
}
