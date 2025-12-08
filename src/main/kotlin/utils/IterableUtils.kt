package utils

fun String.cutAt(index: Int): Pair<String, String> = take(index) to drop(index)

inline fun <T> Iterable<T>.splitBy(delimiterPredicate: (T) -> Boolean): List<List<T>> = buildList {
    var currentAggregate: MutableList<T>? = null
    this@splitBy.forEach { element ->
        if (delimiterPredicate(element)) {
            currentAggregate?.let { this += it }
            currentAggregate = null
        } else {
            if (currentAggregate == null) {
                currentAggregate = mutableListOf()
            }
            currentAggregate += element
        }
    }
    currentAggregate?.let { this += it }
}

inline fun <T> Iterable<T>.filter(limit: Int, predicate: (T) -> Boolean): List<T> = buildList(limit) {
    this@filter.forEach {
        if (predicate(it)) {
            add(it)
            if (size == limit) return@buildList
        }
    }
}

fun Sequence<*>.process(): Unit = forEach { _ -> }
