package utils

data class Coordinates(val y: Int, val x: Int)

fun List<String>.getOrNull(coordinates: Coordinates) = getOrNull(coordinates.y)?.getOrNull(coordinates.x)

@JvmName("getOrNullCharArrays")
fun List<CharArray>.getOrNull(coordinates: Coordinates) = getOrNull(coordinates.y)?.getOrNull(coordinates.x)

data class Point<T>(val coordinates: Coordinates, val value: T)

fun List<String>.pointsSequence(): Sequence<Point<Char>> =
    asSequence().flatMapIndexed { rowNumber, row ->
        row.mapIndexed { columnNumber, value -> Point(Coordinates(rowNumber, columnNumber), value) }
    }
