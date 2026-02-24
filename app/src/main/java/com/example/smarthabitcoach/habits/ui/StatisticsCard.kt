package com.example.smarthabitcoach.habits.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.smarthabitcoach.domain.model.HabitStatistics
import com.example.smarthabitcoach.ui.theme.SmartHabitCoachTheme
import kotlin.math.roundToInt

/**
 * Statistics card showing:
 * - Current streak (consecutive days)
 * - Weekly completion rate (%)
 * - Total habits and completed habits
 */
@Composable
fun StatisticsCard(
    statistics: HabitStatistics,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHighest
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Your Progress",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatisticItem(
                    label = "Current Streak",
                    value = statistics.currentStreak.toString(),
                    unit = "days",
                    modifier = Modifier.weight(1f)
                )

                Spacer(modifier = Modifier.width(12.dp))

                StatisticItem(
                    label = "Weekly Rate",
                    value = (statistics.weeklyCompletionRate * 100).roundToInt().toString(),
                    unit = "%",
                    modifier = Modifier.weight(1f)
                )

                Spacer(modifier = Modifier.width(12.dp))

                StatisticItem(
                    label = "Completed Today",
                    value = "${statistics.completedToday}",
                    unit = "of ${statistics.totalHabits}",
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

/**
 * Single statistic item (reusable component).
 */
@Composable
fun StatisticItem(
    label: String,
    value: String,
    unit: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(4.dp))

        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.width(4.dp))

            Text(
                text = unit,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun StatisticsCardPreview() {
    SmartHabitCoachTheme {
        StatisticsCard(
            statistics = HabitStatistics(
                currentStreak = 7,
                longestStreak = 21,
                weeklyCompletionRate = 0.85f,
                totalHabits = 5,
                completedToday = 4,
                totalCompleted = 34
            )
        )
    }
}

@Preview(showBackground = true)
@Composable
fun StatisticsCardEmptyPreview() {
    SmartHabitCoachTheme {
        StatisticsCard(
            statistics = HabitStatistics()
        )
    }
}


