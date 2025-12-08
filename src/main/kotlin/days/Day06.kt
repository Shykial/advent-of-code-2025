package days

import utils.Regexes
import utils.readInputLines

object Day06 {
    fun part1(input: List<String>): Long {
        val (numbersStrings, operationsString) = input.dropLast(1) to input.last()
        val numbers = numbersStrings.map { line -> line.trim().split(Regexes.Whitespace).map { it.toLong() } }
        val operations = operationsString.trim().split(Regexes.Whitespace)
        return operations.withIndex().sumOf { (idx, opSymbol) ->
            val operation: (Long, Long) -> Long = when (opSymbol) {
                "+" -> Long::plus
                else -> Long::times
            }
            numbers.map { it[idx] }.reduce(operation)
        }
    }

    fun part2(input: List<String>): Long {
        val (numbersStrings, operationsString) = input.dropLast(1) to input.last()
        var sum = 0L
        var currentOperation: (Long, Long) -> Long = Long::times
        var currentAgg = 0L
        (0..numbersStrings.maxOf { it.lastIndex }).forEach { idx ->
            val char = operationsString.getOrNull(idx)?.toString().orEmpty()
            when (char) {
                "*" -> {
                    sum += currentAgg
                    currentOperation = Long::times
                    currentAgg = numbersStrings.joinToString("") { it[idx].toString().trim() }.toLong()
                }

                "+" -> {
                    sum += currentAgg
                    currentOperation = Long::plus
                    currentAgg = numbersStrings.joinToString("") { it[idx].toString().trim() }.toLong()
                }

                else -> {
                    val new = numbersStrings.joinToString("") { it[idx].toString().trim() }
                        .ifEmpty { return@forEach }
                        .toLong()
                    currentAgg = currentOperation(currentAgg, new)
                }
            }
        }
        sum += currentAgg
        return sum
    }
}

fun main() {
    val input = readInputLines("Day06")
    println("part1: ${Day06.part1(input)}")
    println("part2: ${Day06.part2(input)}")
}
