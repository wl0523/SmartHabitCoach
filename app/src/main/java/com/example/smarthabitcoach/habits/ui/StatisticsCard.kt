package com.example.smarthabitcoach.habits.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.TrendingUp
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smarthabitcoach.domain.model.HabitStatistics
import com.example.smarthabitcoach.ui.theme.ChartBarActive
import com.example.smarthabitcoach.ui.theme.ChartBarDark
import com.example.smarthabitcoach.ui.theme.ChartBarDarkInactive
import com.example.smarthabitcoach.ui.theme.ChartBarInactive
import com.example.smarthabitcoach.ui.theme.SmartHabitCoachTheme
import com.example.smarthabitcoach.ui.theme.StreakAmber80
import com.example.smarthabitcoach.ui.theme.StreakFireBottom
import com.example.smarthabitcoach.ui.theme.StreakFireTop
import kotlin.math.roundToInt

/**
 * Production-level Statistics Dashboard Card.
 *
 * Displays:
 * - Current streak with fire icon (animated number)
 * - Weekly completion rate (animated % + mini bar chart)
 * - Today's completions vs total (animated)
 *
 * Accessibility: all metric items have contentDescription for TalkBack.
 * Animation: numbers count up on first display; bars animate in from bottom.
 */
@Composable
fun StatisticsCard(
    statistics: HabitStatistics,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 18.dp)
        ) {
            // ── Header ─────────────────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Today's Progress",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                // Completion badge
                val allDone = statistics.totalHabits > 0 &&
                        statistics.completedToday == statistics.totalHabits
                if (allDone) {
                    AllDoneBadge()
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ── Three metric tiles ─────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Streak
                MetricTile(
                    icon = Icons.Rounded.LocalFireDepartment,
                    iconTint = if (statistics.currentStreak > 0) StreakAmber80 else MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.4f),
                    iconBackground = if (statistics.currentStreak > 0)
                        Brush.verticalGradient(listOf(StreakFireTop, StreakFireBottom))
                    else null,
                    label = "Streak",
                    value = statistics.currentStreak,
                    unit = if (statistics.currentStreak == 1) "day" else "days",
                    accessibilityText = "Current streak: ${statistics.currentStreak} ${if (statistics.currentStreak == 1) "day" else "days"}",
                    modifier = Modifier.weight(1f)
                )

                MetricDivider()

                // Weekly rate — with actual daily data
                WeeklyRateColumn(
                    rate = statistics.weeklyCompletionRate,
                    dailyRates = statistics.weeklyDailyRates,
                    modifier = Modifier.weight(1.3f)
                )

                MetricDivider()

                // Today
                MetricTile(
                    icon = Icons.Rounded.CheckCircle,
                    iconTint = MaterialTheme.colorScheme.primary,
                    iconBackground = null,
                    label = "Today",
                    value = statistics.completedToday,
                    unit = "/ ${statistics.totalHabits}",
                    accessibilityText = "Completed today: ${statistics.completedToday} of ${statistics.totalHabits}",
                    modifier = Modifier.weight(1f)
                )
            }

            // ── Divider + longest streak footer ───────────────────────────
            if (statistics.longestStreak > 0) {
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 14.dp),
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.12f)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.TrendingUp,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "Best streak",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        )
                    }
                    Text(
                        text = "${statistics.longestStreak} days",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.semantics {
                            contentDescription = "Best streak: ${statistics.longestStreak} days"
                        }
                    )
                }
            }
        }
    }
}

// ── All Done Badge ────────────────────────────────────────────────────────────
@Composable
private fun AllDoneBadge() {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text = "✓ All done!",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

// ── Metric Tile ───────────────────────────────────────────────────────────────
@Composable
private fun MetricTile(
    icon: ImageVector,
    iconTint: Color,
    iconBackground: Brush?,
    label: String,
    value: Int,
    unit: String,
    accessibilityText: String,
    modifier: Modifier = Modifier
) {
    val animatedValue by animateIntAsState(
        targetValue = value,
        animationSpec = tween(durationMillis = 700, easing = FastOutSlowInEasing),
        label = "metric_$label"
    )

    Column(
        modifier = modifier.semantics { contentDescription = accessibilityText },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .then(
                    if (iconBackground != null)
                        Modifier.background(iconBackground)
                    else
                        Modifier.background(
                            MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.08f)
                        )
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (iconBackground != null) Color.White else iconTint,
                modifier = Modifier.size(20.dp)
            )
        }

        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
        )

        Row(
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = animatedValue.toString(),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Text(
                text = " $unit",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f),
                modifier = Modifier.padding(bottom = 3.dp)
            )
        }
    }
}

