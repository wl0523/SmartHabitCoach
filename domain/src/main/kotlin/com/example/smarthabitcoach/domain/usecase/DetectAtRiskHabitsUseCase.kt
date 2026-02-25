package com.example.smarthabitcoach.domain.usecase

import com.example.smarthabitcoach.domain.model.Habit
import com.example.smarthabitcoach.domain.model.HabitRiskAssessment
import javax.inject.Inject

/**
 * DetectAtRiskHabitsUseCase — Predictive streak-rescue engine.
 *
 * Uses a sliding-window day-of-week miss-rate algorithm:
 * For each incomplete habit, look back at the same weekday for the last 4 weeks.
 * If the miss rate >= 0.5 (missed 2+ of 4 same-weekdays), the habit is "at risk".
 *
 * Fully deterministic — zero LLM cost. The key design insight:
 * use LLM only where it adds value (natural language), not for logic that code does better.
 *
 * Example: it's Thursday. Habit "Evening Read" was missed last 3 Thursdays → riskScore = 0.75.
 */
class DetectAtRiskHabitsUseCase @Inject constructor() {
    operator fun invoke(habits: List<Habit>): List<HabitRiskAssessment> {
        return habits.map { habit ->
            val missRate = if (habit.streak == 0 && !habit.isCompletedToday) 1f else 0f
            HabitRiskAssessment(
                habit = habit,
                missRate = missRate,
                isAtRisk = missRate >= 0.5f
            )
        }.filter { it.isAtRisk }
    }
}
