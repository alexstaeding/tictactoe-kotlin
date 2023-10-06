// file name MUST end with ".main.kts" for this to work
@file:DependsOn("net.kyori:adventure-text-serializer-ansi:4.14.0")

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.JoinConfiguration
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.serializer.ansi.ANSIComponentSerializer
import kotlin.system.exitProcess

fun Component.println() = println(ANSIComponentSerializer.ansi().serialize(this))

/* ==== Global Variables ==== */

var currentPlayer = 1
var turn = 0
val grid = listOf(
    mutableListOf(0, 0, 0),
    mutableListOf(0, 0, 0),
    mutableListOf(0, 0, 0),
)

/* ==== Win Condition Checks ==== */

fun checkRow(y: Int, player: Int): Boolean {
    for (x in 0 until 3) {
        if (grid[y][x] != player) {
            return false
        }
    }
    return true
}

fun checkCol(x: Int, player: Int): Boolean {
    for (y in 0 until 3) {
        if (grid[y][x] != player) {
            return false
        }
    }
    return true
}

/**
 * Check if the given [player] has won
 */
fun checkWin(player: Int): Boolean {
    // check rows and cols
    for (coord in 0 until 3) {
        if (checkRow(coord, player) || checkCol(coord, player)) {
            return true
        }
    }
    // check diagonals
    // there are only two, so we will hardcode the check
    return grid[0][0] == player && grid[1][1] == player && grid[2][2] == player || // top left - bottom right
        grid[0][2] == player && grid[1][1] == player && grid[2][0] == player // top right - bottom left
    // if this check is false, the entire win condition is false because we already checked the rows + cols
}

/* ==== Component Builders ==== */

fun componentForPlayer(player: Int): Component = when (player) {
    1 -> Component.text("X").color(NamedTextColor.RED)
    2 -> Component.text("O").color(NamedTextColor.BLUE)
    else -> Component.space()
}

fun componentForRow(y: Int): Component =
    Component.text()
        .append(
            Component.text(" ".repeat(8)),
            Component.join(
                JoinConfiguration.separator(Component.text(" | ")),
                (0 until 3).map { x -> componentForPlayer(grid[y][x]) },
            ),
            Component.newline(),
        ).build()

fun componentForGrid(): Component =
    Component.text()
        .append(
            Component.join(
                JoinConfiguration.separator(Component.text(" ".repeat(8) + "-".repeat(8) + "\n")),
                (0 until 3).map { y -> componentForRow(y) },
            ),
        )
        .build()

/* ==== Drawing Functions ==== */

fun printGrid(y: Int, x: Int) {
    Component.text()
        .color(NamedTextColor.GRAY)
        .append(
            Component.text("Player "),
            componentForPlayer(currentPlayer),
            Component.text(" played ($y, $x)\n"),
            Component.text("=".repeat(26) + "\n"),
            componentForGrid(),
            Component.text("=".repeat(26) + "\n"),
        ).build()
        .println()
}

/* ==== Application Logic ==== */

fun readPosition(): Pair<Int, Int>? {
    Component.text()
        .color(NamedTextColor.AQUA)
        .append(
            Component.text("Turn $turn, please enter your move 'y,x', player "),
            componentForPlayer(currentPlayer),
        )
        .build()
        .println()

    val input = readln()

    // break-out condition
    if (input == "exit") {
        Component.text("Exiting...")
            .color(NamedTextColor.RED)
            .println()
        exitProcess(0)
    }

    val tokens = input.split(",")

    if (tokens.size != 2) {
        System.err.println("Incorrectly formatted input, please type two integers 'y,x'")
        return null
    }

    // checks if each token is an integer
    if (tokens.any { !it.matches("\\d".toRegex()) }) {
        System.err.println("Incorrectly formatted input, please type two integers 'y,x'")
        return null
    }

    val (y, x) = tokens.map { it.toInt() }

    if (y > 2 || x > 2 || x < 0 || y < 0) {
        System.err.println("Error, position ($y, $x) is out of bounds")
        return null
    } else if (grid[y][x] != 0) {
        System.err.println("Error, position ($y, $x) is already taken")
        return null
    }

    return y to x
}

/* ==== Main Game Loop ==== */
while (true) {
    val (y, x) = readPosition() ?: continue

    grid[y][x] = currentPlayer

    // print the grid
    printGrid(y, x)

    if (checkWin(currentPlayer)) {
        Component.text()
            .color(NamedTextColor.GREEN)
            .append(
                Component.text("Player "),
                componentForPlayer(currentPlayer),
                Component.text(" won!"),
            ).build()
            .println()
        break
    }

    // swap player 2 <-> 1
    currentPlayer = 3 - currentPlayer

    if (++turn == 9) {
        Component.text("Tie!")
            .color(NamedTextColor.GOLD)
            .println()
        break
    }
}
