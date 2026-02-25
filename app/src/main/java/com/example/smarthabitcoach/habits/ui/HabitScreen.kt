package com.example.smarthabitcoach.habits.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.CheckCircleOutline
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.smarthabitcoach.domain.model.Habit
import com.example.smarthabitcoach.habits.HabitViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitScreen(
    viewModel: HabitViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())

    // Date string for subtitle
    val todayLabel = remember {
        LocalDate.now().format(
            DateTimeFormatter.ofPattern("EEEE, MMMM d", Locale.ENGLISH)
        )
    }

    // Collapse FAB label when habits list is scrolled
    val isFabExpanded by remember {
        derivedStateOf { scrollBehavior.state.collapsedFraction < 0.5f }
    }

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(message = it, duration = SnackbarDuration.Long)
        }
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Smart Habit Coach",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        AnimatedContent(
                            targetState = uiState.statistics.completedToday,
                            transitionSpec = {
                                fadeIn(tween(300)) togetherWith fadeOut(tween(200))
                            },
                            label = "subtitle_anim"
                        ) { completed ->
                            val subtitle = when {
                                uiState.habits.isEmpty() -> todayLabel
                                completed == uiState.statistics.totalHabits && uiState.habits.isNotEmpty() ->
                                    "All done today 🎉"
                                else -> "$todayLabel · $completed/${uiState.statistics.totalHabits} done"
                            }
                            Text(
                                text = subtitle,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                scrollBehavior = scrollBehavior,
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { viewModel.onEvent(HabitUiEvent.ShowCreateDialog) },
                expanded = isFabExpanded,
                icon = { Icon(Icons.Rounded.Add, contentDescription = null) },
                text = { Text("New Habit") },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 4.dp)
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->

        val windowInfo = LocalWindowInfo.current
        val isExpanded = windowInfo.containerSize.width >= 840  // px ~ 600dp for MDPI

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when {
                uiState.isLoading && uiState.habits.isEmpty() -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                isExpanded -> {
                    TwoPaneContent(
                        uiState = uiState,
                        onComplete = { id, done -> viewModel.onEvent(HabitUiEvent.CompleteHabit(id, done)) },
                        onDelete  = { id -> viewModel.onEvent(HabitUiEvent.DeleteHabit(id)) },
                        onEdit    = { habit -> viewModel.onEvent(HabitUiEvent.ShowEditDialog(habit)) }
                    )
                }
                else -> {
                    SinglePaneContent(
                        uiState = uiState,
                        onComplete = { id, done -> viewModel.onEvent(HabitUiEvent.CompleteHabit(id, done)) },
                        onDelete  = { id -> viewModel.onEvent(HabitUiEvent.DeleteHabit(id)) },
                        onEdit    = { habit -> viewModel.onEvent(HabitUiEvent.ShowEditDialog(habit)) }
                    )
                }
            }

            CreateHabitDialog(
                visible             = uiState.createDialogVisible,
                title               = uiState.newHabitTitle,
                description         = uiState.newHabitDescription,
                onTitleChange       = { viewModel.onEvent(HabitUiEvent.UpdateNewHabitTitle(it)) },
                onDescriptionChange = { viewModel.onEvent(HabitUiEvent.UpdateNewHabitDescription(it)) },
                onConfirm = {
                    viewModel.onEvent(
                        HabitUiEvent.CreateHabit(
                            title       = uiState.newHabitTitle,
                            description = uiState.newHabitDescription
                        )
                    )
                },
                onDismiss  = { viewModel.onEvent(HabitUiEvent.HideCreateDialog) },
                isLoading  = uiState.isLoading
            )

            EditHabitDialog(
                visible             = uiState.editDialogHabit != null,
                title               = uiState.editHabitTitle,
                description         = uiState.editHabitDescription,
                onTitleChange       = { viewModel.onEvent(HabitUiEvent.UpdateEditHabitTitle(it)) },
                onDescriptionChange = { viewModel.onEvent(HabitUiEvent.UpdateEditHabitDescription(it)) },
                onConfirm = {
                    uiState.editDialogHabit?.let { habit ->
                        viewModel.onEvent(
                            HabitUiEvent.UpdateHabit(
                                habitId     = habit.id,
                                title       = uiState.editHabitTitle,
                                description = uiState.editHabitDescription
                            )
                        )
                    }
                },
                onDismiss  = { viewModel.onEvent(HabitUiEvent.HideEditDialog) },
                isLoading  = uiState.isLoading
            )
        }
    }
}

