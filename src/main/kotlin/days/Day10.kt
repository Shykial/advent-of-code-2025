package days

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import utils.onFalse
import utils.readInputLines
import java.util.PriorityQueue
import java.util.concurrent.atomic.AtomicInteger
import kotlin.time.TimeSource

private typealias Button = List<Int>

object Day10 {
    fun part1(input: List<String>): Long {
        val machines = input.map { parseMachine(it) }

        return machines.sumOf { machine ->
            val initial = MachineStateNode(lightSequence = machine.lightSequence, moves = 0)
            val seenStates = mutableSetOf(initial.lightSequence)
            val queue = PriorityQueue<MachineStateNode>(compareBy({ it.moves }, { it.distanceFromExpected }))
            generateSequence(initial) { queue.poll() }
                .first { node ->
                    (node.distanceFromExpected == 0).onFalse {
                        queue += node.nextStates(machine.buttons).filter { seenStates.add(it.lightSequence) }
                    }
                }.moves.toLong()
        }
    }

    fun part2(input: List<String>): Long {
        val machines = input.map { parseMachine(it) }

        return machines.sumOf { machine ->
            val initial = MachineStateNode2(
                joltageDelta = machine.joltageRequirements,
                moves = 0,
            )
            val seenStates = mutableSetOf(initial.joltageDelta)
            val queue = PriorityQueue<MachineStateNode2>(compareBy({ it.distanceFromExpected }, { it.moves }))
            generateSequence(initial) { queue.poll() }
                .first { node ->
                    (node.distanceFromExpected == 0).onFalse {
                        queue += node.nextStates(machine.buttons)
                            .filter { seenStates.add(it.joltageDelta) }
                    }
                }.moves
                .also { println("Processed single machine, result: $it") }
        }
    }

    fun part2Parallel(input: List<String>): Long = runBlocking(Dispatchers.Default) {
        val machines = input.map { parseMachine(it) }

        machines.mapIndexed { index, machine ->
            async {
                val initial = MachineStateNode2(
                    joltageDelta = machine.joltageRequirements,
                    moves = 0,
                )
                val seenStates = mutableSetOf(initial.joltageDelta)
                val queue = PriorityQueue<MachineStateNode2>(compareBy({ it.distanceFromExpected }, { it.moves }))
                generateSequence(initial) { queue.poll() }
                    .first { node ->
                        (node.distanceFromExpected == 0).onFalse {
                            queue += node.nextStates(machine.buttons)
                                .filter { seenStates.add(it.joltageDelta) }
                        }
                    }.moves
                    .also { println("Processed single machine for index: $index, result: $it") }
            }
        }.sumOf { it.await() }
    }

    fun part2X(input: List<String>): Long = runBlocking(Dispatchers.Default) {
        val machines = input.map { parseMachine(it) }
        val count = AtomicInteger()
        val timeMark = TimeSource.Monotonic.markNow()
        machines.mapIndexed { index, machine ->
            async {
                val localTimeMark = TimeSource.Monotonic.markNow()
                val buttons = machine.buttons
                val buttonsForIndexes = List(machine.joltageRequirements.size) { idx ->
                    buttons.filter { idx in it }
                }
                val initial = MachineStateNodeX(
                    joltageDelta = machine.joltageRequirements,
                    availableButtons = buttons,
                    moves = 0,
                )

                val seenStates = mutableSetOf(initial.joltageDelta)
                val queue = PriorityQueue<MachineStateNodeX>(
                    compareBy<MachineStateNodeX>(
                        { it.distanceFromExpected },
                        { -it.availableButtons.size },
                        { it.moves },
                    ),
                )
                generateSequence(initial) { queue.poll() }
                    .first { node ->
                        (node.distanceFromExpected == 0).onFalse {
                            queue += node.availableButtons.asSequence()
                                .mapNotNull { button ->
                                    var buttonsToRemove: MutableList<List<Int>>? = null
                                    val newDelta = node.joltageDelta.toMutableList().apply {
                                        button.forEach { idx ->
                                            val newJoltage = --this[idx]
                                            if (newJoltage < 0) return@mapNotNull null
                                            else if (newJoltage == 0) {
                                                if (buttonsToRemove == null) buttonsToRemove = mutableListOf()
                                                buttonsToRemove!! += buttonsForIndexes[idx]
                                            }
                                        }
                                    }
                                    MachineStateNodeX(
                                        joltageDelta = newDelta,
                                        availableButtons = if (buttonsToRemove == null) node.availableButtons else node.availableButtons - buttonsToRemove,
                                        moves = node.moves + 1,
                                    )
                                }
                                .filter { seenStates.add(it.joltageDelta) }
                        }
                    }.moves
                    .also {
                        println("Processed single machine for line: $index, result: $it, ${count.incrementAndGet()} / ${machines.size}, elapsed for machine: ${localTimeMark.elapsedNow()}, in total: ${timeMark.elapsedNow()}")
                    }
            }
        }.sumOf { it.await() }
    }