// ── Metric Divider ────────────────────────────────────────────────────────────
@Composable
private fun MetricDivider() {
    Box(
        modifier = Modifier
            .width(1.dp)
            .height(72.dp)
            .background(MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.10f))
    )
}

// ── Weekly Rate Column with actual daily bar chart ────────────────────────────
@Composable
private fun WeeklyRateColumn(
    rate: Float,
    dailyRates: List<Float>,   // Actual Mon–Sun completion rates (7 entries, calculated in domain layer)
    modifier: Modifier = Modifier
) {
    val animatedRate by animateFloatAsState(
        targetValue = rate.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 900, easing = FastOutSlowInEasing),
        label = "weekly_rate"
    )
    val pct = (animatedRate * 100).roundToInt()

    Column(
        modifier = modifier
            .padding(horizontal = 8.dp)
            .semantics { contentDescription = "Weekly completion rate: $pct percent" },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Rounded.TrendingUp,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
                .padding(8.dp)
        )

        Text(
            text = "Weekly",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
        )

        Text(
            text = "$pct%",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )

        MiniBarChart(
            barHeights = dailyRates.ifEmpty { List(7) { 0f } },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp)
        )
    }
}

// ── Mini 7-day Staggered Bar Chart ────────────────────────────────────────────
@Composable
private fun MiniBarChart(
    barHeights: List<Float>,
    modifier: Modifier = Modifier,
    maxBarHeight: Dp = 28.dp
) {
    val isDark = MaterialTheme.colorScheme.background.red < 0.5f
    val activeColor   = if (isDark) ChartBarDark else ChartBarActive
    val inactiveColor = if (isDark) ChartBarDarkInactive else ChartBarInactive

    val animatedHeights = barHeights.mapIndexed { index, target ->
        val animatable = remember { Animatable(0f) }
        LaunchedEffect(target) {
            animatable.animateTo(
                targetValue = target,
                animationSpec = tween(
                    durationMillis = 500,
                    delayMillis = index * 60,
                    easing = FastOutSlowInEasing
                )
            )
        }
        animatable.value
    }

    val dayLabels = listOf("M", "T", "W", "T", "F", "S", "S")

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.Bottom
    ) {
        animatedHeights.forEachIndexed { index, height ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Bottom
            ) {
                Box(
                    modifier = Modifier
                        .width(6.dp)
                        .height((maxBarHeight.value * height.coerceIn(0.06f, 1f)).dp)
                        .clip(RoundedCornerShape(topStart = 3.dp, topEnd = 3.dp))
                        .background(if (height > 0.3f) activeColor else inactiveColor)
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = dayLabels[index],
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 8.sp,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.5f)
                )
            }
        }
    }
}


// ── Previews ──────────────────────────────────────────────────────────────────
@Preview(showBackground = true)
@Composable
private fun StatisticsCardPreview() {
    SmartHabitCoachTheme {
        StatisticsCard(
            statistics = HabitStatistics(
                currentStreak = 7,
                longestStreak = 14,
                weeklyCompletionRate = 0.85f,
                // Mon–Sun: sample completion rates (1.0 = all habits done, 0.5 = half, 0.0 = none)
                weeklyDailyRates = listOf(1.0f, 0.75f, 1.0f, 0.5f, 1.0f, 0.8f, 0.0f),
                totalHabits = 5,
                completedToday = 4,
                totalCompleted = 47
            )
        )
    }
}

@Preview(showBackground = true, name = "All Done")
@Composable
private fun StatisticsCardAllDonePreview() {
    SmartHabitCoachTheme {
        StatisticsCard(
            statistics = HabitStatistics(
                currentStreak = 3,
                longestStreak = 3,
                weeklyCompletionRate = 1.0f,
                weeklyDailyRates = listOf(1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f),
                totalHabits = 4,
                completedToday = 4,
                totalCompleted = 12
            )
        )
    }
}

