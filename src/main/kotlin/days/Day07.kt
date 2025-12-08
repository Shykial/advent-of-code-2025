package days

import utils.Coordinates
import utils.readInputLines

object Day07 {
    fun part1(input: List<String>): Long {
        val sIndex = input.first().indexOf('S')
        var current = setOf(sIndex)
        var splitCount = 0L
        input.drop(1).forEach { line ->
            current = current.flatMap { i ->
                if (line[i] == '^') {
                    splitCount++
                    listOf(i - 1, i + 1)
                } else listOf(i)
            }.toSet()
        }
        return splitCount
    }

    fun part2(input: List<String>): Long {
        val sIndex = input.first().indexOf('S')
        val timelinesCache = mutableMapOf<Coordinates, Long>()

        fun countPossibleTimelines(y: Int, x: Int): Long = timelinesCache.getOrPut(Coordinates(y, x)) {
            when {
                y == input.lastIndex -> 1L
                (input[y + 1][x] == '^') -> countPossibleTimelines(y + 1, x - 1) + countPossibleTimelines(y + 1, x + 1)
                else -> countPossibleTimelines(y + 1, x)
            }
        }
        return countPossibleTimelines(0, sIndex)
    }
}

fun main() {
    val input = readInputLines("Day07")
    println("part1: ${Day07.part1(input)}")
    println("part2: ${Day07.part2(input)}")
}
