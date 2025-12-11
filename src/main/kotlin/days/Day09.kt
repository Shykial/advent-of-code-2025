package days

import utils.Coordinates
import utils.readInputLines
import java.util.LinkedList
import java.util.PriorityQueue
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

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
//        val topRight: Coordinates,
//        val bottomLeft: Coordinates,
        val bottomRight: Coordinates,
    )

    fun Rectangle.isCutBy(coordsPair: Pair<Coordinates, Coordinates>): Boolean {
        val (first, second) = coordsPair
        val smallerXRange = topLeft.x + 1..<bottomRight.x
        val smallerYRange = topLeft.y + 1..<bottomRight.y
//        val (minX, maxX) = coordsPair.toList().sortedBy { it.x }
//        val (minY, maxY) = coordsPair.toList().sortedBy { it.y }
        return if (first.x == second.x) {
            first.x in (topLeft.x + 1)..<bottomRight.x &&
                run {
                    val (minY, maxY) = coordsPair.toList().map { it.y }.sorted()
//                val affectedRange = minY..maxY
                    topLeft.y + 1 in minY..maxY || bottomRight.y - 1 in minY..maxY
//                (affectedRange.first >= smallerYRange.first && affectedRange.last >= smallerYRange.first) ||
//                    (affectedRange.first >= smallerYRange.last && affectedRange.last >= smallerYRange.last) ||
                }
        } else if (first.y == second.y) {
            first.y in (topLeft.y + 1)..<bottomRight.y &&
                run {
                    val (minX, maxX) = coordsPair.toList().map { it.x }.sorted()
//                val affectedRange = minY..maxY
                    topLeft.x + 1 in minX..maxX || bottomRight.x - 1 in minX..maxX
//                (affectedRange.first >= smallerYRange.first && affectedRange.last >= smallerYRange.first) ||
//                    (affectedRange.first >= smallerYRange.last && affectedRange.last >= smallerYRange.last) ||
                }
        } else error("should not happend")
    }

//    private fun IntRange.intersect(other: IntRange) =
//
//        operator

    fun Rectangle.contains(coordinates: Coordinates) =
        coordinates.x in topLeft.x + 1..<bottomRight.x && coordinates.y in topLeft.y + 1..<bottomRight.y
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
                val rectangle = Rectangle(
                    topLeft = Coordinates(min(a.y, b.y), min(a.x, b.x)),
                    bottomRight = Coordinates(max(a.y, b.y), max(a.x, b.x)),
                )
//                val z = coordsInOrder.none { it in rectangle }

                //                val slice1 = vectors.cyclingSequence()
//                    .sliceByElements(start = { it.first == a }, end = { it.second == b })
//                val slice2 = vectors.cyclingSequence()
//                    .sliceByElements(start = { it.first == b }, end = { it.second == a })
//                val minSlice = listOf(slice1, slice2).minBy { it.size }
//                fun List<Pair<Coordinates, Coordinates>>.toGridString2(
//                    width: Int,
//                    height: Int,
//                    first: Coordinates,
//                    second: Coordinates
//                ): String {
//                    val list = List(height + 1) { y ->
//                        CharArray(width + 1) { '.' }
//                    }
//                    this.forEach { (a, b) ->
//                        if (a.y == b.y) {
//                            val (min, max) = listOf(a.x, b.x).sorted()
//                            (min..max).forEach { x ->
//                                list[a.y][x] = 'X'
//                            }
//                        } else {
//                            val (min, max) = listOf(a.y, b.y).sorted()
//                            (min..max).forEach { y ->
//                                list[y][a.x] = 'X'
//                            }
//                        }
//                    }
//                    list[first.y][first.x] = 'O'
//                    list[second.y][second.x] = 'O'
//                    return list.joinToString("\n") { it.concatToString() }
//                }
//
//                val str = vectors.toGridString2(width = grid.maxOf { it.x }, height = grid.maxOf { it.y }, a, b)
                val z = vectors.none { rectangle.isCutBy(it) }

                z
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
    println("part2: ${Day09.part2(input)}") // TODO fix on testInput
    println("part2: ${Day09.part2(testInput)}")
}
