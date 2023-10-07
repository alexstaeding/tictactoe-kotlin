// file name MUST end with ".main.kts" for this to work
@file:DependsOn("net.kyori:adventure-text-serializer-ansi:4.14.0")
@file:DependsOn("org.jline:jline:3.22.0")

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.JoinConfiguration
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.text.serializer.ansi.ANSIComponentSerializer
import org.jline.terminal.Terminal
import org.jline.terminal.TerminalBuilder
import java.io.PrintWriter

data class Pos(val y: Int, val x: Int) {
    override fun toString(): String = "(y: $y, x: $x)"
}

fun PrintWriter.printlnComponent(component: Component) = println(
    ANSIComponentSerializer.ansi().serialize(
        component,
    ),
)

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

fun componentForRow(y: Int, highlight: Pos? = null): Component =
    Component.text()
        .append(
            Component.text(" ".repeat(8)),
            Component.join(
                JoinConfiguration.separator(Component.text(" | ")),
                (0 until 3).map { x ->
                    // only set bold if this is the highlighted position
                    highlight?.takeIf { (hY, hX) -> hY == y && hX == x }
                        ?.let { componentForPlayer(currentPlayer).decorate(TextDecoration.UNDERLINED) }
                        ?: componentForPlayer(grid[y][x]) // or else component without bold decoration
                },
            ),
            Component.newline(),
        ).build()

fun componentForGrid(highlight: Pos? = null): Component =
    Component.text()
        .append(
            Component.text("=".repeat(26) + "\n"),
            Component.join(
                JoinConfiguration.separator(Component.text(" ".repeat(8) + "-".repeat(8) + "\n")),
                (0 until 3).map { y -> componentForRow(y, highlight) },
            ),
            Component.text("=".repeat(26) + "\n"),
        )
        .build()

fun componentForSelectionGrid(pos: Pos): Component =
    Component.text()
        .color(NamedTextColor.GRAY)
        .append(
            Component.text("Player "),
            componentForPlayer(currentPlayer),
            Component.text(" selecting $pos - "),
            Component.text("Turn $turn\n").color(NamedTextColor.GOLD),
            componentForGrid(pos),
        ).build()

/* ==== Application Logic ==== */

var occupiedMessage: Component? = null

/**
 * Ensures that the given [this@checkPosition] is valid (i.e. not occupied) and returns it if so, otherwise returns null
 */
fun Pos.check(): Pos? {
    if (grid[y][x] == 0) {
        occupiedMessage = null
        return this
    }
    occupiedMessage = Component.text("Position $this is occupied by ").color(NamedTextColor.RED)
        .append(componentForPlayer(grid[y][x]))
    return null
}

fun readPosition(terminal: Terminal): Pos {
    val reader = terminal.reader()
    val writer = terminal.writer()
    var sY = 1
    var sX = 1

    while (true) {
        // clear screen
        writer.print("\u001b[H\u001b[2J")
        writer.flush()
        writer.printlnComponent(componentForSelectionGrid(Pos(sY, sX)))
        writer.printlnComponent(
            Component.text("Use arrow keys to move, enter to select")
                .color(NamedTextColor.LIGHT_PURPLE),
        )
        if (occupiedMessage != null) {
            writer.printlnComponent(occupiedMessage!!)
            occupiedMessage = null
        }
        writer.flush()

        val key = reader.read()
        when (key) {
            27 -> {
                val key2 = reader.read()
                val key3 = reader.read()
                when (key2) {
                    91 -> when (key3) {
                        65 -> sY = (sY + 2) % 3
                        66 -> sY = (sY + 1) % 3
                        67 -> sX = (sX + 1) % 3
                        68 -> sX = (sX + 2) % 3
                    }
                    10, 13 -> Pos(sY, sX).check()?.also { return it }
                }
            }
            10, 13 -> Pos(sY, sX).check()?.also { return it }
        }
    }
}

/* ==== Main Game Loop ==== */
fun runGameLoop(terminal: Terminal) {
    val writer = terminal.writer()
    while (true) {
        val (y, x) = readPosition(terminal)

        grid[y][x] = currentPlayer

        if (checkWin(currentPlayer)) {
            writer.printlnComponent(
                Component.text()
                    .color(NamedTextColor.GREEN)
                    .append(
                        Component.text("Player "),
                        componentForPlayer(currentPlayer),
                        Component.text(" won!"),
                    ).build(),
            )
            writer.flush()
            break
        }

        // swap player 2 <-> 1
        currentPlayer = 3 - currentPlayer

        if (++turn == 9) {
            writer.printlnComponent(Component.text("Tie!").color(NamedTextColor.GOLD))
            writer.flush()
            break
        }
    }
}

/* ==== Entry Point ==== */
TerminalBuilder.builder()
    .system(true)
    .jansi(true)
    .build()
    .also { it.enterRawMode() }
    .use(::runGameLoop)
