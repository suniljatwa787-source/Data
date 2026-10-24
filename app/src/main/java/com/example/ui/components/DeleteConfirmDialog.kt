package com.example.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.testTag

@Composable
fun DeleteConfirmDialog(
    itemCount: Int,
    singleItemName: String? = null,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    val title = if (itemCount > 1) "Delete $itemCount Records?" else "Delete Record?"
    val message = if (itemCount > 1) {
        "Are you sure you want to delete these $itemCount selected records? This action cannot be undone."
    } else {
        "Are you sure you want to delete \"${singleItemName ?: "this record"}\"? All associated document notes and history will be permanently removed."
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error
            )
        },
        title = {
            Text(text = title)
        },
        text = {
            Text(text = message)
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm()
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError
                ),
                modifier = androidx.compose.ui.Modifier.testTag("confirm_delete_button")
            ) {
                Text(if (itemCount > 1) "Delete $itemCount Records" else "Delete Record")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = androidx.compose.ui.Modifier.testTag("cancel_delete_button")
            ) {
                Text("Cancel")
            }
        }
    )
}
