package com.example.ui.editors.spreadsheet

import java.util.Locale
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.pow
import kotlin.math.round
import kotlin.math.sqrt

object FormulaEngine {

    /**
     * Evaluates a formula or returns raw text if not starting with '='
     * @param input Raw cell formula or text (e.g. "=SUM(A1:A5)" or "1500")
     * @param cellResolver lambda to get raw text of another cell, e.g. ("A1") -> "120"
     */
    fun evaluate(input: String, cellResolver: (String) -> String): String {
        val trimmed = input.trim()
        if (!trimmed.startsWith("=")) {
            return input
        }

        val expr = trimmed.substring(1).trim()
        if (expr.isEmpty()) return ""

        return try {
            evalExpression(expr, cellResolver)
        } catch (e: Exception) {
            "#ERROR!"
        }
    }

    private fun evalExpression(expr: String, cellResolver: (String) -> String): String {
        val upper = expr.uppercase(Locale.US)

        // Check if starts with a known formula FUNCTION(args)
        val parenOpen = expr.indexOf('(')
        val parenClose = expr.lastIndexOf(')')
        if (parenOpen > 0 && parenClose == expr.length - 1) {
            val func = expr.substring(0, parenOpen).trim().uppercase(Locale.US)
            val argsStr = expr.substring(parenOpen + 1, parenClose).trim()
            val args = splitArguments(argsStr)

            return when (func) {
                "SUM" -> {
                    val numbers = resolveRangeNumbers(args, cellResolver)
                    formatResult(numbers.sum())
                }
                "AVERAGE", "AVG" -> {
                    val numbers = resolveRangeNumbers(args, cellResolver)
                    if (numbers.isEmpty()) "0" else formatResult(numbers.average())
                }
                "MIN" -> {
                    val numbers = resolveRangeNumbers(args, cellResolver)
                    if (numbers.isEmpty()) "0" else formatResult(numbers.minOrNull() ?: 0.0)
                }
                "MAX" -> {
                    val numbers = resolveRangeNumbers(args, cellResolver)
                    if (numbers.isEmpty()) "0" else formatResult(numbers.maxOrNull() ?: 0.0)
                }
                "COUNT" -> {
                    val numbers = resolveRangeNumbers(args, cellResolver)
                    numbers.size.toString()
                }
                "COUNTA" -> {
                    val cells = resolveRangeRaw(args, cellResolver)
                    cells.count { it.isNotBlank() }.toString()
                }
                "IF" -> {
                    if (args.size >= 2) {
                        val condition = evaluateCondition(args[0], cellResolver)
                        val trueVal = if (args.size > 1) evalExpression(args[1], cellResolver) else "TRUE"
                        val falseVal = if (args.size > 2) evalExpression(args[2], cellResolver) else "FALSE"
                        if (condition) trueVal else falseVal
                    } else "#VALUE!"
                }
                "AND" -> {
                    val allTrue = args.all { evaluateCondition(it, cellResolver) }
                    allTrue.toString().uppercase(Locale.US)
                }
                "OR" -> {
                    val anyTrue = args.any { evaluateCondition(it, cellResolver) }
                    anyTrue.toString().uppercase(Locale.US)
                }
                "NOT" -> {
                    val arg = args.firstOrNull() ?: ""
                    (!evaluateCondition(arg, cellResolver)).toString().uppercase(Locale.US)
                }
                "ROUND" -> {
                    val num = parseNum(args.getOrNull(0), cellResolver)
                    val dec = parseNum(args.getOrNull(1), cellResolver).toInt()
                    val mult = 10.0.pow(dec.toDouble())
                    formatResult(round(num * mult) / mult)
                }
                "ROUNDUP" -> {
                    val num = parseNum(args.getOrNull(0), cellResolver)
                    val dec = parseNum(args.getOrNull(1), cellResolver).toInt()
                    val mult = 10.0.pow(dec.toDouble())
                    formatResult(ceil(num * mult) / mult)
                }
                "ROUNDDOWN" -> {
                    val num = parseNum(args.getOrNull(0), cellResolver)
                    val dec = parseNum(args.getOrNull(1), cellResolver).toInt()
                    val mult = 10.0.pow(dec.toDouble())
                    formatResult(floor(num * mult) / mult)
                }
                "ABS" -> {
                    val num = parseNum(args.getOrNull(0), cellResolver)
                    formatResult(abs(num))
                }
                "SQRT" -> {
                    val num = parseNum(args.getOrNull(0), cellResolver)
                    if (num < 0) "#NUM!" else formatResult(sqrt(num))
                }
                "POWER" -> {
                    val base = parseNum(args.getOrNull(0), cellResolver)
                    val exp = parseNum(args.getOrNull(1), cellResolver)
                    formatResult(base.pow(exp))
                }
                "MOD" -> {
                    val a = parseNum(args.getOrNull(0), cellResolver)
                    val b = parseNum(args.getOrNull(1), cellResolver)
                    if (b == 0.0) "#DIV/0!" else formatResult(a % b)
                }
                "CONCAT", "CONCATENATE" -> {
                    val strings = resolveRangeRaw(args, cellResolver)
                    strings.joinToString("")
                }
                else -> evalArithmetic(expr, cellResolver)
            }
        }

        // Try direct simple arithmetic e.g. A1+B1 or 100*1.15
        return evalArithmetic(expr, cellResolver)
    }

