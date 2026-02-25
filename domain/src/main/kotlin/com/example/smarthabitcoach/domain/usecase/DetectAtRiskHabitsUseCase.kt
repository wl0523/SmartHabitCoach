package com.example.smarthabitcoach.domain.usecase

import com.example.smarthabitcoach.domain.model.Habit
import com.example.smarthabitcoach.domain.model.HabitRiskAssessment
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.inject.Inject

/**
 * DetectAtRiskHabitsUseCase — Predictive streak-rescue engine.
 *
 * Uses a sliding-window day-of-week miss-rate algorithm:
 * For each habit, look back at the same weekday for the last 4 weeks (excluding today).
 * missRate = (# of same-weekday occurrences NOT in completedDates) / validWindowSize
 * If missRate >= 0.5 (missed 2+ out of valid window), the habit is flagged "at risk".
 *
 * Dates before the habit's createdAt are excluded from the window.
 * If fewer than 2 valid data points exist, no judgment is made (too new to assess).
 *
 * Fully deterministic — zero LLM cost.
 *
 * Example: it's Thursday. Habit "Evening Read" was missed last 3 Thursdays → missRate = 0.75 → at risk.
 * Example: habit created today → 0 valid past Thursdays → not assessed → not at risk.
 */
class DetectAtRiskHabitsUseCase @Inject constructor() {

    private val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    /** @param today injectable for deterministic unit testing; defaults to LocalDate.now() */
    operator fun invoke(
        habits: List<Habit>,
        today: LocalDate = LocalDate.now()
    ): List<HabitRiskAssessment> {
        return habits
            .map { habit -> assess(habit, today) }
            .filter { it.isAtRisk }
    }

    private fun assess(habit: Habit, today: LocalDate): HabitRiskAssessment {
        val createdDate = Instant.ofEpochMilli(habit.createdAt)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()

        // Build the 4-week window: same weekday as today, going back 1–4 weeks
        // Exclude any dates that predate the habit's creation
        val windowDates = (1..4)
            .map { weeksAgo -> today.minusWeeks(weeksAgo.toLong()) }
            .filter { it >= createdDate }

        // Not enough history to make a meaningful assessment — treat as not at risk
        if (windowDates.size < 2) {
            return HabitRiskAssessment(habit = habit, missRate = 0f, isAtRisk = false)
        }

        val missedCount = windowDates.count { date ->
            date.format(formatter) !in habit.completedDates
        }

        val missRate = missedCount / windowDates.size.toFloat()

        return HabitRiskAssessment(
            habit    = habit,
            missRate = missRate,
            isAtRisk = missRate >= 0.5f
        )
    }
}
