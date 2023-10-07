// file name MUST end with ".main.kts" for this to work
@file:DependsOn("net.kyori:adventure-text-serializer-ansi:4.14.0")
@file:DependsOn("org.jline:jline:3.22.0")

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.JoinConfiguration
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.serializer.ansi.ANSIComponentSerializer
import org.jline.terminal.TerminalBuilder

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
    val terminal = TerminalBuilder.builder()
        .system(true)
        .build()

    var sY = 1
    var sX = 1

    val input = System.`in`

    // move sY and sX with the arrow keys until the user presses enter
    while (true) {
        print("\r")
        println("$sY $sX")
        val key = input.read()

        when (key) {
            27 -> {
                // escape sequence
                val key2 = input.read()
                val key3 = input.read()
                when (key2) {
                    91 -> {
                        // arrow key
                        when (key3) {
                            65 -> sY = (sY + 2) % 3
                            66 -> sY = (sY + 1) % 3
                            67 -> sX = (sX + 1) % 3
                            68 -> sX = (sX + 2) % 3
                        }
                    }
                    10 -> return sY to sX
                }
            }
            10 -> return sY to sX
        }
    }
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
