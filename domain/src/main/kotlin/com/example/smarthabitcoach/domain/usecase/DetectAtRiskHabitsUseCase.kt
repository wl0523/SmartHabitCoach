package com.example.smarthabitcoach.domain.usecase

import com.example.smarthabitcoach.domain.model.Habit
import com.example.smarthabitcoach.domain.model.HabitRiskAssessment
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

/**
 * DetectAtRiskHabitsUseCase — Predictive streak-rescue engine.
 *
 * Uses a sliding-window day-of-week miss-rate algorithm:
 * For each habit, look back at the same weekday for the last 4 weeks (excluding today).
 * missRate = (# of same-weekday occurrences NOT in completedDates) / windowSize
 * If missRate >= 0.5 (missed 2+ out of 4 same-weekdays), the habit is flagged "at risk".
 *
 * Fully deterministic — zero LLM cost. The key design insight:
 * use LLM only where it adds value (natural language), not for logic that code does better.
 *
 * Example: it's Thursday. Habit "Evening Read" was missed last 3 Thursdays → missRate = 0.75 → at risk.
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
        // Build the 4-week window: same weekday as today, going back 1–4 weeks
        val windowDates = (1..4).map { weeksAgo -> today.minusWeeks(weeksAgo.toLong()) }

        val missedCount = windowDates.count { date ->
            date.formatter() !in habit.completedDates
        }

        val missRate = missedCount / windowDates.size.toFloat()

        return HabitRiskAssessment(
            habit    = habit,
            missRate = missRate,
            isAtRisk = missRate >= 0.5f
        )
    }

    private fun LocalDate.formatter(): String = this.format(formatter)
}
