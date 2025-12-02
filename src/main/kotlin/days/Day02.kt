package days

import utils.readInput
import kotlin.math.pow

object Day02 {
    fun part1(input: String): Long =
        parseIdSequences(input)
            .flatten()
            .filter { n ->
                n.toString().let { it.length % 2 == 0 && it.isPeriodicSequence(it.length / 2) }
            }.sum()

    fun part2(input: String): Long =
        parseIdSequences(input)
            .flatten()
            .filter { n ->
                n.toString().let { (1..it.length / 2).any { chunkSize -> it.isPeriodicSequence(chunkSize) } }
            }.sum()

    fun part2Faster(input: String): Long =
        parseIdSequences(input)
            .flatMap { generateInvalidIds(it) }
            .sum()

    private fun String.isPeriodicSequence(chunkSize: Int): Boolean {
        if (length % chunkSize != 0) return false
        val first = substring(0, chunkSize)
        return (chunkSize..<length step chunkSize).all { substring(it, it + chunkSize) == first }
    }

    private fun parseIdSequences(input: String) = input.splitToSequence(",").map { parseRange(it) }

    private fun parseRange(rangeString: String) =
        rangeString.split('-').let { (start, end) -> start.toLong()..end.toLong() }

    private fun generateInvalidIds(range: LongRange): Sequence<Long> = sequence {
        val startAsString = range.first.toString()
        val endAsString = range.last.toString()

        if (startAsString.length < endAsString.length) {
            yieldAll(generateInvalidIds(range.first..<10.0.pow(endAsString.length - 1).toLong()))
            yieldAll(generateInvalidIds(10.0.pow(endAsString.length - 1).toLong()..range.last))
        } else {
            (1..endAsString.length / 2)
                .flatMap { chunkSize ->
                    val numberOfChunks = startAsString.length / chunkSize
                    val min = startAsString.take(chunkSize).toLong()
                    val max = endAsString.take(chunkSize).toLong()
                    (min..max)
                        .asSequence()
                        .map { it.toString().repeat(numberOfChunks).toLong() }
                        .filter { it in range }
                }.distinct()
                .let { yieldAll(it) }
        }
    }
}

fun main() {
    val input = readInput("Day02")
    println("part1: ${Day02.part1(input)}")
    println("part2: ${Day02.part2(input)}")
    println("part2: ${Day02.part2Faster(input)}")
}
