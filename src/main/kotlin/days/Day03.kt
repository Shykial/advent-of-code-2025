package days

import utils.readInputLines
import java.util.PriorityQueue

object Day03 {
    fun part1(input: List<String>): Long = input.sumOf { findMaxVoltage(it, 2) }

    fun part2(input: List<String>): Long = input.sumOf { findMaxVoltage(it, 12) }

    private fun findMaxVoltage(bank: String, numberOfBatteries: Int): Long {
        val startingNode = JoltageNode(0, numberOfBatteries, bank)
        val comparator = compareByDescending<JoltageNode> { it.joltage }
            .thenBy { it.batteriesLeft }
            .thenByDescending { it.bank.length }
        val combinationsQueue = PriorityQueue(comparator)
        return generateSequence(startingNode) { combinationsQueue.poll() }
            .onEach { node -> combinationsQueue += node.nextNodes() }
            .first { it.batteriesLeft == 0 }
            .joltage
    }

    private fun JoltageNode.nextNodes(): List<JoltageNode> {
        var max = 0
        return bank.mapIndexedNotNull { index, value ->
            val digitValue = value.digitToInt()
            if (digitValue <= max) return@mapIndexedNotNull null
            max = digitValue
            JoltageNode(
                joltage = "$joltage$value".toLong(),
                batteriesLeft = batteriesLeft - 1,
                bank = bank.drop(index + 1),
            )
        }
    }

    private data class JoltageNode(
        val joltage: Long,
        val batteriesLeft: Int,
        val bank: String,
    )
}

fun main() {
    val input = readInputLines("Day03")
    println("part1: ${Day03.part1(input)}")
    println("part2: ${Day03.part2(input)}")
}
