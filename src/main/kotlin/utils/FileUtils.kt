package utils

import java.io.BufferedReader

private val classLoader = object {}::class.java.classLoader

fun readInputLines(name: String): List<String> = withResourceReader("inputs/$name.txt") { it.readLines() }

private inline fun <T> withResourceReader(path: String, block: (BufferedReader) -> T) =
    requireNotNull(classLoader.getResourceAsStream(path)) { "Resource $path not found" }
        .bufferedReader()
        .use(block)
