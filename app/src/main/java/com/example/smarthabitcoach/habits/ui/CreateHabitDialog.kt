package com.example.smarthabitcoach.habits.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
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
import androidx.compose.material.icons.rounded.AutoAwesome
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

private const val MAX_TITLE_LENGTH = 50
private const val MAX_DESCRIPTION_LENGTH = 120

/**
 * Create Habit Dialog — production-grade implementation.
 *
 * Features:
 * - Character counter for title (max 50)
 * - Optional description with max 120 chars
 * - Keyboard IME actions (Next / Done)
 * - Confirm disabled until valid title entered
 * - Loading spinner in confirm button
 */
@Composable
fun CreateHabitDialog(
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
    val titleOverLimit = title.length > MAX_TITLE_LENGTH
    val descOverLimit  = description.length > MAX_DESCRIPTION_LENGTH
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
                    imageVector = Icons.Rounded.AutoAwesome,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp)
                )
                Text(
                    text = "New Habit",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                // ── Title field ────────────────────────────────────────────
                OutlinedTextField(
                    value = title,
                    onValueChange = { if (it.length <= MAX_TITLE_LENGTH + 5) onTitleChange(it) },
                    label = { Text("Habit title *") },
                    placeholder = { Text("e.g. Morning run") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .semantics { contentDescription = "Habit title input, required" },
                    singleLine = true,
                    enabled = !isLoading,
                    isError = titleOverLimit,
                    shape = RoundedCornerShape(14.dp),
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences,
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = { focusManager.moveFocus(FocusDirection.Down) }
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    )
                )

                // Title char counter
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    Text(
                        text = "${title.length}/$MAX_TITLE_LENGTH",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (titleOverLimit)
                            MaterialTheme.colorScheme.error
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // ── Description field ──────────────────────────────────────
                OutlinedTextField(
                    value = description,
                    onValueChange = { if (it.length <= MAX_DESCRIPTION_LENGTH + 5) onDescriptionChange(it) },
                    label = { Text("Description") },
                    placeholder = { Text("e.g. 30 min outdoor") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .semantics { contentDescription = "Habit description input, optional" },
                    maxLines = 3,
                    enabled = !isLoading,
                    isError = descOverLimit,
                    shape = RoundedCornerShape(14.dp),
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            focusManager.clearFocus()
                            if (isConfirmEnabled) onConfirm()
                        }
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    )
                )

                // Description char counter (only show when user types)
                AnimatedVisibility(
                    visible = description.isNotEmpty(),
                    enter = fadeIn(tween(200)) + expandVertically(tween(200))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Text(
                            text = "${description.length}/$MAX_DESCRIPTION_LENGTH",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (descOverLimit)
                                MaterialTheme.colorScheme.error
                            else
                                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    }
                }

                // Helper text
                Text(
                    text = "Tip: Keep it specific and achievable",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f),
                    modifier = Modifier.padding(top = 6.dp, start = 4.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                enabled = isConfirmEnabled,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.38f)
                )
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Create Habit", fontWeight = FontWeight.SemiBold)
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isLoading
            ) {
                Text("Cancel")
            }
        },
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
private fun CreateHabitDialogPreview() {
    SmartHabitCoachTheme {
        CreateHabitDialog(
            visible = true,
            title = "Morning Jog",
            description = "5 km outdoor run",
            onTitleChange = {},
            onDescriptionChange = {},
            onConfirm = {},
            onDismiss = {}
        )
    }
}

@Preview(showBackground = true, name = "Loading state")
@Composable
private fun CreateHabitDialogLoadingPreview() {
    SmartHabitCoachTheme {
        CreateHabitDialog(
            visible = true,
            title = "Morning Jog",
            description = "",
            onTitleChange = {},
            onDescriptionChange = {},
            onConfirm = {},
            onDismiss = {},
            isLoading = true
        )
    }
}
