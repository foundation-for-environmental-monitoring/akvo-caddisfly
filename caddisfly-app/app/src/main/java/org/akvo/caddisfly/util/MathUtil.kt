/*
 * Copyright (C) Stichting Akvo (Akvo Foundation)
 *
 * This file is part of Akvo Caddisfly.
 *
 * Akvo Caddisfly is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Akvo Caddisfly is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Akvo Caddisfly. If not, see <http://www.gnu.org/licenses/>.
 */
package org.akvo.caddisfly.util

import kotlin.math.*

object MathUtil {
    /**
     * Evaluates a math expression in a string.
     * http://stackoverflow.com/questions/3422673/evaluating-a-math-expression-given-in-string-form#answer-26227947
     *
     * @param str the string to evaluate
     * @return evaluated result
     */
    @JvmStatic
    fun eval(str: String): Double {
        return object : Any() {
            private var pos = -1
            private var ch = 0
            fun nextChar() {
                ch = (if (++pos < str.length) str[pos].toInt() else -1)
            }

            fun eat(charToEat: Int): Boolean {
                while (ch == ' '.toInt()) {
                    nextChar()
                }
                if (ch == charToEat) {
                    nextChar()
                    return true
                }
                return false
            }

            fun parse(): Double {
                nextChar()
                val x = parseExpression()
                require(pos >= str.length) { "Unexpected: " + ch.toChar() }
                return x
            }

            // Grammar:
            // expression = term | expression `+` term | expression `-` term
            // term = factor | term `*` factor | term `/` factor
            // factor = `+` factor | `-` factor | `(` expression `)`
            //        | number | functionName factor | factor `^` factor
            fun parseExpression(): Double {
                var x = parseTerm()
                while (true) {
                    if (eat('+'.toInt())) {
                        x += parseTerm() // addition
                    } else {
                        if (eat('-'.toInt())) {
                            x -= parseTerm() // subtraction
                        } else {
                            return x
                        }
                    }
                }
            }

            fun parseTerm(): Double {
                var x = parseFactor()
                while (true) {
                    if (eat('*'.toInt())) {
                        x *= parseFactor() // multiplication
                    } else {
                        if (eat('/'.toInt())) {
                            x /= parseFactor() // division
                        } else {
                            return x
                        }
                    }
                }
            }

            fun parseFactor(): Double {
                if (eat('+'.toInt())) {
                    return parseFactor() // unary plus
                }
                if (eat('-'.toInt())) {
                    return -parseFactor() // unary minus
                }
                var x: Double
                val startPos = pos
                if (eat('('.toInt())) { // parentheses
                    x = parseExpression()
                    eat(')'.toInt())
                } else if (ch >= '0'.toInt() && ch <= '9'.toInt() || ch == '.'.toInt()) { // numbers
                    while (ch >= '0'.toInt() && ch <= '9'.toInt() || ch == '.'.toInt()) {
                        nextChar()
                    }
                    x = str.substring(startPos, pos).toDouble()
                } else if (ch >= 'a'.toInt() && ch <= 'z'.toInt()) { // functions
                    while (ch >= 'a'.toInt() && ch <= 'z'.toInt()) {
                        nextChar()
                    }
                    val func = str.substring(startPos, pos)
                    x = parseFactor()
                    x = when (func) {
                        "sqrt" -> sqrt(x)
                        "sin" -> sin(Math.toRadians(x))
                        "cos" -> cos(Math.toRadians(x))
                        "tan" -> tan(Math.toRadians(x))
                        else -> throw IllegalArgumentException("Unknown function: $func")
                    }
                } else {
                    throw IllegalArgumentException("Unexpected: " + ch.toChar())
                }
                if (eat('^'.toInt())) {
                    x = x.pow(parseFactor()) // exponentiation
                }
                return x
            }
        }.parse()
    }
}