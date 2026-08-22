package com.wakecalc.alarm.challenge

/**
 * A single wake-up challenge.
 *
 * [prompt] is what the user sees (may contain unicode like ∫, ², etc).
 * [correctAnswer] is an expression in terms of x (or a constant) that the
 * typed answer is checked against for mathematical equivalence.
 * [indefinite] true for indefinite integrals, where "+ C" is accepted.
 */
data class Problem(
    val prompt: String,
    val correctAnswer: String,
    val indefinite: Boolean,
    val category: Category,
    val hint: String
) {
    enum class Category(val label: String) {
        LIMITS("Limit"),
        DERIVATIVES("Derivative"),
        INTEGRALS("Integral"),
        SERIES("Series")
    }
}
