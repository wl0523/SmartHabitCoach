package com.example.smarthabitcoach.habits.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.smarthabitcoach.domain.model.Habit
import com.example.smarthabitcoach.ui.theme.CompletionGreen
import com.example.smarthabitcoach.ui.theme.HabitGreen40
import com.example.smarthabitcoach.ui.theme.HabitGreen90
import com.example.smarthabitcoach.ui.theme.SmartHabitCoachTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitListItem(
    habit: Habit,
    onComplete: (habitId: String, completed: Boolean) -> Unit,
    onDelete: (habitId: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val dismissState = rememberSwipeToDismissBoxState()
    val haptic = LocalHapticFeedback.current

    // Trigger delete with haptic feedback when fully swiped
    LaunchedEffect(dismissState.currentValue) {
        if (dismissState.currentValue == SwipeToDismissBoxValue.EndToStart) {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            onDelete(habit.id)
        }
    }

    SwipeToDismissBox(
        state = dismissState,
        enableDismissFromStartToEnd = false,
        enableDismissFromEndToStart = true,
        backgroundContent = { SwipeDismissBackground(fraction = dismissState.progress) },
        modifier = modifier.padding(horizontal = 16.dp, vertical = 5.dp)
    ) {
        HabitCard(habit = habit, onComplete = onComplete)
    }
}

// ── Swipe Background — progressive red reveal ─────────────────────────────────
@Composable
private fun SwipeDismissBackground(fraction: Float) {
    val bgAlpha = (fraction * 2f).coerceIn(0f, 1f)
    val iconScale = (0.6f + fraction * 0.8f).coerceIn(0.6f, 1f)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(20.dp))
            .background(
                MaterialTheme.colorScheme.errorContainer.copy(alpha = bgAlpha)
            ),
        contentAlignment = Alignment.CenterEnd
    ) {
        Icon(
            imageVector = Icons.Rounded.Delete,
            contentDescription = "Delete habit",
            tint = MaterialTheme.colorScheme.onErrorContainer,
            modifier = Modifier
                .padding(end = 24.dp)
                .size(24.dp)
                .scale(iconScale)
        )
    }
}

// ── Main Habit Card ───────────────────────────────────────────────────────────
@Composable
private fun HabitCard(
    habit: Habit,
    onComplete: (habitId: String, completed: Boolean) -> Unit
) {
    val haptic = LocalHapticFeedback.current

    // Track previous completion for "just completed" animation trigger
    var wasJustCompleted by remember { mutableStateOf(false) }
    val prevCompleted = remember { mutableStateOf(habit.isCompleted) }

    LaunchedEffect(habit.isCompleted) {
        wasJustCompleted = habit.isCompleted && !prevCompleted.value
        prevCompleted.value = habit.isCompleted
    }

    // Card background color
    val cardColor by animateColorAsState(
        targetValue = if (habit.isCompleted)
            MaterialTheme.colorScheme.primaryContainer
        else
            MaterialTheme.colorScheme.surfaceContainerHigh,
        animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing),
        label = "card_color"
    )

    // Spring pop on completion (scale up then settle)
    val scaleAnim = remember { Animatable(1f) }
    LaunchedEffect(wasJustCompleted) {
        if (wasJustCompleted) {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            scaleAnim.animateTo(
                targetValue = 1.03f,
                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessHigh)
            )
            scaleAnim.animateTo(
                targetValue = 1f,
                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium)
            )
        }
    }

    val semanticDesc = buildString {
        append(habit.title)
        if (!habit.description.isNullOrBlank()) append(". ${habit.description}")
        append(". ${if (habit.isCompleted) "Completed" else "Not completed"}. Tap to toggle.")
    }

    Card(
        onClick = { onComplete(habit.id, !habit.isCompleted) },
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer { scaleX = scaleAnim.value; scaleY = scaleAnim.value }
            .semantics(mergeDescendants = true) {
                contentDescription = semanticDesc
                stateDescription = if (habit.isCompleted) "Completed" else "Not completed"
                role = Role.Checkbox
            },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (habit.isCompleted) 0.dp else 1.dp,
            pressedElevation = 0.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Left: Completion ring indicator
            CompletionRing(isCompleted = habit.isCompleted)

            // Center: Title + description
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = habit.title,
                    style = MaterialTheme.typography.titleSmall,
                    textDecoration = if (habit.isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                    color = if (habit.isCompleted)
                        MaterialTheme.colorScheme.onSurfaceVariant
                    else
                        MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                habit.description?.takeIf { it.isNotBlank() }?.let { desc ->
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = desc,
                        style = MaterialTheme.typography.bodySmall,
                        color = if (habit.isCompleted)
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Right: Completion status chip
            CompletionStatusChip(isCompleted = habit.isCompleted)
        }
    }
}

