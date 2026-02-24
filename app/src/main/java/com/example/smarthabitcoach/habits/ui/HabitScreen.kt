package com.example.smarthabitcoach.habits.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.smarthabitcoach.domain.model.Habit
import com.example.smarthabitcoach.habits.HabitViewModel
import com.example.smarthabitcoach.ui.theme.SmartHabitCoachTheme

/**
 * Main screen for Habit management feature.
 *
 * Architecture:
 * - Observes uiState from ViewModel as StateFlow
 * - Delegates all user interactions to ViewModel via onEvent
 * - No business logic in composables
 * - Pure function composition pattern
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitScreen(
    viewModel: HabitViewModel,
    modifier: Modifier = Modifier
) {
    // Collect UI state from ViewModel
    val uiState = viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    // Show snackbar when error occurs
    LaunchedEffect(uiState.value.error) {
        uiState.value.error?.let { error ->
            snackbarHostState.showSnackbar(
                message = error,
                duration = SnackbarDuration.Long
            )
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("My Habits") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    viewModel.onEvent(HabitUiEvent.ShowCreateDialog)
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Habit")
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (uiState.value.isLoading && uiState.value.habits.isEmpty()) {
                // Loading state
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
// ...existing code...
            } else if (uiState.value.habits.isEmpty()) {
                // Empty state
                EmptyHabitsPlaceholder(
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                // Habit list with statistics
                Column(modifier = Modifier.fillMaxSize()) {
                    StatisticsCard(statistics = uiState.value.statistics)

                    HabitList(
                        habits = uiState.value.habits,
                        onCompleteHabit = { habitId, completed ->
                            viewModel.onEvent(HabitUiEvent.CompleteHabit(habitId, completed))
                        },
                        onDeleteHabit = { habitId ->
                            viewModel.onEvent(HabitUiEvent.DeleteHabit(habitId))
                        }
                    )
                }
            }
// ...existing code...

            // Create Habit Dialog
            CreateHabitDialog(
                visible = uiState.value.createDialogVisible,
                title = uiState.value.newHabitTitle,
                description = uiState.value.newHabitDescription,
                onTitleChange = { newTitle ->
                    viewModel.onEvent(HabitUiEvent.UpdateNewHabitTitle(newTitle))
                },
                onDescriptionChange = { newDesc ->
                    viewModel.onEvent(HabitUiEvent.UpdateNewHabitDescription(newDesc))
                },
                onConfirm = {
                    viewModel.onEvent(
                        HabitUiEvent.CreateHabit(
                            title = uiState.value.newHabitTitle,
                            description = uiState.value.newHabitDescription
                        )
                    )
                },
                onDismiss = {
                    viewModel.onEvent(HabitUiEvent.HideCreateDialog)
                },
                isLoading = uiState.value.isLoading
            )

            // Error dismissal
            LaunchedEffect(Unit) {
                // Auto-clear error after showing snackbar
            }
        }
    }
}

/**
 * Habit list composable.
 * Displays list of habits in a LazyColumn for efficient rendering.
 */
@Composable
fun HabitList(
    habits: List<Habit>,
    onCompleteHabit: (habitId: String, completed: Boolean) -> Unit,
    onDeleteHabit: (habitId: String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(0.dp),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 8.dp)
    ) {
        items(
            items = habits,
            key = { it.id }
        ) { habit ->
            HabitListItem(
                habit = habit,
                onComplete = onCompleteHabit,
                onDelete = onDeleteHabit
            )
        }
    }
}

/**
 * Empty state placeholder.
 * Shown when no habits exist.
 */
@Composable
fun EmptyHabitsPlaceholder(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "No habits yet",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = "Tap the + button to create your first habit",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun HabitListPreview() {
    SmartHabitCoachTheme {
        HabitList(
            habits = listOf(
                Habit(id = "1", title = "Morning Exercise", description = "30 min cardio", isCompleted = false),
                Habit(id = "2", title = "Read", description = "20 pages", isCompleted = true),
                Habit(id = "3", title = "Meditate", isCompleted = false)
            ),
            onCompleteHabit = { _, _ -> },
            onDeleteHabit = { }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun EmptyHabitsPlaceholderPreview() {
    SmartHabitCoachTheme {
        EmptyHabitsPlaceholder()
    }
}

