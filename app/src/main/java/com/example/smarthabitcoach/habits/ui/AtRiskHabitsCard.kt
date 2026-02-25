package com.example.smarthabitcoach.habits.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.smarthabitcoach.domain.model.HabitRiskAssessment
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale
import kotlin.math.roundToInt

/**
 * Card showing habits at risk of breaking their streak.
 * Powered by DetectAtRiskHabitsUseCase — sliding-window day-of-week miss-rate algorithm.
 * Deterministic, zero LLM cost.
 */
@Composable
fun AtRiskHabitsCard(
    atRiskHabits: List<HabitRiskAssessment>,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = atRiskHabits.isNotEmpty(),
        enter = fadeIn(tween(500)) + expandVertically(tween(400)),
        modifier = modifier
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp)
                .semantics { contentDescription = "At-risk habits: ${atRiskHabits.size} habits need attention" },
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.6f)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Header
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Rounded.Warning,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Streak at Risk",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // List each at-risk habit
                atRiskHabits.forEach { assessment ->
                    AtRiskHabitRow(assessment = assessment)
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
private fun AtRiskHabitRow(assessment: HabitRiskAssessment) {
    // e.g. "missed 3 of last 4 Tuesdays"
    val windowSize = (1..4).map { LocalDate.now().minusWeeks(it.toLong()) }.size
    val missedCount = (assessment.missRate * windowSize).roundToInt()
    val dayName = LocalDate.now().dayOfWeek.getDisplayName(TextStyle.FULL, Locale.ENGLISH)
    val nudgeText = "missed $missedCount of last $windowSize ${dayName}s"

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .semantics { contentDescription = "${assessment.habit.title}: $nudgeText" }
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = assessment.habit.title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onErrorContainer,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = nudgeText,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.7f)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { assessment.missRate },
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.error,
            trackColor = MaterialTheme.colorScheme.errorContainer
        )
    }
}

