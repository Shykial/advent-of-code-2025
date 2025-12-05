package days

import utils.readInputLines
import utils.splitBy

object Day05 {
    fun part1(input: List<String>): Int {
        val (ranges, ids) = parseInput(input)
        return ids.count { id -> ranges.any { id in it } }
    }

    fun part2(input: List<String>): Long {
        val ranges = parseInput(input).ranges
        val deduplicatedRanges = buildList<LongRange> {
            ranges
                .sortedBy { it.first }
                .forEach { newRange ->
                    val oldRange = lastOrNull()
                    when {
                        oldRange == null || newRange.first !in oldRange -> add(newRange)
                        newRange.last !in oldRange -> set(lastIndex, oldRange.first..newRange.last)
                    }
                }
        }
        return deduplicatedRanges.sumOf { it.last - it.first + 1 }
    }

    private fun parseInput(input: List<String>): ParsedInput {
        val (rangesString, idsString) = input.splitBy { it.isBlank() }
        val ranges = rangesString.map {
            val (start, end) = it.split('-')
            start.toLong()..end.toLong()
        }
        return ParsedInput(ranges = ranges, ids = idsString.map { it.toLong() })
    }

    private data class ParsedInput(val ranges: List<LongRange>, val ids: List<Long>)
}

fun main() {
    val input = readInputLines("Day05")
    println("part1: ${Day05.part1(input)}")
    println("part2: ${Day05.part2(input)}")
}
