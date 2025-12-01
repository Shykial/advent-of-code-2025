package days

import utils.cutAt
import utils.readInputLines

object Day01 {
    fun part1(input: List<String>): Int =
        input.asSequence()
            .map { parseLine(it) }
            .runningFold(50) { counter, distance -> (counter + distance).mod(100) }
            .count { it == 0 }

    fun part2(input: List<String>): Int =
        input.asSequence()
            .map { parseLine(it) }
            .runningFold(50 to 0) { (counter), distance ->
                val unadjustedCounter = counter + distance
                val clicks = when {
                    unadjustedCounter <= 0 -> -unadjustedCounter / 100 + (if (counter == 0) 0 else 1)
                    unadjustedCounter >= 100 -> unadjustedCounter / 100
                    else -> 0
                }
                unadjustedCounter.mod(100) to clicks
            }.sumOf { (_, clicks) -> clicks }

    private fun parseLine(line: String): Int {
        val (rotation, distance) = line.cutAt(1)
        return when (rotation) {
            "L" -> -distance.toInt()
            "R" -> distance.toInt()
            else -> error("Invalid rotation: $rotation")
        }
    }
}

fun main() {
    val input = readInputLines("Day01")
    println("part1: ${Day01.part1(input)}")
    println("part2: ${Day01.part2(input)}")
}