    fun part2_2(input: List<String>): Int {
        val machines = input.map { parseMachine(it) }

        return machines.sumOf { machine ->
            val sortedRequirements = machine.joltageRequirements.withIndex().sortedBy { it.value }
            val buttonsForIndexes = List(machine.joltageRequirements.size) { idx ->
                machine.buttons.filter { idx in it }
            }
            val buttonsMap = mutableMapOf<List<Int>, Int>()

            sortedRequirements.forEach { (index, value) ->
                val applicableButtons = buttonsForIndexes[index]
                applicableButtons.forEach {
                    buttonsMap.merge(it, value) { _, old -> minOf(old, value) }
                }
            }
            buttonsMap.values.sum().also { println("Machine result: $it") }
        }
    }

    fun part2_2Recursive(input: List<String>): Long = runBlocking(Dispatchers.Default) {
        val machines = input.map { parseMachine(it) }

        machines.mapIndexed { index, machine ->
            async {
                val sortedRequirements = machine.joltageRequirements.withIndex().sortedBy { it.value }
                val buttonsAsArrays = machine.buttons.map { it.toIntArray() }
                val buttonsForIndexes = List(machine.joltageRequirements.size) { idx ->
                    buttonsAsArrays.filter { idx in it }
                }
//            val buttonsMap = mutableMapOf<List<Int>, Int>()

                //            sortedRequirements.forEach { (index, value) ->
//                val applicableButtons = buttonsForIndexes[index]
//                applicableButtons.forEach {
//                    buttonsMap.merge(it, value) { _, old -> minOf(old, value) }
//                }
//            }
                val sortedButtons = buttonsAsArrays.sortedByDescending { it.size }
                val cache = mutableMapOf<List<Int>, Long>()

                fun trySolve(currentDelta: IntArray, availableButtons: List<IntArray>, depth: Long): Long {
                    return cache.getOrPut(currentDelta.asList()) {
                        if (currentDelta.all { it == 0 }) return@getOrPut depth
                        if (availableButtons.isEmpty()) return@getOrPut -1
                        return availableButtons
                            .firstNotNullOfOrNull { button ->
                                var buttonsToRemove: MutableList<IntArray>? = null
                                val newDelta = currentDelta.copyOf().apply {
                                    button.forEach { idx ->
                                        val newValue = --this[idx]
                                        if (newValue == 0) {
                                            if (buttonsToRemove == null) buttonsToRemove = mutableListOf()
                                            buttonsToRemove!! += buttonsForIndexes[idx]
                                        }
                                    }
                                }
                                val newButtons =
                                    if (buttonsToRemove != null) availableButtons - buttonsToRemove else availableButtons
                                trySolve(newDelta, newButtons, depth + 1L)
                                    .takeIf { it != -1L }
                            } ?: -1
                    }
                }

                trySolve(machine.joltageRequirements.toIntArray(), sortedButtons, 0)
                    .also { println("part2_2Recursive Machine result for line: $index: $it") }
            }
//            buttonsMap.values.sum().also { println("Machine result: $it") }
        }.sumOf { it.await() }
    }

