package com.example.smarthabitcoach.habits.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.smarthabitcoach.ui.theme.SmartHabitCoachTheme

private const val MAX_EDIT_TITLE_LENGTH = 50
private const val MAX_EDIT_DESCRIPTION_LENGTH = 120

/**
 * Edit Habit Dialog — lets users update an existing habit's title and description.
 *
 * Mirrors CreateHabitDialog structure. Powered by UpdateHabitUseCase in the domain layer.
 */
@Composable
fun EditHabitDialog(
    visible: Boolean,
    title: String,
    description: String,
    onTitleChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
) {
    if (!visible) return

    val focusManager = LocalFocusManager.current
    val titleOverLimit = title.length > MAX_EDIT_TITLE_LENGTH
    val descOverLimit  = description.length > MAX_EDIT_DESCRIPTION_LENGTH
    val isConfirmEnabled = !isLoading && title.isNotBlank() && !titleOverLimit && !descOverLimit

    AlertDialog(
        onDismissRequest = { if (!isLoading) onDismiss() },
        shape = RoundedCornerShape(28.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Edit,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp)
                )
                Text(
                    text = "Edit Habit",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                // Title field
                OutlinedTextField(
                    value = title,
                    onValueChange = { if (it.length <= MAX_EDIT_TITLE_LENGTH + 5) onTitleChange(it) },
                    label = { Text("Habit title") },
                    placeholder = { Text("e.g. Morning Run") },
                    singleLine = true,
                    isError = titleOverLimit,
                    supportingText = {
                        Text(
                            text = if (titleOverLimit) "Max $MAX_EDIT_TITLE_LENGTH characters"
                                   else "${title.length} / $MAX_EDIT_TITLE_LENGTH",
                            color = if (titleOverLimit) MaterialTheme.colorScheme.error
                                    else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences,
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = { focusManager.moveFocus(FocusDirection.Down) }
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        focusedLabelColor  = MaterialTheme.colorScheme.primary,
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .semantics { contentDescription = "Edit habit title" }
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Description field
                OutlinedTextField(
                    value = description,
                    onValueChange = { if (it.length <= MAX_EDIT_DESCRIPTION_LENGTH + 5) onDescriptionChange(it) },
                    label = { Text("Description (optional)") },
                    placeholder = { Text("Add more details...") },
                    singleLine = false,
                    maxLines = 3,
                    isError = descOverLimit,
                    supportingText = {
                        Text(
                            text = if (descOverLimit) "Max $MAX_EDIT_DESCRIPTION_LENGTH characters"
                                   else "${description.length} / $MAX_EDIT_DESCRIPTION_LENGTH",
                            color = if (descOverLimit) MaterialTheme.colorScheme.error
                                    else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = { focusManager.clearFocus(); if (isConfirmEnabled) onConfirm() }
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        focusedLabelColor  = MaterialTheme.colorScheme.primary,
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .semantics { contentDescription = "Edit habit description" }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                enabled = isConfirmEnabled,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                modifier = Modifier.padding(end = 4.dp, bottom = 4.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Save", fontWeight = FontWeight.SemiBold)
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isLoading,
                modifier = Modifier.padding(bottom = 4.dp)
            ) {
                Text("Cancel")
            }
        },
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
private fun EditHabitDialogPreview() {
    SmartHabitCoachTheme {
        EditHabitDialog(
            visible = true,
            title = "Morning Run",
            description = "5 km outdoor",
            onTitleChange = {},
            onDescriptionChange = {},
            onConfirm = {},
            onDismiss = {}
        )
    }
}