// ── Single pane ───────────────────────────────────────────────────────────────
@Composable
private fun SinglePaneContent(
    uiState: HabitUiState,
    onComplete: (String, Boolean) -> Unit,
    onDelete: (String) -> Unit,
    onEdit: (com.example.smarthabitcoach.domain.model.Habit) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        AnimatedVisibility(
            visible = uiState.habits.isNotEmpty(),
            enter = fadeIn(tween(400)) + expandVertically(tween(400)),
            exit  = fadeOut(tween(200)) + shrinkVertically(tween(200))
        ) {
            Column {
                StatisticsCard(statistics = uiState.statistics)
                AtRiskHabitsCard(atRiskHabits = uiState.atRiskHabits)
                DailyNudgeCard(nudge = uiState.dailyNudge)
            }
        }
        HabitListOrEmpty(
            habits = uiState.habits,
            onComplete = onComplete,
            onDelete = onDelete,
            onEdit = onEdit
        )
    }
}

// ── Two pane (tablet / foldable) ──────────────────────────────────────────────
@Composable
private fun TwoPaneContent(
    uiState: HabitUiState,
    onComplete: (String, Boolean) -> Unit,
    onDelete: (String) -> Unit,
    onEdit: (com.example.smarthabitcoach.domain.model.Habit) -> Unit
) {
    Row(modifier = Modifier.fillMaxSize()) {
        // Left pane: habit list
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
        ) {
            HabitListOrEmpty(
                habits = uiState.habits,
                onComplete = onComplete,
                onDelete = onDelete,
                onEdit = onEdit
            )
        }

        VerticalDivider(
            modifier = Modifier.fillMaxHeight(),
            color = MaterialTheme.colorScheme.outlineVariant
        )

        // Right pane: statistics panel
        Column(
            modifier = Modifier
                .width(380.dp)
                .fillMaxHeight()
                .padding(vertical = 16.dp),
            verticalArrangement = Arrangement.Top
        ) {
            Text(
                text = "Statistics",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
            )
            StatisticsCard(
                statistics = uiState.statistics,
                modifier = Modifier.fillMaxWidth()
            )
            AtRiskHabitsCard(atRiskHabits = uiState.atRiskHabits)
            DailyNudgeCard(nudge = uiState.dailyNudge)
        }
    }
}

// ── Shared: list or empty state ───────────────────────────────────────────────
@Composable
private fun HabitListOrEmpty(
    habits: List<Habit>,
    onComplete: (String, Boolean) -> Unit,
    onDelete: (String) -> Unit,
    onEdit: (Habit) -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        // Empty state
        AnimatedVisibility(
            visible  = habits.isEmpty(),
            enter    = fadeIn(tween(500)),
            exit     = fadeOut(tween(200)),
            modifier = Modifier
                .align(Alignment.Center)
                .semantics { liveRegion = LiveRegionMode.Polite }
        ) {
            EmptyHabitsPlaceholder(modifier = Modifier.fillMaxSize())
        }

        // Habit list
        AnimatedVisibility(
            visible = habits.isNotEmpty(),
            enter   = fadeIn(tween(350)),
            exit    = fadeOut(tween(200))
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 96.dp, top = 4.dp)
            ) {
                itemsIndexed(items = habits, key = { _, item -> item.id }) { _, habit ->
                    HabitListItem(
                        habit = habit,
                        onComplete = onComplete,
                        onDelete = onDelete,
                        onEdit = onEdit
                    )
                }
            }
        }
    }
}

// ── Empty state ───────────────────────────────────────────────────────────────
@Composable
fun EmptyHabitsPlaceholder(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Stacked icon cluster
        Box(
            modifier = Modifier
                .size(96.dp)
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.CheckCircleOutline,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
            )
            Icon(
                imageVector = Icons.Rounded.AutoAwesome,
                contentDescription = null,
                modifier = Modifier
                    .size(40.dp)
                    .align(Alignment.Center),
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Build your first habit",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = "Small daily actions compound into\nlife-changing results.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            lineHeight = MaterialTheme.typography.bodyMedium.lineHeight
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Tap + below to get started",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center
        )
    }
}
