package com.wakecalc.alarm.challenge

import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.ln
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.math.tan

/**
 * A tiny, dependency-free math expression evaluator.
 *
 * Supports: + - * / ^, parentheses, unary minus, implicit multiplication
 * (e.g. "6x", "2(x+1)", "3x^2"), the variable x, the constants pi and e,
 * and the functions sin, cos, tan, ln, log (natural), sqrt, exp, abs.
 *
 * Used to check a typed answer for *mathematical equivalence* to the
 * correct answer, rather than an exact string match. So "6x^3/3 + 2x^2",
 * "2x^3 + 2x^2" and "2*x**3+2x^2" all count as the same answer.
 */
object MathExpr {

    class ParseException(message: String) : Exception(message)

    /** Evaluate [expr] at the given [x]. Throws ParseException on bad input. */
    fun eval(expr: String, x: Double): Double {
        val normalized = normalize(expr)
        val parser = Parser(normalized, x)
        val result = parser.parseExpression()
        parser.expectEnd()
        return result
    }

    /**
     * True if [user] is mathematically equivalent to [correct].
     * When [indefinite] is true (indefinite integrals), answers that differ
     * only by an additive constant (the "+ C") are accepted.
     */
    fun equivalent(user: String, correct: String, indefinite: Boolean): Boolean {
        if (user.isBlank()) return false
        val samples = doubleArrayOf(0.31, 0.72, 1.34, 2.13, -0.57, 0.93, 1.77)
        val diffs = ArrayList<Double>()
        for (xv in samples) {
            val u: Double
            val c: Double
            try {
                u = eval(user, xv)
                c = eval(correct, xv)
            } catch (e: Exception) {
                if (e is ParseException) return false
                continue // domain error (e.g. divide by zero) at this sample; skip it
            }
            if (u.isNaN() || c.isNaN() || u.isInfinite() || c.isInfinite()) continue
            diffs.add(u - c)
        }
        if (diffs.size < 3) return false
        val tol = 1e-6
        return if (indefinite) {
            val base = diffs[0]
            diffs.all { abs(it - base) < tol }
        } else {
            diffs.all { abs(it) < tol }
        }
    }

    /** Insert explicit '*' for implicit multiplication and standardise symbols. */
    private fun normalize(raw: String): String {
        var s = raw.lowercase().trim()
        s = s.replace("²", "^2").replace("³", "^3").replace("¹", "^1")
        s = s.replace("−", "-").replace("·", "*").replace("×", "*").replace("÷", "/")
        s = s.replace("**", "^")
        s = s.replace(" ", "")
        // pi as a token -> keep, but ensure "pi" isn't split. We'll handle in tokenizer.
        val out = StringBuilder()
        val fns = listOf("sin", "cos", "tan", "sqrt", "exp", "ln", "log", "abs", "pi")
        var i = 0
        while (i < s.length) {
            val c = s[i]
            // detect function / identifier starting here
            var matchedFn: String? = null
            for (f in fns) {
                if (s.startsWith(f, i)) { matchedFn = f; break }
            }
            if (matchedFn != null) {
                // implicit multiply if previous char closes a value
                if (out.isNotEmpty() && needsMulBefore(out.last())) out.append('*')
                out.append(matchedFn)
                i += matchedFn.length
                continue
            }
            if (c == 'x' || c == 'e') {
                if (out.isNotEmpty() && needsMulBefore(out.last())) out.append('*')
                out.append(c)
                i++
                // implicit multiply handled when next token starts
                continue
            }
            if (c.isDigit() || c == '.') {
                if (out.isNotEmpty() && out.last() == ')') out.append('*')
                out.append(c)
                i++
                continue
            }
            if (c == '(') {
                if (out.isNotEmpty() && needsMulBefore(out.last())) out.append('*')
                out.append(c)
                i++
                continue
            }
            out.append(c)
            i++
        }
        return out.toString()
    }

    private fun needsMulBefore(prev: Char): Boolean {
        // A value-ending char followed by another value means implicit multiplication.
        return prev.isDigit() || prev == ')' || prev == 'x' || prev == 'e' || prev == '.'
    }

    /** Recursive-descent parser / evaluator. */
    private class Parser(val s: String, val x: Double) {
        var pos = 0

        fun expectEnd() {
            if (pos != s.length) throw ParseException("Unexpected '${s.getOrNull(pos)}' at $pos")
        }

        // expression = term (('+'|'-') term)*
        fun parseExpression(): Double {
            var value = parseTerm()
            while (pos < s.length) {
                when (s[pos]) {
                    '+' -> { pos++; value += parseTerm() }
                    '-' -> { pos++; value -= parseTerm() }
                    else -> return value
                }
            }
            return value
        }

        // term = unary (('*'|'/') unary)*
        private fun parseTerm(): Double {
            var value = parseUnary()
            while (pos < s.length) {
                when (s[pos]) {
                    '*' -> { pos++; value *= parseUnary() }
                    '/' -> { pos++; value /= parseUnary() }
                    else -> return value
                }
            }
            return value
        }

        // unary binds looser than power, so -x^2 == -(x^2)
        private fun parseUnary(): Double {
            if (pos < s.length && s[pos] == '-') { pos++; return -parseUnary() }
            if (pos < s.length && s[pos] == '+') { pos++; return parseUnary() }
            return parsePower()
        }

        // power = primary ('^' unary)?   (right associative; exponent may be signed)
        private fun parsePower(): Double {
            val base = parsePrimary()
            if (pos < s.length && s[pos] == '^') {
                pos++
                val exp = parseUnary()
                return base.pow(exp)
            }
            return base
        }

        private fun parsePrimary(): Double {
            if (pos >= s.length) throw ParseException("Unexpected end")
            val c = s[pos]
            if (c == '(') {
                pos++
                val v = parseExpression()
                if (pos >= s.length || s[pos] != ')') throw ParseException("Missing ')'")
                pos++
                return v
            }
            if (c.isDigit() || c == '.') return parseNumber()
            // identifiers: functions, variable x, constants
            val id = parseIdent()
            return when (id) {
                "x" -> x
                "pi" -> Math.PI
                "e" -> Math.E
                "sin" -> sin(parseArg())
                "cos" -> cos(parseArg())
                "tan" -> tan(parseArg())
                "sqrt" -> sqrt(parseArg())
                "exp" -> exp(parseArg())
                "ln", "log" -> ln(parseArg())
                "abs" -> abs(parseArg())
                else -> throw ParseException("Unknown symbol '$id'")
            }
        }

        private fun parseArg(): Double {
            if (pos >= s.length || s[pos] != '(') throw ParseException("Expected '(' after function")
            pos++
            val v = parseExpression()
            if (pos >= s.length || s[pos] != ')') throw ParseException("Missing ')'")
            pos++
            return v
        }

        private fun parseNumber(): Double {
            val start = pos
            while (pos < s.length && (s[pos].isDigit() || s[pos] == '.')) pos++
            return s.substring(start, pos).toDoubleOrNull()
                ?: throw ParseException("Bad number")
        }

        private fun parseIdent(): String {
            val start = pos
            while (pos < s.length && s[pos].isLetter()) pos++
            if (pos == start) throw ParseException("Unexpected '${s[pos]}'")
            return s.substring(start, pos)
        }
    }
}
