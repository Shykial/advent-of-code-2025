package days

import utils.product
import utils.readInputLines

object Day11 {
    fun part1(input: List<String>): Long {
        val inputMap = parseInput(input)
        return countValidPaths("you", "out", inputMap)
    }

    fun part2(input: List<String>): Long {
        val inputMap = parseInput(input)
        return listOf(
            listOf("svr", "dac", "fft", "out"),
            listOf("svr", "fft", "dac", "out"),
        ).sumOf { devices ->
            devices.zipWithNext { from, to -> countValidPaths(from, to, inputMap) }.product()
        }
    }

    private fun countValidPaths(from: String, to: String, inputMap: Map<String, List<String>>): Long {
        val cache = mutableMapOf<String, Long>()

        fun countPaths(start: String): Long = cache.getOrPut(start) {
            if (start == to) 1 else inputMap[start].orEmpty().sumOf { countPaths(it) }
        }

        return countPaths(from)
    }

    private fun parseInput(input: List<String>): Map<String, List<String>> =
        input.associate { it.substringBefore(":") to it.substringAfter(": ").split(" ") }
}

fun main() {
    val input = readInputLines("Day11")
    println("part1: ${Day11.part1(input)}")
    println("part2: ${Day11.part2(input)}")
}
