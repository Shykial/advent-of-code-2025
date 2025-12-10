package days

import utils.onFalse
import utils.readInputLines
import java.util.PriorityQueue

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
                moves = 0
            )
            val seenStates = mutableSetOf(initial.joltageDelta)
            val queue = PriorityQueue<MachineStateNode2>(compareBy({ it.distanceFromExpected }, { it.moves }))
            generateSequence(initial) { queue.poll() }
                .first { node ->
                    (node.distanceFromExpected == 0).onFalse {
                        queue += node.nextStates(machine.buttons).filter { seenStates.add(it.joltageDelta) }
                    }
                }.moves
                .also { println("Processed single machine") }
        }
    }

    fun part2Hybrid(input: List<String>): Long {
        val machines = input.map { parseMachine(it) }
        return machines.sumOf { machine ->
            val neededMovesMap = mutableMapOf<List<Int>, Long>()

            fun tryProcessSequence(sequence: List<Int>): Long = neededMovesMap.getOrPut(sequence) {
                val seenStates = mutableSetOf(sequence)
                val node = MachineStateNode3(sequence, machine.joltageRequirements, 0, 0)

                val queue = PriorityQueue<MachineStateNode3>(
                    compareBy(
                        { it.distanceFromExpected },
                        { it.moves })
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
                                    neededMoves = tryProcessSequence(newJoltageSequence),
                                    moves = node.moves + 1
                                )
                            }.filter { seenStates.add(it.joltageSequence) }
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
        val moves: Int
    ) {
        val distanceFromExpected by lazy { lightSequence.count { it } }
    }

    private data class MachineStateNode2(
        val joltageDelta: List<Int>,
        val moves: Long
    ) {
        val distanceFromExpected by lazy(mode = LazyThreadSafetyMode.NONE) { joltageDelta.sum() }
    }

    private data class MachineStateNode3(
        val joltageSequence: List<Int>,
        val expectedJoltageSequence: List<Int>,
        val moves: Long,
        val neededMoves: Long
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
                    moves = moves + 1
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
        val joltageRequirements: List<Int>
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
    println("part2: ${Day10.part2Recursive2(testInput)}")
    println("part2: ${Day10.part2Recursive2(input)}")
//    println("part2: ${Day10.part2Recursive(testInput)}")
//    println("part2: ${Day10.part2Hybrid(testInput)}")
//    println("part2: ${Day10.part2Hybrid(input)}")
//    println("part2: ${Day10.part2Recursive(input)}")
//    println("part2: ${Day10.part2(input)}")
}
