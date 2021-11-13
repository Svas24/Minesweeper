package minesweeper

enum class GameConditions { GAME, WIN, FAILED }

class Field(val numOfMines: Int, val width: Int, val height: Int) {

    private class Cell(
        var isMine: Boolean,
        var isHidden: Boolean = true,
        var isMarked: Boolean = false,
        var nearbyMines: Int = 0) {
        override fun toString(): String = when {
            isMarked -> "*"
            isHidden -> "."
            isMine -> "X"
            nearbyMines == 0 -> "/"
            else -> nearbyMines.toString()
        }
    }

    private val cells = Array<Cell>(width * height) { Cell( it < numOfMines) }

    init {
        cells.shuffle()
    }
    var condition = GameConditions.GAME

    fun free(id: Int) {
        if (cells.all { it.isHidden }) {  // first time explore
            if (cells[id].isMine) {
                cells[cells.indexOfFirst { !it.isMine }].isMine = true
                cells[id].isMine = false
            }
            for (id in cells.indices) if (cells[id].isMine) getNeighborsIds(id).forEach { cells[it].nearbyMines++ }
        }

        if (!cells[id].isHidden) return
        if (cells[id].isMine) {  // stepped on mine
            cells.filter { it.isMine }.forEach { it.isHidden = false }  // open all mines
            condition = GameConditions.FAILED
        } else {
            openCellsRecursive(id)
            if (cells.filter { it.isHidden }.size == numOfMines) condition = GameConditions.WIN // check for WIN
        }
    }

    fun mark(id: Int) {
        if (cells[id].isHidden) cells[id].isMarked = !cells[id].isMarked
    }

    private fun getNeighborsIds(id: Int) : List<Int> {
        val neighborsIds = mutableListOf<Int>()
        val column = id % 9
        val row = id / 9
        with (neighborsIds) {
            if (row > 0) add(id - width)  // N
            if (row < 8) add(id + width)  // S
            if (column > 0) add(id - 1)  // W
            if (column < 8) add(id + 1)  // E
            if (column > 0 && row > 0) add(id - width - 1) // N-W
            if (column < 8 && row > 0) add(id - width + 1) // N-E
            if (column > 0 && row < 8) add(id + width - 1) // S-W
            if (column < 8 && row < 8) add(id + width + 1) // S-E
        }
        return neighborsIds
    }

    private fun openCellsRecursive(id: Int) {
        cells[id].isHidden = false
        cells[id].isMarked = false
        if (cells[id].nearbyMines == 0) getNeighborsIds(id).filter { cells[it].isHidden }.forEach { openCellsRecursive(it) }
    }

    override fun toString(): String = "\n |123456789|\n—│—————————│" +
            Array<String>(height) { cells.slice(it * width until (it + 1) * width)
                .joinToString("", "\n${it + 1}|", "|") }.joinToString("") +
            "\n—│—————————│"
}

fun main() {
    val width = 9
    val heigth = 9
    println("How many mines do you want on the field?")
    var numOfMines = readLine()!!.toInt()
    val field = Field(numOfMines, width, heigth)
    while (field.condition == GameConditions.GAME) {
        println(field)
        println("Set/unset mines marks or claim a cell as free:")
        val input = readLine()!!.split(" ")
        val cellId = (input[1].toInt() - 1) * 9 + input[0].toInt() - 1
        when (input[2]) {
            "free" -> field.free(cellId)
            "mine" -> field.mark(cellId)
            else -> println("Unknown command ${input[2]}")
        }
    }
    println(field)
    if (field.condition == GameConditions.FAILED) println("You stepped on a mine and failed!")
    if (field.condition == GameConditions.WIN) println("Congratulations! You found all the mines!")
}