// ── Completion Ring ────────────────────────────────────────────────────────────
@Composable
private fun CompletionRing(isCompleted: Boolean) {
    val bgColor by animateColorAsState(
        targetValue = if (isCompleted) CompletionGreen
        else MaterialTheme.colorScheme.surfaceContainerHighest,
        animationSpec = tween(300),
        label = "ring_bg"
    )
    val borderColor by animateColorAsState(
        targetValue = if (isCompleted) CompletionGreen
        else MaterialTheme.colorScheme.outline,
        animationSpec = tween(300),
        label = "ring_border"
    )

    // Outer ring (border effect via background + inner box)
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(borderColor.copy(alpha = if (isCompleted) 1f else 0.3f))
            .clearAndSetSemantics {},
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(if (isCompleted) 40.dp else 34.dp)
                .clip(CircleShape)
                .background(bgColor),
            contentAlignment = Alignment.Center
        ) {
            val iconAlpha by animateFloatAsState(
                targetValue = if (isCompleted) 1f else 0f,
                animationSpec = tween(200),
                label = "check_alpha"
            )
            val iconScale by animateFloatAsState(
                targetValue = if (isCompleted) 1f else 0.3f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessHigh
                ),
                label = "check_scale"
            )
            Icon(
                imageVector = Icons.Rounded.Check,
                contentDescription = null,
                tint = androidx.compose.ui.graphics.Color.White,
                modifier = Modifier
                    .size(20.dp)
                    .graphicsLayer { alpha = iconAlpha; scaleX = iconScale; scaleY = iconScale }
            )
        }
    }
}

// ── Completion Status Chip ─────────────────────────────────────────────────────
@Composable
private fun CompletionStatusChip(isCompleted: Boolean) {
    val chipAlpha by animateFloatAsState(
        targetValue = if (isCompleted) 1f else 0f,
        animationSpec = tween(300),
        label = "chip_alpha"
    )

    Box(
        modifier = Modifier
            .graphicsLayer { alpha = chipAlpha }
            .clip(RoundedCornerShape(8.dp))
            .background(
                Brush.horizontalGradient(
                    listOf(HabitGreen40.copy(alpha = 0.15f), HabitGreen90.copy(alpha = 0.3f))
                )
            )
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .clearAndSetSemantics {}
    ) {
        Text(
            text = "Done",
            style = MaterialTheme.typography.labelSmall,
            color = HabitGreen40
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun HabitListItemPreview() {
    SmartHabitCoachTheme {
        Column {
            HabitListItem(
                habit = Habit(id = "1", title = "Morning Run", description = "5 km outdoor", isCompleted = false),
                onComplete = { _, _ -> },
                onDelete = {}
            )
            HabitListItem(
                habit = Habit(id = "2", title = "Read 20 pages", description = "Non-fiction only", isCompleted = true),
                onComplete = { _, _ -> },
                onDelete = {}
            )
            HabitListItem(
                habit = Habit(id = "3", title = "Meditate", isCompleted = false),
                onComplete = { _, _ -> },
                onDelete = {}
            )
        }
    }
}