    fun part2Hybrid(input: List<String>): Long {
        val machines = input.map { parseMachine(it) }
        return machines.sumOf { machine ->
            val neededMovesMap = mutableMapOf<List<Int>, Long>()
            val seenStates = mutableSetOf<List<Int>>()

            fun tryProcessSequence(sequence: List<Int>): Long = neededMovesMap.getOrPut(sequence) {
                val node = MachineStateNode3(sequence, machine.joltageRequirements, 0, 0)

                val queue = PriorityQueue<MachineStateNode3>(
                    compareBy(
                        { it.distanceFromExpected },
                        { it.moves },
                    ),
                )
                val result = generateSequence(node) { queue.poll() }
                    .firstOrNull { node ->
                        (node.distanceFromExpected == 0).onFalse {
                            queue += machine.buttons.asSequence().mapNotNull { button ->
                                val newJoltageSequence = node.joltageSequence.toMutableList().apply {
                                    button.forEach { idx ->
                                        val newJoltage = ++this[idx]
                                        if (newJoltage > node.expectedJoltageSequence[idx]) return@mapNotNull null
                                    }
                                }
                                MachineStateNode3(
                                    joltageSequence = newJoltageSequence,
                                    expectedJoltageSequence = node.expectedJoltageSequence,
                                    neededMoves = neededMovesMap.getOrPut(newJoltageSequence) {
                                        tryProcessSequence(
                                            newJoltageSequence,
                                        )
                                    },
                                    moves = node.moves + 1,
                                )
                            }.distinctBy { it.neededMoves }
                        }
                    }?.let { it.moves + it.neededMoves } ?: Long.MAX_VALUE
                result
            }
            tryProcessSequence(List(machine.joltageRequirements.size) { 0 })
                .also {
                    println("Processed line")
                }
        }
    }

    fun part2Recursive(input: List<String>): Long {
        val machines = input.map { parseMachine(it) }

        return machines.sumOf { machine ->
            val buttons = machine.buttons
            val cache = mutableMapOf<List<Int>, Long>()

            fun findMinCount(currentDelta: List<Int>, movesSoFar: Long): Long = cache.getOrPut(currentDelta) {
                if (currentDelta.all { it == 0 }) movesSoFar
                else buttons.asSequence().mapNotNull { button ->
                    currentDelta.toMutableList().apply {
                        button.forEach { idx ->
                            val newDiff = --this[idx]
                            if (newDiff < 0) return@mapNotNull null
                        }
                    }
                }.minOfOrNull { findMinCount(it, movesSoFar + 1) } ?: Long.MAX_VALUE
            }
            findMinCount(machine.joltageRequirements, 0)
                .also { println("Processed single machine in recursive way") }
        }
    }

    fun part2Recursive2(input: List<String>): Long {
        val machines = input.map { parseMachine(it) }

        return machines.sumOf { machine ->
            val buttons = machine.buttons
            val cache = mutableMapOf<List<Int>, Long>()
            val startingNode = MachineStateNode2(machine.joltageRequirements, 0)

            fun findMinCount(node: MachineStateNode2): Long? {
                cache[node.joltageDelta]?.let { return it }
                val result = if (node.distanceFromExpected == 0) node.moves
                else node.nextStates(buttons)
                    .filterTo(PriorityQueue(compareBy { it.distanceFromExpected })) { it.joltageDelta !in cache }
                    .firstNotNullOfOrNull { findMinCount(it) }

                if (result != null) {
                    cache.put(node.joltageDelta, result)
                }
                return result.also {
                    if (cache.size % 10000 == 1) println("Cache size: ${cache.size}")
                }
            }

            findMinCount(startingNode)!!
                .also { println("Processed single machine in recursive way") }
        }
    }

    private data class MachineStateNode(
        val lightSequence: List<Boolean>,
        val moves: Int,
    ) {
        val distanceFromExpected by lazy { lightSequence.count { it } }
    }

    private data class MachineStateNode2(
        val joltageDelta: List<Int>,
        val moves: Long,
    ) {
        val distanceFromExpected by lazy(mode = LazyThreadSafetyMode.NONE) { joltageDelta.sum() }
    }

