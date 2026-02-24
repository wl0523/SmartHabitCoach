package com.example.smarthabitcoach.habits.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.smarthabitcoach.domain.model.Habit
import com.example.smarthabitcoach.ui.theme.SmartHabitCoachTheme

/**
 * Reusable habit list item component.
 * Displays a single habit with checkbox, title, description, and delete button.
 */
@Composable
fun HabitListItem(
    habit: Habit,
    onComplete: (habitId: String, completed: Boolean) -> Unit,
    onDelete: (habitId: String) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Checkbox + Title/Description
            Row(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start
            ) {
                Checkbox(
                    checked = habit.isCompleted,
                    onCheckedChange = { isChecked ->
                        onComplete(habit.id, isChecked)
                    },
                    modifier = Modifier.padding(end = 8.dp)
                )

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = habit.title,
                        style = MaterialTheme.typography.bodyLarge,
                        textDecoration = if (habit.isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                        color = if (habit.isCompleted) MaterialTheme.colorScheme.onSurfaceVariant
                                else MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    habit.description?.let { desc ->
                        if (desc.isNotBlank()) {
                            Text(
                                text = desc,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }
                    }
                }
            }

            // Delete button
            IconButton(
                onClick = { onDelete(habit.id) },
                modifier = Modifier.padding(end = 4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Delete habit",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HabitListItemPreview() {
    SmartHabitCoachTheme {
        HabitListItem(
            habit = Habit(
                id = "1",
                title = "Morning Exercise",
                description = "30 minutes of cardio",
                isCompleted = false
            ),
            onComplete = { _, _ -> },
            onDelete = { }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun HabitListItemCompletedPreview() {
    SmartHabitCoachTheme {
        HabitListItem(
            habit = Habit(
                id = "2",
                title = "Read a book",
                description = "20 pages",
                isCompleted = true
            ),
            onComplete = { _, _ -> },
            onDelete = { }
        )
    }
}
