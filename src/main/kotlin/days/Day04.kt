package days

import utils.Coordinates
import utils.getOrNull
import utils.onTrue
import utils.pointsSequence
import utils.readInputLines

object Day04 {
    fun part1(input: List<String>): Int = input.pointsSequence()
        .count { it.value == '@' && it.coordinates.isAccessibleByForklift(input::getOrNull) }

    fun part2(input: List<String>): Int {
        val mutableInput = input.map { it.toCharArray() }
        return generateSequence {
            mutableInput.withIndex().sumOf { (y, row) ->
                row.withIndex().count { (x, c) ->
                    (c == '@' && Coordinates(y, x).isAccessibleByForklift(mutableInput::getOrNull))
                        .onTrue { mutableInput[y][x] = '.' }
                }
            }
        }.takeWhile { deletedCount -> deletedCount > 0 }.sum()
    }

    // for whatever reason that's slower than the functional version,
    // decompiled bytecode looks like a mess given it's just simple loops...
    fun part2Imperative(input: List<String>): Int {
        val mutableInput = input.map { it.toCharArray() }

        var deletions = 0
        do {
            var localDeletions = 0
            mutableInput.forEachIndexed { y, row ->
                row.forEachIndexed { x, c ->
                    if (c == '@' && Coordinates(y, x).isAccessibleByForklift(mutableInput::getOrNull)) {
                        localDeletions++
                        mutableInput[y][x] = '.'
                    }
                }
            }
            deletions += localDeletions
        } while (localDeletions > 0)
        return deletions
    }

    private inline fun Coordinates.isAccessibleByForklift(charSupplier: (Coordinates) -> Char?) =
        (y - 1..y + 1).sumOf { y ->
            (x - 1..x + 1).count { x -> charSupplier(Coordinates(y, x)) == '@' }
        } < 5
}

fun main() {
    val input = readInputLines("Day04")
    println("part1: ${Day04.part1(input)}")
    println("part2: ${Day04.part2(input)}")
}