    private fun parseNum(arg: String?, cellResolver: (String) -> String): Double {
        if (arg == null) return 0.0
        val resolved = resolveSingleValue(arg.trim(), cellResolver)
        return resolved.replace("$", "").replace(",", "").toDoubleOrNull() ?: 0.0
    }

    private fun resolveSingleValue(arg: String, cellResolver: (String) -> String): String {
        val trimmed = arg.trim().trim('"', '\'')
        if (isCellCoordinate(trimmed)) {
            val cellVal = cellResolver(trimmed.uppercase(Locale.US))
            return if (cellVal.startsWith("=")) evaluate(cellVal, cellResolver) else cellVal
        }
        return trimmed
    }

    private fun evaluateCondition(condition: String, cellResolver: (String) -> String): Boolean {
        val ops = listOf(">=", "<=", "!=", "=", ">", "<")
        for (op in ops) {
            val idx = condition.indexOf(op)
            if (idx > 0) {
                val leftStr = condition.substring(0, idx).trim()
                val rightStr = condition.substring(idx + op.length).trim()
                val leftVal = parseNum(leftStr, cellResolver)
                val rightVal = parseNum(rightStr, cellResolver)
                return when (op) {
                    ">=" -> leftVal >= rightVal
                    "<=" -> leftVal <= rightVal
                    "!=" -> leftVal != rightVal
                    "=" -> leftVal == rightVal
                    ">" -> leftVal > rightVal
                    "<" -> leftVal < rightVal
                    else -> false
                }
            }
        }
        val num = parseNum(condition, cellResolver)
        return num != 0.0
    }

    private fun evalArithmetic(expr: String, cellResolver: (String) -> String): String {
        val cleaned = expr.trim()
        val ops = listOf('+', '-', '*', '/')
        for (op in ops) {
            val idx = cleaned.indexOf(op)
            if (idx > 0 && idx < cleaned.length - 1) {
                val left = parseNum(cleaned.substring(0, idx), cellResolver)
                val right = parseNum(cleaned.substring(idx + 1), cellResolver)
                val res = when (op) {
                    '+' -> left + right
                    '-' -> left - right
                    '*' -> left * right
                    '/' -> if (right == 0.0) return "#DIV/0!" else left / right
                    else -> 0.0
                }
                return formatResult(res)
            }
        }

        // Single cell or number
        if (isCellCoordinate(cleaned)) {
            val raw = cellResolver(cleaned.uppercase(Locale.US))
            return if (raw.startsWith("=")) evaluate(raw, cellResolver) else raw
        }
        return cleaned
    }

