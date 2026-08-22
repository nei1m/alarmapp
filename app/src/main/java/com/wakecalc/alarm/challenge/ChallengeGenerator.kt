package com.wakecalc.alarm.challenge

import kotlin.random.Random

/**
 * Generates Calc 1–2 problems (limits, derivatives, integrals, series) with
 * known correct answers. Difficulty rises with [level] (0 = easy .. 3 = hard),
 * so the fail-safe / repeated-mistake path can escalate.
 */
object ChallengeGenerator {

    private fun sup(n: Int): String = when (n) {
        2 -> "²"; 3 -> "³"; 4 -> "⁴"; 5 -> "⁵"; else -> "^$n"
    }

    private val enabledDefault = Problem.Category.values().toSet()

    /** Pick a random problem from one of the [categories], at difficulty [level]. */
    fun generate(
        categories: Set<Problem.Category> = enabledDefault,
        level: Int = 1,
        rng: Random = Random.Default
    ): Problem {
        val cats = if (categories.isEmpty()) enabledDefault else categories
        return when (cats.random(rng)) {
            Problem.Category.DERIVATIVES -> derivative(level, rng)
            Problem.Category.INTEGRALS -> integral(level, rng)
            Problem.Category.LIMITS -> limit(level, rng)
            Problem.Category.SERIES -> series(level, rng)
        }
    }

    // ---- Derivatives -------------------------------------------------------

    private fun derivative(level: Int, rng: Random): Problem {
        return when (rng.nextInt(if (level >= 2) 4 else 2)) {
            0 -> { // polynomial
                val a = rng.nextInt(2, 6)
                val n = rng.nextInt(2, 5)
                val b = rng.nextInt(1, 8)
                Problem(
                    prompt = "d/dx [ ${a}x${sup(n)} + ${b}x ]",
                    correctAnswer = "${a * n}x^${n - 1} + $b",
                    indefinite = false,
                    category = Problem.Category.DERIVATIVES,
                    hint = "power rule: bring the exponent down"
                )
            }
            1 -> { // trig
                val a = rng.nextInt(2, 6)
                Problem(
                    prompt = "d/dx [ ${a}·sin(x) ]",
                    correctAnswer = "${a}cos(x)",
                    indefinite = false,
                    category = Problem.Category.DERIVATIVES,
                    hint = "derivative of sin is cos"
                )
            }
            2 -> { // product rule
                val n = rng.nextInt(2, 4)
                Problem(
                    prompt = "d/dx [ x${sup(n)}·sin(x) ]",
                    correctAnswer = "${n}x^${n - 1}*sin(x) + x^$n*cos(x)",
                    indefinite = false,
                    category = Problem.Category.DERIVATIVES,
                    hint = "product rule: u'v + uv'"
                )
            }
            else -> { // chain rule
                val a = rng.nextInt(2, 5)
                Problem(
                    prompt = "d/dx [ sin(${a}x) ]",
                    correctAnswer = "${a}cos(${a}x)",
                    indefinite = false,
                    category = Problem.Category.DERIVATIVES,
                    hint = "chain rule: multiply by the inside derivative"
                )
            }
        }
    }

    // ---- Integrals ---------------------------------------------------------

    private fun integral(level: Int, rng: Random): Problem {
        return when (rng.nextInt(if (level >= 2) 3 else 2)) {
            0 -> { // indefinite polynomial
                val a = rng.nextInt(2, 7)
                val n = rng.nextInt(1, 4)
                val b = rng.nextInt(1, 6)
                val coeff = a * (n + 1)
                // ∫ (a(n+1)) x^n dx form so the coefficient is clean:
                Problem(
                    prompt = "∫ ( ${coeff}x${if (n == 1) "" else sup(n)} + $b ) dx",
                    correctAnswer = "${a}x^${n + 1} + ${b}x",
                    indefinite = true,
                    category = Problem.Category.INTEGRALS,
                    hint = "add 1 to the exponent, divide by it (+ C)"
                )
            }
            1 -> { // definite polynomial 0..1
                val a = rng.nextInt(2, 6) // ∫0^1 a*x dx = a/2 ... keep integer: use 2k
                val k = rng.nextInt(1, 5)
                // ∫0^1 (2k) x dx = k
                Problem(
                    prompt = "∫₀¹ ( ${2 * k}x ) dx",
                    correctAnswer = "$k",
                    indefinite = false,
                    category = Problem.Category.INTEGRALS,
                    hint = "FTC: evaluate the antiderivative at 1 minus at 0"
                )
            }
            else -> { // 1/x integral
                Problem(
                    prompt = "∫ ( 1 / x ) dx",
                    correctAnswer = "ln(abs(x))",
                    indefinite = true,
                    category = Problem.Category.INTEGRALS,
                    hint = "this one is a natural log (+ C)"
                )
            }
        }
    }

    // ---- Limits ------------------------------------------------------------

    private fun limit(level: Int, rng: Random): Problem {
        return when (rng.nextInt(3)) {
            0 -> {
                val a = rng.nextInt(2, 7)
                Problem(
                    prompt = "lim (x→0) sin(${a}x) / x",
                    correctAnswer = "$a",
                    indefinite = false,
                    category = Problem.Category.LIMITS,
                    hint = "sin(kx)/x → k as x→0"
                )
            }
            1 -> {
                val a = rng.nextInt(2, 6)
                val b = rng.nextInt(1, 6)
                Problem(
                    prompt = "lim (x→∞) (${a}x² + ${b}x) / (x²)",
                    correctAnswer = "$a",
                    indefinite = false,
                    category = Problem.Category.LIMITS,
                    hint = "compare leading terms"
                )
            }
            else -> {
                val a = rng.nextInt(2, 6)
                Problem(
                    prompt = "lim (x→3) (x² - 9) / (x - 3)",
                    correctAnswer = "6",
                    indefinite = false,
                    category = Problem.Category.LIMITS,
                    hint = "factor the numerator, then cancel"
                )
            }
        }
    }

    // ---- Series (Calc 2) ---------------------------------------------------

    private fun series(level: Int, rng: Random): Problem {
        return when (rng.nextInt(2)) {
            0 -> { // geometric sum
                // sum_{n=0}^inf r^n = 1/(1-r), pick r=1/2,1/3,1/4
                val d = rng.nextInt(2, 5)
                Problem(
                    prompt = "Σ (n=0→∞) (1/$d)ⁿ  =  ?",
                    correctAnswer = "${d}/${d - 1}",
                    indefinite = false,
                    category = Problem.Category.SERIES,
                    hint = "geometric series sums to 1/(1 - r)"
                )
            }
            else -> {
                Problem(
                    prompt = "Σ (n=1→∞) (1/2)ⁿ  =  ?",
                    correctAnswer = "1",
                    indefinite = false,
                    category = Problem.Category.SERIES,
                    hint = "geometric, first term 1/2, ratio 1/2"
                )
            }
        }
    }
}
