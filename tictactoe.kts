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

fun color(player: Int): String = when (player) {
    1 -> "X"
    2 -> "O"
    else -> " "
}

/**
 * Return the character for the player at the given position
 */
fun c(y: Int, x: Int): String = color(grid[y][x])

/* ==== Main Game Loop ==== */

// OUTER is a label used by continue on line 81
OUTER@ while (true) {
    println("Turn $turn, please enter your move 'y,x' , player ${color(currentPlayer)}")
    val eingabe = readln()

    // break-out condition
    if (eingabe == "exit") {
        break
    }

    val tokens = eingabe.split(",")

    if (tokens.size != 2) {
        System.err.println("Incorrectly formatted input, please type two integers 'y,x'")
        continue
    }

    // checks if each token is an integer
    for (token in tokens) {
        if (!token.matches("\\d".toRegex())) { // the regex \d matches only numbers
            System.err.println("Incorrectly formatted input, please type two integers 'y,x'")
            continue@OUTER // continue OUTER loop, not the one from line 78
        }
    }

    // parse position
    val (y, x) = tokens.map { it.toInt() }
    // (y, x) = String -split-> List<String> -map-> List<Int>

    // this code is equivalent
    //    val y = tokens[0].toInt()
    //    val x = tokens[1].toInt()

    if (y > 2 || x > 2 || x < 0 || y < 0) {
        System.err.println("Error, position ($y, $x) is out of bounds")
        continue
    } else if (grid[y][x] != 0) {
        System.err.println("Error, position ($y, $x) is already taken")
        continue
    } else {
        grid[y][x] = currentPlayer
    }

    // print the grid

    println(
        """
Player ${color(currentPlayer)} played ($y, $x)
==========================
        ${c(0, 0)} | ${c(0, 1)} | ${c(0, 2)}
        ---------
        ${c(1, 0)} | ${c(1, 1)} | ${c(1, 2)}
        ---------
        ${c(2, 0)} | ${c(2, 1)} | ${c(2, 2)}
==========================
""",
    )

    if (checkWin(currentPlayer)) {
        println("Player ${color(currentPlayer)} won!")
        break
    }

    // swap player 2 <-> 1
    currentPlayer = 3 - currentPlayer

    if (++turn == 9) {
        println("Tie!")
        break
    }
}