    private data class MachineStateNodeX(
        val joltageDelta: List<Int>,
        val moves: Long,
        val availableButtons: List<List<Int>>,
    ) {
        val distanceFromExpected by lazy(mode = LazyThreadSafetyMode.NONE) { joltageDelta.sum() }
    }

    private data class MachineStateNode3(
        val joltageSequence: List<Int>,
        val expectedJoltageSequence: List<Int>,
        val moves: Long,
        val neededMoves: Long,
    ) {
        val distanceFromExpected by lazy(mode = LazyThreadSafetyMode.NONE) {
            joltageSequence.zip(expectedJoltageSequence) { a, b -> b - a }.sum()
        }
    }

    private fun MachineStateNode.nextStates(buttons: List<List<Int>>): Sequence<MachineStateNode> =
        buttons.asSequence().map { button ->
            val newLightSequence = lightSequence.toMutableList().apply {
                button.forEach { idx -> this[idx] = !this[idx] }
            }
            MachineStateNode(newLightSequence, moves + 1)
        }

    private fun MachineStateNode2.nextStates(buttons: List<List<Int>>): Sequence<MachineStateNode2> =
        buttons.asSequence()
            .mapNotNull { button ->
                val newDelta = joltageDelta.toMutableList().apply {
                    button.forEach { idx ->
                        val newJoltage = --this[idx]
                        if (newJoltage < 0) return@mapNotNull null
                    }
                }
                MachineStateNode2(
                    joltageDelta = newDelta,
                    moves = moves + 1,
                )
            }

    private fun parseMachine(line: String): Machine {
        val lights = line.drop(1).substringBefore(']').map { it == '#' }
        val buttons =
            Regex("""\(.*?\)""").findAll(line).map { it.value.drop(1).dropLast(1).split(',').map { it.toInt() } }
                .toList()
        val joltageRequirements =
            Regex("""\{.*?}""").find(line)!!.value.drop(1).dropLast(1).split(',').map { it.toInt() }
        return Machine(lightSequence = lights, buttons = buttons, joltageRequirements = joltageRequirements)
    }

    data class Machine(
        val lightSequence: List<Boolean>,
        val buttons: List<List<Int>>,
        val joltageRequirements: List<Int>,
    )
}

fun main() {
    val input = readInputLines("Day10")

    val testInput = """
        [.##.] (3) (1,3) (2) (2,3) (0,2) (0,1) {3,5,4,7}
        [...#.] (0,2,3,4) (2,3) (0,4) (0,1,2) (1,2,3,4) {7,5,12,7,2}
        [.###.#] (0,1,2,3,4) (0,3,4) (0,1,2,4,5) (1,2) {10,11,11,5,10,5}
    """.trimIndent().lines()

    println("part1: ${Day10.part1(testInput)}")
    println("part1: ${Day10.part1(input)}") // 594 too high????
    println("part2: ${Day10.part2(testInput)}")
    println("part2: ${Day10.part2(testInput)}")
    println("part2: ${Day10.part2_2(testInput)}")
    println("part2: ${Day10.part2_2Recursive(testInput)}")
    println("part2: ${Day10.part2X(testInput)}")
    println("part2: ${Day10.part2Parallel(testInput)}")
//    println("part2: ${Day10.part2Parallel(input)}")
    println("part2: ${Day10.part2X(input)}")
    println("part2: ${Day10.part2_2Recursive(input)}")
//    println("part2: ${Day10.part2Hybrid(testInput)}")
//    println("part2: ${Day10.part2Hybrid(input)}")
//    println("part2: ${Day10.part2(input)}")
//    println("part2: ${Day10.part2Recursive2(testInput)}")
//    println("part2: ${Day10.part2Recursive2(input)}")
//    println("part2: ${Day10.part2Recursive(testInput)}")
//    println("part2: ${Day10.part2Hybrid(testInput)}")
//    println("part2: ${Day10.part2Hybrid(input)}")
//    println("part2: ${Day10.part2Recursive(input)}")
//    println("part2: ${Day10.part2(input)}")
}
