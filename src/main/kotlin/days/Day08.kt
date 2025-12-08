package days

import utils.filter
import utils.process
import utils.readInputLines
import java.util.PriorityQueue
import kotlin.math.sqrt

object Day08 {
    fun part1(input: List<String>): Long =
        with(JunctionBoxProcessingScope(input.map { parseJunctionBoxCoords(it) })) {
            circuitingSequence.take(1000).process()
            circuits.map { it.size }.sortedDescending().take(3).fold(1L, Long::times)
        }

    fun part2(input: List<String>): Long =
        with(JunctionBoxProcessingScope(input.map { parseJunctionBoxCoords(it) })) {
            circuitingSequence.first { circuits.size == 1 }.let { it.first.x * it.second.x }
        }

    private data class Coordinates3D(val x: Long, val y: Long, val z: Long)

    private class JunctionBoxProcessingScope(allCoords: List<Coordinates3D>) {
        private val _circuits = allCoords.map { mutableSetOf(it) }.toMutableList()
        val circuits: List<Set<Coordinates3D>> = _circuits

        private val pairsByDistanceQueue = allCoords.pairsByDistanceQueue()
        val circuitingSequence = generateSequence { pairsByDistanceQueue.poll() }
            .onEach { (first, second) ->
                val matchingCircuits = _circuits.filter(limit = 2) { first in it || second in it }
                when (matchingCircuits.size) {
                    0 -> _circuits += mutableSetOf(first, second)
                    1 -> matchingCircuits.single().let {
                        it += first
                        it += second
                    }

                    2 -> {
                        _circuits.remove(matchingCircuits.last())
                        matchingCircuits.first() += matchingCircuits.last()
                    }
                }
            }
    }

    private fun parseJunctionBoxCoords(coordsString: String) =
        coordsString
            .split(",")
            .map { it.toLong() }
            .let { (x, y, z) -> Coordinates3D(x, y, z) }

    private fun Coordinates3D.distanceFrom(other: Coordinates3D): Double =
        sqrt(((x - other.x).squared() + (y - other.y).squared() + (z - other.z).squared()).toDouble())

    private fun List<Coordinates3D>.pairsByDistanceQueue(): PriorityQueue<Pair<Coordinates3D, Coordinates3D>> =
        asSequence().flatMapIndexedTo(PriorityQueue(compareBy { it.first.distanceFrom(it.second) })) { idx, a ->
            subList(idx + 1, size).asSequence().map { b -> a to b }
        }

    private fun Long.squared(): Long = this * this
}

fun main() {
    val input = readInputLines("Day08")
    println("part1: ${Day08.part1(input)}")
    println("part2: ${Day08.part2(input)}")
}
