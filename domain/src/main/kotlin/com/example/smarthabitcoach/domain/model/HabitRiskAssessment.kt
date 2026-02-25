package com.example.smarthabitcoach.domain.model

/**
 * Risk assessment result for a single habit.
 * Produced by DetectAtRiskHabitsUseCase — fully deterministic, no LLM cost.
 */
data class HabitRiskAssessment(
    val habit: Habit,
    val missRate: Float,              // 0.0–1.0  (>= 0.5 = at risk)
    val isAtRisk: Boolean
)