    private fun splitArguments(argsStr: String): List<String> {
        if (argsStr.isBlank()) return emptyList()
        val list = mutableListOf<String>()
        var depth = 0
        var current = StringBuilder()
        for (ch in argsStr) {
            when (ch) {
                '(' -> { depth++; current.append(ch) }
                ')' -> { depth--; current.append(ch) }
                ',' -> {
                    if (depth == 0) {
                        list.add(current.toString().trim())
                        current = StringBuilder()
                    } else {
                        current.append(ch)
                    }
                }
                else -> current.append(ch)
            }
        }
        if (current.isNotBlank()) list.add(current.toString().trim())
        return list
    }

    private fun resolveRangeNumbers(args: List<String>, cellResolver: (String) -> String): List<Double> {
        val numbers = mutableListOf<Double>()
        for (arg in args) {
            if (arg.contains(":")) {
                val cells = expandRange(arg)
                for (cell in cells) {
                    val raw = cellResolver(cell)
                    val evaluated = if (raw.startsWith("=")) evaluate(raw, cellResolver) else raw
                    val clean = evaluated.replace("$", "").replace(",", "").replace("%", "").trim()
                    clean.toDoubleOrNull()?.let { numbers.add(it) }
                }
            } else {
                val num = parseNum(arg, cellResolver)
                numbers.add(num)
            }
        }
        return numbers
    }

    private fun resolveRangeRaw(args: List<String>, cellResolver: (String) -> String): List<String> {
        val list = mutableListOf<String>()
        for (arg in args) {
            if (arg.contains(":")) {
                val cells = expandRange(arg)
                for (cell in cells) {
                    val raw = cellResolver(cell)
                    val evaluated = if (raw.startsWith("=")) evaluate(raw, cellResolver) else raw
                    list.add(evaluated)
                }
            } else {
                list.add(resolveSingleValue(arg, cellResolver))
            }
        }
        return list
    }

    private fun expandRange(rangeStr: String): List<String> {
        val parts = rangeStr.split(":")
        if (parts.size != 2) return emptyList()
        val start = parts[0].trim().uppercase(Locale.US)
        val end = parts[1].trim().uppercase(Locale.US)

        val startCol = start.filter { it.isLetter() }
        val startRow = start.filter { it.isDigit() }.toIntOrNull() ?: 1
        val endCol = end.filter { it.isLetter() }
        val endRow = end.filter { it.isDigit() }.toIntOrNull() ?: 1

        val col1 = colToNumber(startCol)
        val col2 = colToNumber(endCol)
        val minCol = minOf(col1, col2)
        val maxCol = maxOf(col1, col2)
        val minRow = minOf(startRow, endRow)
        val maxRow = maxOf(startRow, endRow)

        val result = mutableListOf<String>()
        for (r in minRow..maxRow) {
            for (c in minCol..maxCol) {
                result.add("${numberToCol(c)}$r")
            }
        }
        return result
    }

    fun isCellCoordinate(str: String): Boolean {
        val s = str.trim().uppercase(Locale.US)
        if (s.length < 2) return false
        val letters = s.takeWhile { it in 'A'..'Z' }
        val digits = s.drop(letters.length)
        return letters.isNotEmpty() && digits.isNotEmpty() && digits.all { it in '0'..'9' }
    }

    private fun colToNumber(col: String): Int {
        var num = 0
        for (ch in col) {
            num = num * 26 + (ch - 'A' + 1)
        }
        return num
    }

    private fun numberToCol(num: Int): String {
        var n = num
        val sb = StringBuilder()
        while (n > 0) {
            val rem = (n - 1) % 26
            sb.append(('A'.code + rem).toChar())
            n = (n - 1) / 26
        }
        return sb.reverse().toString()
    }

    private fun formatResult(num: Double): String {
        return if (num == num.toLong().toDouble()) {
            num.toLong().toString()
        } else {
            String.format(Locale.US, "%.2f", num)
        }
    }
}
