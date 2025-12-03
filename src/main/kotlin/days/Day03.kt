package days

import utils.onFalse
import utils.readInputLines
import java.util.PriorityQueue

object Day03 {
    fun part1(input: List<String>): Long = input.sumOf { findMaxVoltageAStar(it, 2) }

    fun part2(input: List<String>): Long = input.sumOf { findMaxVoltageAStar(it, 12) }

    fun part2Linear(input: List<String>): Long = input.sumOf { findMaxVoltageLinear(it, 12) }

    private fun findMaxVoltageAStar(bank: String, numberOfBatteries: Int): Long {
        val startingNode = JoltageNode(0, numberOfBatteries, bank)
        val comparator = compareByDescending<JoltageNode> { it.joltage }
            .thenBy { it.batteriesLeft }
            .thenByDescending { it.bank.length }
        val combinationsQueue = PriorityQueue(comparator)
        return generateSequence(startingNode) { combinationsQueue.poll() }
            .first { (it.batteriesLeft == 0).onFalse { combinationsQueue += it.nextNodes() } }
            .joltage
    }

    private fun JoltageNode.nextNodes(): List<JoltageNode> {
        var max = 0
        return bank
            .dropLast(batteriesLeft - 1)
            .mapIndexedNotNull { index, value ->
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

    private fun findMaxVoltageLinear(bank: String, numberOfBatteries: Int): Long =
        buildString(numberOfBatteries) {
            var startIndex = 0
            repeat(numberOfBatteries) { consumedBatteries ->
                var max = '0'
                val endIndex = bank.length - numberOfBatteries + consumedBatteries
                for (index in startIndex..endIndex) {
                    val value = bank[index]
                    if (value == '9') {
                        max = value
                        startIndex = index + 1
                        break
                    }
                    if (value > max) {
                        max = value
                        startIndex = index + 1
                    }
                }
                append(max)
            }
        }.toLong()

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
    println("part2 with hints on solution: ${Day03.part2Linear(input)}")
}
