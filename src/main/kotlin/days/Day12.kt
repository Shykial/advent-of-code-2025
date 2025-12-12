package days

import utils.readInputLines
import utils.splitBy

object Day12 {
    fun part1(input: List<String>): Long {
        val parsedInput = parseInput(input)
        TODO()
    }

    fun part2(input: List<String>): Long {
        TODO()
    }

    private fun Region.fitsAll(presents: List<Present>): Boolean {
        TODO()
    }

    private fun parseInput(input: List<String>): ParsedInput {
        val chunked = input.splitBy { it.isEmpty() }
        val (presentsStrings, regionsStrings) = chunked.dropLast(1) to chunked.last()

        return ParsedInput(
            presents = presentsStrings.map { parsePresent(it) },
            regions = regionsStrings.map { parseRegion(it) }
        )
    }

    private fun parseRegion(regionString: String): Region {
        val (sizeString, quantitiesString) = regionString.split(": ")
        val (width, length) = sizeString.split("x").map { it.toInt() }
        return Region(
            width = width,
            length = length,
            quantities = quantitiesString.split(" ").map { it.toInt() }
        )
    }

    private fun parsePresent(presentStrings: List<String>): Present {
        val (index, shapeString) = presentStrings.first() to presentStrings.drop(1)
        return Present(
            index = index.dropLast(1).toInt(),
            originalShape = shapeString.map { line -> line.map { it == '#' } }
        )
    }

    private data class ParsedInput(
        val presents: List<Present>,
        val regions: List<Region>
    )

    private data class Region(
        val width: Int,
        val length: Int,
        val quantities: List<Int>
    )

    private data class Present(
        val index: Int,
        val shapeArrangements: List<List<List<Boolean>>>
    )

    private fun Present(index: Int, originalShape: List<List<Boolean>>): Present {
        val possibleArrangements = generateSequence(originalShape) { it.turnRight() }
            .take(4)
            .flatMap { listOf(it, it.flipHorizontally(), it.flipVertically()) }
            .distinct()
            .toList()
        return Present(index, possibleArrangements)
    }

    private fun <T> List<List<T>>.turnRight() = List(size) { y ->
        List(size) { x ->
            this[lastIndex - x][y]
        }
    }

    private fun <T> List<List<T>>.flipVertically() = List(size) { y ->
        List(size) { x -> this[lastIndex - y][x] }
    }

    private fun <T> List<List<T>>.flipHorizontally() = List(size) { y ->
        List(size) { x -> this[y][lastIndex - x] }
    }
}

fun main() {
    val input = readInputLines("Day12")

    val testInput = """
        0:
        ###
        ##.
        ##.

        1:
        ###
        ##.
        .##

        2:
        .##
        ###
        ##.

        3:
        ##.
        ###
        ##.

        4:
        ###
        #..
        ###

        5:
        ###
        .#.
        ###

        4x4: 0 0 0 0 2 0
        12x5: 1 0 1 0 2 2
        12x5: 1 0 1 0 3 2
    """.trimIndent().lines()

    println("part1: ${Day12.part1(testInput)}")
    println("part2: ${Day12.part2(testInput)}")
}
