package utils

fun String.cutAt(index: Int): Pair<String, String> = take(index) to drop(index)
