package days

import utils.Coordinates
import utils.readInputLines
import java.util.LinkedList
import java.util.PriorityQueue
import kotlin.math.abs

object Day09 {
    fun part1(input: List<String>): Long {
        val grid = input.map { parseLine(it) }
        val pairsSequence = grid.combinationPairs()
        return pairsSequence.maxOf { it.areaOfRectangle() }
    }

    /*
    ............
    .......#...#
    ............
    ..#....#....
    ............
    ..#......#..
    ............
    .........#.#
     */

    enum class Turn { RIGHT, LEFT }

    data class Rectangle(
        val topLeft: Coordinates,
        val topRight: Coordinates,
        val bottomLeft: Coordinates,
        val bottomRight: Coordinates
    )

//    fun Rectangle.intersection(other: Rectangle) = Rectangle(
//        topLeft =
//    )

    fun part2(input: List<String>): Long {
        val grid = input.map { parseLine(it) }
        val remaining = LinkedList(grid)
        val start = grid.minWith(compareBy({ it.y }, { it.x }))
        remaining.remove(start)

        val coordsInOrder = generateSequence(start) { current ->
            remaining.removeFirst { it.y == current.y || it.x == current.x }
        }.takeWhileInclusive { remaining.isNotEmpty() }.toList()

        val vectors = sequence {
            yieldAll(coordsInOrder)
            yield(coordsInOrder.first())
        }.zipWithNext().toList()

        val pairsQueue = grid.combinationPairs()
            .toCollection(PriorityQueue(compareByDescending { it.areaOfRectangle() }))

        return generateSequence { pairsQueue.poll() }
            .first { (a, b) ->
                val (min, max) = listOf(a, b).sortedWith(compareBy({ it.y }, { it.x }))
//                val slice1 = vectors.cyclingSequence()
//                    .sliceByElements(start = { it.first == a }, end = { it.second == b })
//                val slice2 = vectors.cyclingSequence()
//                    .sliceByElements(start = { it.first == b }, end = { it.second == a })
//                val minSlice = listOf(slice1, slice2).minBy { it.size }
                false
            }.let { it.areaOfRectangle() }
    }

    private inline fun <T> Iterable<T>.sliceByElements(start: (T) -> Boolean, end: (T) -> Boolean): List<T> {
        val iterator = iterator()
        val agg = ArrayList<T>()
        for (e in iterator) {
            if (start(e)) {
                agg += e
                break
            }
        }
        for (e in iterator) {
            agg += e
            if (end(e)) break
        }
        return agg
    }

    private inline fun <T> Sequence<T>.sliceByElements(start: (T) -> Boolean, end: (T) -> Boolean): List<T> {
        val iterator = iterator()
        val agg = ArrayList<T>()
        for (e in iterator) {
            if (start(e)) {
                agg += e
                break
            }
        }
        for (e in iterator) {
            agg += e
            if (end(e)) break
        }
        return agg
    }

    private fun <T> List<T>.cyclingSequence() = generateSequence(0) { (it + 1) % size }.map { this[it] }


    private fun parseLine(line: String) =
        line
            .split(",")
            .map { it.toInt() }.let { (x, y) -> Coordinates(y, x) }

    private fun List<Coordinates>.toGridString(): String {
        val asSet = toSet()
        val height = maxOf { it.y } + 1
        val width = maxOf { it.x } + 1
        return List(height) { y ->
            CharArray(width) { x -> if (Coordinates(y, x) in asSet) '#' else '.' }.concatToString()
        }.joinToString("\n")
    }

    private fun <T> List<T>.combinationPairs() = asSequence().flatMapIndexed { index, a ->
        subList(index + 1, size).asSequence().map { b -> a to b }
    }

    private fun Pair<Coordinates, Coordinates>.areaOfRectangle(): Long =
        (abs(first.y - second.y) + 1L) * (abs(first.x - second.x) + 1L)
}

fun <T> MutableIterable<T>.removeFirst(predicate: (T) -> Boolean): T {
    val iterator = iterator()
    return Iterable { iterator }.first { predicate(it) }.also { iterator.remove() }
}

inline fun <T> Sequence<T>.takeWhileInclusive(crossinline predicate: (T) -> Boolean) = sequence {
    this@takeWhileInclusive.forEach {
        yield(it)
        if (!predicate(it)) return@sequence
    }
}

fun main() {
    val testInput = """
        7,1
        11,1
        11,7
        9,7
        9,5
        2,5
        2,3
        7,3
    """.trimIndent().lines()
    val testInput2 = """
        
    """.trimIndent()

    val input = readInputLines("Day09")
    println("part1: ${Day09.part1(testInput)}")
    println("part1: ${Day09.part1(input)}")
    println("part2: ${Day09.part2(testInput)}")
    println("part2: ${Day09.part2(input)}")
}